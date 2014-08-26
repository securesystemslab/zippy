#!/usr/bin/env python2.7
#
# ----------------------------------------------------------------------------------------------------
#
# Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
# ----------------------------------------------------------------------------------------------------
#
r"""
mx is a command line tool for managing the development of Java code organized as suites of projects.

Version 1.x supports a single suite of projects.

Full documentation can be found at https://wiki.openjdk.java.net/display/Graal/The+mx+Tool
"""

import sys, os, errno, time, subprocess, shlex, types, StringIO, zipfile, signal, xml.sax.saxutils, tempfile, fnmatch
import multiprocessing
import textwrap
import socket
import tarfile
import hashlib
import xml.parsers.expat
import shutil, re, xml.dom.minidom
import pipes
import difflib
from collections import Callable
from threading import Thread
from argparse import ArgumentParser, REMAINDER
from os.path import join, basename, dirname, exists, getmtime, isabs, expandvars, isdir, isfile

_projects = dict()
_libs = dict()
_jreLibs = dict()
_dists = dict()
_suites = dict()
_annotationProcessors = None
_primary_suite_path = None
_primary_suite = None
_opts = None
_java_homes = None
_warn = False

"""
A distribution is a jar or zip file containing the output from one or more Java projects.
"""
class Distribution:
    def __init__(self, suite, name, path, sourcesPath, deps, mainClass, excludedDependencies, distDependencies):
        self.suite = suite
        self.name = name
        self.path = path.replace('/', os.sep)
        self.path = _make_absolute(self.path, suite.dir)
        self.sourcesPath = _make_absolute(sourcesPath.replace('/', os.sep), suite.dir) if sourcesPath else None
        self.deps = deps
        self.update_listeners = set()
        self.mainClass = mainClass
        self.excludedDependencies = excludedDependencies
        self.distDependencies = distDependencies

    def sorted_deps(self, includeLibs=False, transitive=False):
        deps = []
        if transitive:
            for depDist in [distribution(name) for name in self.distDependencies]:
                for d in depDist.sorted_deps(includeLibs=includeLibs, transitive=True):
                    if d not in deps:
                        deps.append(d)
        try:
            excl = [dependency(d) for d in self.excludedDependencies]
        except SystemExit as e:
            abort('invalid excluded dependency for {} distribution: {}'.format(self.name, e))
        return deps + [d for d in sorted_deps(self.deps, includeLibs=includeLibs) if d not in excl]

    def __str__(self):
        return self.name

    def add_update_listener(self, listener):
        self.update_listeners.add(listener)

    """
    Gets the directory in which the IDE project configuration
    for this distribution is generated. If this is a distribution
    derived from a project defining an annotation processor, then
    None is return to indicate no IDE configuration should be
    created for this distribution.
    """
    def get_ide_project_dir(self):
        if hasattr(self, 'definingProject') and self.definingProject.definedAnnotationProcessorsDist == self:
            return None
        if hasattr(self, 'subDir'):
            return join(self.suite.dir, self.subDir, self.name + '.dist')
        else:
            return join(self.suite.dir, self.name + '.dist')

    def make_archive(self):
        # are sources combined into main archive?
        unified = self.path == self.sourcesPath

        with Archiver(self.path) as arc, Archiver(None if unified else self.sourcesPath) as srcArcRaw:
            srcArc = arc if unified else srcArcRaw
            services = {}
            def overwriteCheck(zf, arcname, source):
                if not hasattr(zf, '_provenance'):
                    zf._provenance = {}
                existingSource = zf._provenance.get(arcname, None)
                isOverwrite = False
                if existingSource and existingSource != source:
                    if arcname[-1] != os.path.sep:
                        logv('warning: ' + self.path + ': avoid overwrite of ' + arcname + '\n  new: ' + source + '\n  old: ' + existingSource)
                    isOverwrite = True
                zf._provenance[arcname] = source
                return isOverwrite

            if self.mainClass:
                manifest = "Manifest-Version: 1.0\nMain-Class: %s\n\n" % (self.mainClass)
                if not overwriteCheck(arc.zf, "META-INF/MANIFEST.MF", "project files"):
                    arc.zf.writestr("META-INF/MANIFEST.MF", manifest)

            for dep in self.sorted_deps(includeLibs=True):
                if dep.isLibrary():
                    l = dep
                    # merge library jar into distribution jar
                    logv('[' + self.path + ': adding library ' + l.name + ']')
                    lpath = l.get_path(resolve=True)
                    libSourcePath = l.get_source_path(resolve=True)
                    if lpath:
                        with zipfile.ZipFile(lpath, 'r') as lp:
                            for arcname in lp.namelist():
                                if arcname.startswith('META-INF/services/') and not arcname == 'META-INF/services/':
                                    service = arcname[len('META-INF/services/'):]
                                    assert '/' not in service
                                    services.setdefault(service, []).extend(lp.read(arcname).splitlines())
                                else:
                                    if not overwriteCheck(arc.zf, arcname, lpath + '!' + arcname):
                                        arc.zf.writestr(arcname, lp.read(arcname))
                    if srcArc.zf and libSourcePath:
                        with zipfile.ZipFile(libSourcePath, 'r') as lp:
                            for arcname in lp.namelist():
                                if not overwriteCheck(srcArc.zf, arcname, lpath + '!' + arcname):
                                    srcArc.zf.writestr(arcname, lp.read(arcname))
                elif dep.isProject():
                    p = dep

                    isCoveredByDependecy = False
                    for d in self.distDependencies:
                        if p in _dists[d].sorted_deps():
                            logv("Excluding {0} from {1} because it's provided by the dependency {2}".format(p.name, self.path, d))
                            isCoveredByDependecy = True
                            break

                    if isCoveredByDependecy:
                        continue

                    # skip a  Java project if its Java compliance level is "higher" than the configured JDK
                    jdk = java(p.javaCompliance)
                    assert jdk

                    logv('[' + self.path + ': adding project ' + p.name + ']')
                    outputDir = p.output_dir()
                    for root, _, files in os.walk(outputDir):
                        relpath = root[len(outputDir) + 1:]
                        if relpath == join('META-INF', 'services'):
                            for service in files:
                                with open(join(root, service), 'r') as fp:
                                    services.setdefault(service, []).extend([provider.strip() for provider in fp.readlines()])
                        elif relpath == join('META-INF', 'providers'):
                            for provider in files:
                                with open(join(root, provider), 'r') as fp:
                                    for service in fp:
                                        services.setdefault(service.strip(), []).append(provider)
                        else:
                            for f in files:
                                arcname = join(relpath, f).replace(os.sep, '/')
                                if not overwriteCheck(arc.zf, arcname, join(root, f)):
                                    arc.zf.write(join(root, f), arcname)
                    if srcArc.zf:
                        sourceDirs = p.source_dirs()
                        if p.source_gen_dir():
                            sourceDirs.append(p.source_gen_dir())
                        for srcDir in sourceDirs:
                            for root, _, files in os.walk(srcDir):
                                relpath = root[len(srcDir) + 1:]
                                for f in files:
                                    if f.endswith('.java'):
                                        arcname = join(relpath, f).replace(os.sep, '/')
                                        if not overwriteCheck(srcArc.zf, arcname, join(root, f)):
                                            srcArc.zf.write(join(root, f), arcname)

            for service, providers in services.iteritems():
                arcname = 'META-INF/services/' + service
                arc.zf.writestr(arcname, '\n'.join(providers))

        self.notify_updated()


    def notify_updated(self):
        for l in self.update_listeners:
            l(self)

"""
A dependency is a library or project specified in a suite.
"""
class Dependency:
    def __init__(self, suite, name):
        self.name = name
        self.suite = suite

    def __str__(self):
        return self.name

    def __eq__(self, other):
        return self.name == other.name

    def __ne__(self, other):
        return self.name != other.name

    def __hash__(self):
        return hash(self.name)

    def isLibrary(self):
        return isinstance(self, Library)

    def isJreLibrary(self):
        return isinstance(self, JreLibrary)

    def isProject(self):
        return isinstance(self, Project)

class Project(Dependency):
    def __init__(self, suite, name, srcDirs, deps, javaCompliance, workingSets, d):
        Dependency.__init__(self, suite, name)
        self.srcDirs = srcDirs
        self.deps = deps
        self.checkstyleProj = name
        self.javaCompliance = JavaCompliance(javaCompliance) if javaCompliance is not None else None
        self.native = False
        self.workingSets = workingSets
        self.dir = d

        # The annotation processors defined by this project
        self.definedAnnotationProcessors = None
        self.definedAnnotationProcessorsDist = None


        # Verify that a JDK exists for this project if its compliance level is
        # less than the compliance level of the default JDK
        jdk = java(self.javaCompliance)
        if jdk is None and self.javaCompliance < java().javaCompliance:
            abort('Cannot find ' + str(self.javaCompliance) + ' JDK required by ' + name + '. ' +
                  'Specify it with --extra-java-homes option or EXTRA_JAVA_HOMES environment variable.')

        # Create directories for projects that don't yet exist
        if not exists(d):
            os.mkdir(d)
        for s in self.source_dirs():
            if not exists(s):
                os.mkdir(s)

    def all_deps(self, deps, includeLibs, includeSelf=True, includeJreLibs=False, includeAnnotationProcessors=False):
        """
        Add the transitive set of dependencies for this project, including
        libraries if 'includeLibs' is true, to the 'deps' list.
        """
        childDeps = list(self.deps)
        if includeAnnotationProcessors and len(self.annotation_processors()) > 0:
            childDeps = self.annotation_processors() + childDeps
        if self in deps:
            return deps
        for name in childDeps:
            assert name != self.name
            dep = dependency(name)
            if not dep in deps and (dep.isProject or (dep.isLibrary() and includeLibs) or (dep.isJreLibrary() and includeJreLibs)):
                dep.all_deps(deps, includeLibs=includeLibs, includeJreLibs=includeJreLibs, includeAnnotationProcessors=includeAnnotationProcessors)
        if not self in deps and includeSelf:
            deps.append(self)
        return deps

    def _compute_max_dep_distances(self, name, distances, dist):
        currentDist = distances.get(name)
        if currentDist is None or currentDist < dist:
            distances[name] = dist
            p = project(name, False)
            if p is not None:
                for dep in p.deps:
                    self._compute_max_dep_distances(dep, distances, dist + 1)

    def canonical_deps(self):
        """
        Get the dependencies of this project that are not recursive (i.e. cannot be reached
        via other dependencies).
        """
        distances = dict()
        result = set()
        self._compute_max_dep_distances(self.name, distances, 0)
        for n, d in distances.iteritems():
            assert d > 0 or n == self.name
            if d == 1:
                result.add(n)

        if len(result) == len(self.deps) and frozenset(self.deps) == result:
            return self.deps
        return result

    def max_depth(self):
        """
        Get the maximum canonical distance between this project and its most distant dependency.
        """
        distances = dict()
        self._compute_max_dep_distances(self.name, distances, 0)
        return max(distances.values())

    def source_dirs(self):
        """
        Get the directories in which the sources of this project are found.
        """
        return [join(self.dir, s) for s in self.srcDirs]

    def source_gen_dir(self):
        """
        Get the directory in which source files generated by the annotation processor are found/placed.
        """
        if self.native:
            return None
        return join(self.dir, 'src_gen')

    def output_dir(self):
        """
        Get the directory in which the class files of this project are found/placed.
        """
        if self.native:
            return None
        return join(self.dir, 'bin')

    def jasmin_output_dir(self):
        """
        Get the directory in which the Jasmin assembled class files of this project are found/placed.
        """
        if self.native:
            return None
        return join(self.dir, 'jasmin_classes')

    def append_to_classpath(self, cp, resolve):
        if not self.native:
            cp.append(self.output_dir())

    def find_classes_with_matching_source_line(self, pkgRoot, function, includeInnerClasses=False):
        """
        Scan the sources of this project for Java source files containing a line for which
        'function' returns true. A map from class name to source file path for each existing class
        corresponding to a matched source file is returned.
        """
        result = dict()
        pkgDecl = re.compile(r"^package\s+([a-zA-Z_][\w\.]*)\s*;$")
        for srcDir in self.source_dirs():
            outputDir = self.output_dir()
            for root, _, files in os.walk(srcDir):
                for name in files:
                    if name.endswith('.java') and name != 'package-info.java':
                        matchFound = False
                        source = join(root, name)
                        with open(source) as f:
                            pkg = None
                            for line in f:
                                if line.startswith("package "):
                                    match = pkgDecl.match(line)
                                    if match:
                                        pkg = match.group(1)
                                if function(line.strip()):
                                    matchFound = True
                                if pkg and matchFound:
                                    break

                        if matchFound:
                            simpleClassName = name[:-len('.java')]
                            assert pkg is not None
                            if pkgRoot is None or pkg.startswith(pkgRoot):
                                pkgOutputDir = join(outputDir, pkg.replace('.', os.path.sep))
                                if exists(pkgOutputDir):
                                    for e in os.listdir(pkgOutputDir):
                                        if includeInnerClasses:
                                            if e.endswith('.class') and (e.startswith(simpleClassName) or e.startswith(simpleClassName + '$')):
                                                className = pkg + '.' + e[:-len('.class')]
                                                result[className] = source
                                        elif e == simpleClassName + '.class':
                                            className = pkg + '.' + simpleClassName
                                            result[className] = source
        return result

    def _init_packages_and_imports(self):
        if not hasattr(self, '_defined_java_packages'):
            packages = set()
            extendedPackages = set()
            depPackages = set()
            for d in self.all_deps([], includeLibs=False, includeSelf=False):
                depPackages.update(d.defined_java_packages())
            imports = set()
            importRe = re.compile(r'import\s+(?:static\s+)?([^;]+);')
            for sourceDir in self.source_dirs():
                for root, _, files in os.walk(sourceDir):
                    javaSources = [name for name in files if name.endswith('.java')]
                    if len(javaSources) != 0:
                        pkg = root[len(sourceDir) + 1:].replace(os.sep, '.')
                        if not pkg in depPackages:
                            packages.add(pkg)
                        else:
                            # A project extends a package already defined by one of it dependencies
                            extendedPackages.add(pkg)
                            imports.add(pkg)

                        for n in javaSources:
                            with open(join(root, n)) as fp:
                                content = fp.read()
                                imports.update(importRe.findall(content))
            self._defined_java_packages = frozenset(packages)
            self._extended_java_packages = frozenset(extendedPackages)

            importedPackages = set()
            for imp in imports:
                name = imp
                while not name in depPackages and len(name) > 0:
                    lastDot = name.rfind('.')
                    if lastDot == -1:
                        name = None
                        break
                    name = name[0:lastDot]
                if name is not None:
                    importedPackages.add(name)
            self._imported_java_packages = frozenset(importedPackages)

    def defined_java_packages(self):
        """Get the immutable set of Java packages defined by the Java sources of this project"""
        self._init_packages_and_imports()
        return self._defined_java_packages

    def extended_java_packages(self):
        """Get the immutable set of Java packages extended by the Java sources of this project"""
        self._init_packages_and_imports()
        return self._extended_java_packages

    def imported_java_packages(self):
        """Get the immutable set of Java packages defined by other Java projects that are
           imported by the Java sources of this project."""
        self._init_packages_and_imports()
        return self._imported_java_packages

    """
    Gets the list of projects defining the annotation processors that will be applied
    when compiling this project. This includes the projects declared by the annotationProcessors property
    of this project and any of its project dependencies. It also includes
    any project dependencies that define an annotation processors.
    """
    def annotation_processors(self):
        if not hasattr(self, '_annotationProcessors'):
            aps = set()
            if hasattr(self, '_declaredAnnotationProcessors'):
                aps = set(self._declaredAnnotationProcessors)
                for ap in aps:
                    if project(ap).definedAnnotationProcessorsDist is None:
                        config = join(project(ap).source_dirs()[0], 'META-INF', 'services', 'javax.annotation.processing.Processor')
                        if not exists(config):
                            TimeStampFile(config).touch()
                        abort('Project ' + ap + ' declared in annotationProcessors property of ' + self.name + ' does not define any annotation processors.\n' +
                              'Please specify the annotation processors in ' + config)

            allDeps = self.all_deps([], includeLibs=False, includeSelf=False, includeAnnotationProcessors=False)
            for p in allDeps:
                # Add an annotation processor dependency
                if p.definedAnnotationProcessorsDist is not None:
                    aps.add(p.name)

                # Inherit annotation processors from dependencies
                aps.update(p.annotation_processors())

            self._annotationProcessors = list(aps)
        return self._annotationProcessors

    """
    Gets the class path composed of the distribution jars containing the 
    annotation processors that will be applied when compiling this project.
    """
    def annotation_processors_path(self):
        aps = [project(ap) for ap in self.annotation_processors()]
        if len(aps):
            return os.pathsep.join([ap.definedAnnotationProcessorsDist.path for ap in aps if ap.definedAnnotationProcessorsDist])
        return None

    def update_current_annotation_processors_file(self):
        aps = self.annotation_processors()
        outOfDate = False
        currentApsFile = join(self.suite.mxDir, 'currentAnnotationProcessors', self.name)
        currentApsFileExists = exists(currentApsFile)
        if currentApsFileExists:
            with open(currentApsFile) as fp:
                currentAps = [l.strip() for l in fp.readlines()]
                if currentAps != aps:
                    outOfDate = True
        if outOfDate or not currentApsFileExists:
            if not exists(dirname(currentApsFile)):
                os.mkdir(dirname(currentApsFile))
            with open(currentApsFile, 'w') as fp:
                for ap in aps:
                    print >> fp, ap
        return outOfDate

    def make_archive(self, path=None):
        outputDir = self.output_dir()
        if not path:
            path = join(self.dir, self.name + '.jar')
        with Archiver(path) as arc:
            for root, _, files in os.walk(outputDir):
                for f in files:
                    relpath = root[len(outputDir) + 1:]
                    arcname = join(relpath, f).replace(os.sep, '/')
                    arc.zf.write(join(root, f), arcname)
        return path

def _make_absolute(path, prefix):
    """
    Makes 'path' absolute if it isn't already by prefixing 'prefix'
    """
    if not isabs(path):
        return join(prefix, path)
    return path

def sha1OfFile(path):
    with open(path, 'rb') as f:
        d = hashlib.sha1()
        while True:
            buf = f.read(4096)
            if not buf:
                break
            d.update(buf)
        return d.hexdigest()

def download_file_with_sha1(name, path, urls, sha1, sha1path, resolve, mustExist, sources=False, canSymlink=True):
    def _download_lib():
        cacheDir = get_env('MX_CACHE_DIR', join(_opts.user_home, '.mx', 'cache'))
        if not exists(cacheDir):
            os.makedirs(cacheDir)
        base = basename(path)
        cachePath = join(cacheDir, base + '_' + sha1)

        if not exists(cachePath) or sha1OfFile(cachePath) != sha1:
            if exists(cachePath):
                log('SHA1 of ' + cachePath + ' does not match expected value (' + sha1 + ') - re-downloading')
            print 'Downloading ' + ("sources " if sources else "") + name + ' from ' + str(urls)
            download(cachePath, urls)

        d = dirname(path)
        if d != '' and not exists(d):
            os.makedirs(d)

        if canSymlink and 'symlink' in dir(os):
            if exists(path):
                os.unlink(path)
            os.symlink(cachePath, path)
        else:
            shutil.copy(cachePath, path)

    def _sha1Cached():
        with open(sha1path, 'r') as f:
            return f.read()[0:40]

    def _writeSha1Cached():
        with open(sha1path, 'w') as f:
            f.write(sha1OfFile(path))

    if resolve and mustExist and not exists(path):
        assert not len(urls) == 0, 'cannot find required library ' + name + ' ' + path
        _download_lib()

    if exists(path):
        if sha1 and not exists(sha1path):
            _writeSha1Cached()

        if sha1 and sha1 != _sha1Cached():
            _download_lib()
            if sha1 != sha1OfFile(path):
                abort("SHA1 does not match for " + name + ". Broken download? SHA1 not updated in projects file?")
            _writeSha1Cached()

    return path

class BaseLibrary(Dependency):
    def __init__(self, suite, name, optional):
        Dependency.__init__(self, suite, name)
        self.optional = optional

    def __ne__(self, other):
        result = self.__eq__(other)
        if result is NotImplemented:
            return result
        return not result

"""
A library that will be provided by the JRE but may be absent.
Any project or normal library that depends on a missing library
will be removed from the global project and library dictionaries
(i.e., _projects and _libs).

This mechanism exists primarily to be able to support code
that may use functionality in one JRE (e.g., Oracle JRE)
that is not present in another JRE (e.g., OpenJDK). A
motivating example is the Java Flight Recorder library
found in the Oracle JRE. 
"""
class JreLibrary(BaseLibrary):
    def __init__(self, suite, name, jar, optional):
        BaseLibrary.__init__(self, suite, name, optional)
        self.jar = jar

    def __eq__(self, other):
        if isinstance(other, JreLibrary):
            return self.jar == other.jar
        else:
            return NotImplemented

    def is_present_in_jdk(self, jdk):
        for e in jdk.bootclasspath().split(os.pathsep):
            if basename(e) == self.jar:
                return True
        for d in jdk.extdirs().split(os.pathsep):
            if len(d) and self.jar in os.listdir(d):
                return True
        for d in jdk.endorseddirs().split(os.pathsep):
            if len(d) and self.jar in os.listdir(d):
                return True
        return False

    def all_deps(self, deps, includeLibs, includeSelf=True, includeJreLibs=False, includeAnnotationProcessors=False):
        """
        Add the transitive set of dependencies for this JRE library to the 'deps' list.
        """
        if includeJreLibs and includeSelf and not self in deps:
            deps.append(self)
        return deps

class Library(BaseLibrary):
    def __init__(self, suite, name, path, optional, urls, sha1, sourcePath, sourceUrls, sourceSha1, deps):
        BaseLibrary.__init__(self, suite, name, optional)
        self.path = path.replace('/', os.sep)
        self.urls = urls
        self.sha1 = sha1
        self.sourcePath = sourcePath
        self.sourceUrls = sourceUrls
        if sourcePath == path:
            assert sourceSha1 is None or sourceSha1 == sha1
            sourceSha1 = sha1
        self.sourceSha1 = sourceSha1
        self.deps = deps
        abspath = _make_absolute(path, self.suite.dir)
        if not optional and not exists(abspath):
            if not len(urls):
                abort('Non-optional library {} must either exist at {} or specify one or more URLs from which it can be retrieved'.format(name, abspath))

        def _checkSha1PropertyCondition(propName, cond, inputPath):
            if not cond:
                absInputPath = _make_absolute(inputPath, self.suite.dir)
                if exists(absInputPath):
                    abort('Missing "{}" property for library {}. Add the following line to projects file:\nlibrary@{}@{}={}'.format(propName, name, name, propName, sha1OfFile(absInputPath)))
                abort('Missing "{}" property for library {}'.format(propName, name))

        _checkSha1PropertyCondition('sha1', sha1, path)
        _checkSha1PropertyCondition('sourceSha1', not sourcePath or sourceSha1, sourcePath)

        for url in urls:
            if url.endswith('/') != self.path.endswith(os.sep):
                abort('Path for dependency directory must have a URL ending with "/": path=' + self.path + ' url=' + url)

    def __eq__(self, other):
        if isinstance(other, Library):
            if len(self.urls) == 0:
                return self.path == other.path
            else:
                return self.urls == other.urls
        else:
            return NotImplemented

    def get_path(self, resolve):
        path = _make_absolute(self.path, self.suite.dir)
        sha1path = path + '.sha1'

        includedInJDK = getattr(self, 'includedInJDK', None)
        if includedInJDK and java().javaCompliance >= JavaCompliance(includedInJDK):
            return None

        bootClassPathAgent = getattr(self, 'bootClassPathAgent').lower() == 'true' if hasattr(self, 'bootClassPathAgent') else False

        return download_file_with_sha1(self.name, path, self.urls, self.sha1, sha1path, resolve, not self.optional, canSymlink=not bootClassPathAgent)

    def get_source_path(self, resolve):
        if self.sourcePath is None:
            return None
        path = _make_absolute(self.sourcePath, self.suite.dir)
        sha1path = path + '.sha1'

        return download_file_with_sha1(self.name, path, self.sourceUrls, self.sourceSha1, sha1path, resolve, len(self.sourceUrls) != 0, sources=True)

    def append_to_classpath(self, cp, resolve):
        path = self.get_path(resolve)
        if path and (exists(path) or not resolve):
            cp.append(path)

    def all_deps(self, deps, includeLibs, includeSelf=True, includeJreLibs=False, includeAnnotationProcessors=False):
        """
        Add the transitive set of dependencies for this library to the 'deps' list.
        """
        if not includeLibs:
            return deps
        childDeps = list(self.deps)
        if self in deps:
            return deps
        for name in childDeps:
            assert name != self.name
            dep = library(name)
            if not dep in deps:
                dep.all_deps(deps, includeLibs=includeLibs, includeJreLibs=includeJreLibs, includeAnnotationProcessors=includeAnnotationProcessors)
        if not self in deps and includeSelf:
            deps.append(self)
        return deps

class HgConfig:
    """
    Encapsulates access to Mercurial (hg)
    """
    def __init__(self):
        self.missing = 'no hg executable found'
        self.has_hg = None

    def check(self, abortOnFail=True):
        if self.has_hg is None:
            try:
                subprocess.check_output(['hg'])
                self.has_hg = True
            except OSError:
                self.has_hg = False
                warn(self.missing)

        if not self.has_hg:
            if abortOnFail:
                abort(self.missing)
            else:
                warn(self.missing)

    def tip(self, sDir, abortOnError=True):
        try:
            return subprocess.check_output(['hg', 'tip', '-R', sDir, '--template', '{node}'])
        except OSError:
            warn(self.missing)
        except subprocess.CalledProcessError:
            if abortOnError:
                abort('failed to get tip revision id')
            else:
                return None

    def isDirty(self, sDir, abortOnError=True):
        try:
            return len(subprocess.check_output(['hg', 'status', '-R', sDir])) > 0
        except OSError:
            warn(self.missing)
        except subprocess.CalledProcessError:
            if abortOnError:
                abort('failed to get status')
            else:
                return None

class Suite:
    def __init__(self, mxDir, primary, load=True):
        self.dir = dirname(mxDir)
        self.mxDir = mxDir
        self.projects = []
        self.libs = []
        self.jreLibs = []
        self.dists = []
        self.commands = None
        self.primary = primary
        self.requiredMxVersion = None
        self.name = _suitename(mxDir)  # validated in _load_projects
        if load:
            # just check that there are no imports
            self._load_imports()
            self._load_env()
            self._load_commands()
        _suites[self.name] = self

    def __str__(self):
        return self.name

    def _load_projects(self):
        libsMap = dict()
        jreLibsMap = dict()
        projsMap = dict()
        distsMap = dict()
        projectsFile = join(self.mxDir, 'projects')
        if not exists(projectsFile):
            return

        with open(projectsFile) as f:
            prefix = ''
            lineNum = 0

            def error(message):
                abort(projectsFile + ':' + str(lineNum) + ': ' + message)

            for line in f:
                lineNum = lineNum + 1
                line = line.strip()
                if line.endswith('\\'):
                    prefix = prefix + line[:-1]
                    continue
                if len(prefix) != 0:
                    line = prefix + line
                    prefix = ''
                if len(line) != 0 and line[0] != '#':
                    if '=' not in line:
                        error('non-comment line does not contain an "=" character')
                    key, value = line.split('=', 1)

                    parts = key.split('@')

                    if len(parts) == 1:
                        if parts[0] == 'suite':
                            if self.name != value:
                                error('suite name in project file does not match ' + _suitename(self.mxDir))
                        elif parts[0] == 'mxversion':
                            try:
                                self.requiredMxVersion = VersionSpec(value)
                            except AssertionError as ae:
                                error('Exception while parsing "mxversion" in project file: ' + str(ae))
                        else:
                            error('Single part property must be "suite": ' + key)

                        continue
                    if len(parts) != 3:
                        error('Property name does not have 3 parts separated by "@": ' + key)
                    kind, name, attr = parts
                    if kind == 'project':
                        m = projsMap
                    elif kind == 'library':
                        m = libsMap
                    elif kind == 'jrelibrary':
                        m = jreLibsMap
                    elif kind == 'distribution':
                        m = distsMap
                    else:
                        error('Property name does not start with "project@", "library@" or "distribution@": ' + key)

                    attrs = m.get(name)
                    if attrs is None:
                        attrs = dict()
                        m[name] = attrs
                    value = expandvars_in_property(value)
                    attrs[attr] = value

        def pop_list(attrs, name):
            v = attrs.pop(name, None)
            if v is None or len(v.strip()) == 0:
                return []
            return [n.strip() for n in v.split(',')]

        for name, attrs in projsMap.iteritems():
            srcDirs = pop_list(attrs, 'sourceDirs')
            deps = pop_list(attrs, 'dependencies')
            ap = pop_list(attrs, 'annotationProcessors')
            javaCompliance = attrs.pop('javaCompliance', None)
            subDir = attrs.pop('subDir', None)
            if subDir is None:
                d = join(self.dir, name)
            else:
                d = join(self.dir, subDir, name)
            workingSets = attrs.pop('workingSets', None)
            p = Project(self, name, srcDirs, deps, javaCompliance, workingSets, d)
            p.checkstyleProj = attrs.pop('checkstyle', name)
            p.native = attrs.pop('native', '') == 'true'
            if not p.native and p.javaCompliance is None:
                error('javaCompliance property required for non-native project ' + name)
            if len(ap) > 0:
                p._declaredAnnotationProcessors = ap
            p.__dict__.update(attrs)
            self.projects.append(p)

        for name, attrs in jreLibsMap.iteritems():
            jar = attrs.pop('jar')
            # JRE libraries are optional by default
            optional = attrs.pop('optional', 'true') != 'false'
            l = JreLibrary(self, name, jar, optional)
            self.jreLibs.append(l)

        for name, attrs in libsMap.iteritems():
            path = attrs.pop('path')
            urls = pop_list(attrs, 'urls')
            sha1 = attrs.pop('sha1', None)
            sourcePath = attrs.pop('sourcePath', None)
            sourceUrls = pop_list(attrs, 'sourceUrls')
            sourceSha1 = attrs.pop('sourceSha1', None)
            deps = pop_list(attrs, 'dependencies')
            # Add support optional libraries once we have a good use case
            optional = False
            l = Library(self, name, path, optional, urls, sha1, sourcePath, sourceUrls, sourceSha1, deps)
            l.__dict__.update(attrs)
            self.libs.append(l)

        for name, attrs in distsMap.iteritems():
            path = attrs.pop('path')
            sourcesPath = attrs.pop('sourcesPath', None)
            deps = pop_list(attrs, 'dependencies')
            mainClass = attrs.pop('mainClass', None)
            exclDeps = pop_list(attrs, 'exclude')
            distDeps = pop_list(attrs, 'distDependencies')
            d = Distribution(self, name, path, sourcesPath, deps, mainClass, exclDeps, distDeps)
            d.__dict__.update(attrs)
            self.dists.append(d)

        # Create a distribution for each project that defines annotation processors
        for p in self.projects:
            annotationProcessors = None
            for srcDir in p.source_dirs():
                configFile = join(srcDir, 'META-INF', 'services', 'javax.annotation.processing.Processor')
                if exists(configFile):
                    with open(configFile) as fp:
                        annotationProcessors = [ap.strip() for ap in fp]
                        if len(annotationProcessors) != 0:
                            for ap in annotationProcessors:
                                if not ap.startswith(p.name):
                                    abort(ap + ' in ' + configFile + ' does not start with ' + p.name)
            if annotationProcessors:
                dname = p.name.replace('.', '_').upper()
                apDir = join(p.dir, 'ap')
                path = join(apDir, p.name + '.jar')
                sourcesPath = None
                deps = [p.name]
                mainClass = None
                exclDeps = []
                distDeps = []
                d = Distribution(self, dname, path, sourcesPath, deps, mainClass, exclDeps, distDeps)
                d.subDir = os.path.relpath(os.path.dirname(p.dir), self.dir)
                self.dists.append(d)
                p.definedAnnotationProcessors = annotationProcessors
                p.definedAnnotationProcessorsDist = d
                d.definingProject = p

                # Restrict exported annotation processors to those explicitly defined by the project
                def _refineAnnotationProcessorServiceConfig(dist):
                    aps = dist.definingProject.definedAnnotationProcessors
                    apsJar = dist.path
                    config = 'META-INF/services/javax.annotation.processing.Processor'
                    with zipfile.ZipFile(apsJar, 'r') as zf:
                        currentAps = zf.read(config).split()
                    if currentAps != aps:
                        logv('[updating ' + config + ' in ' + apsJar + ']')
                        with Archiver(apsJar) as arc, zipfile.ZipFile(apsJar, 'r') as lp:
                            for arcname in lp.namelist():
                                if arcname == config:
                                    arc.zf.writestr(arcname, '\n'.join(aps))
                                else:
                                    arc.zf.writestr(arcname, lp.read(arcname))
                d.add_update_listener(_refineAnnotationProcessorServiceConfig)
                self.dists.append(d)

        if self.name is None:
            abort('Missing "suite=<name>" in ' + projectsFile)

    def _commands_name(self):
        return 'mx_' + self.name.replace('-', '_')

    def _find_commands(self, name):
        commandsPath = join(self.mxDir, name + '.py')
        if exists(commandsPath):
            return name
        else:
            return None

    def _load_commands(self):
        commandsName = self._find_commands(self._commands_name())
        if commandsName is None:
            # backwards compatibility
            commandsName = self._find_commands('commands')
        if commandsName is not None:
            if commandsName in sys.modules:
                abort(commandsName + '.py in suite ' + self.name + ' duplicates ' + sys.modules[commandsName].__file__)
            # temporarily extend the Python path
            sys.path.insert(0, self.mxDir)
            mod = __import__(commandsName)

            self.commands = sys.modules.pop(commandsName)
            sys.modules[commandsName] = self.commands

            # revert the Python path
            del sys.path[0]

            if not hasattr(mod, 'mx_init'):
                abort(commandsName + '.py in suite ' + self.name + ' must define an mx_init(suite) function')
            if hasattr(mod, 'mx_post_parse_cmd_line'):
                self.mx_post_parse_cmd_line = mod.mx_post_parse_cmd_line

            mod.mx_init(self)
            self.commands = mod

    def _load_imports(self):
        if exists(join(self.mxDir, 'imports')):
            abort('multiple suites are not supported in this version of mx')

    def _load_env(self):
        e = join(self.mxDir, 'env')
        if exists(e):
            with open(e) as f:
                lineNum = 0
                for line in f:
                    lineNum = lineNum + 1
                    line = line.strip()
                    if len(line) != 0 and line[0] != '#':
                        if not '=' in line:
                            abort(e + ':' + str(lineNum) + ': line does not match pattern "key=value"')
                        key, value = line.split('=', 1)
                        os.environ[key.strip()] = expandvars_in_property(value.strip())

    def _post_init(self, opts):
        self._load_projects()
        if self.requiredMxVersion is None:
            warn("This suite does not express any required mx version. Consider adding 'mxversion=<version>' to your projects file.")
        elif self.requiredMxVersion > version:
            abort("This suite requires mx version " + str(self.requiredMxVersion) + " while your current mx version is " + str(version) + ". Please update mx.")
        # set the global data structures, checking for conflicts unless _check_global_structures is False
        for p in self.projects:
            existing = _projects.get(p.name)
            if existing is not None:
                abort('cannot override project  ' + p.name + ' in ' + p.dir + " with project of the same name in  " + existing.dir)
            if not p.name in _opts.ignored_projects:
                _projects[p.name] = p
        for l in self.libs:
            existing = _libs.get(l.name)
            # Check that suites that define same library are consistent
            if existing is not None and existing != l:
                abort('inconsistent library redefinition of ' + l.name + ' in ' + existing.suite.dir + ' and ' + l.suite.dir)
            _libs[l.name] = l
        for l in self.jreLibs:
            existing = _jreLibs.get(l.name)
            # Check that suites that define same library are consistent
            if existing is not None and existing != l:
                abort('inconsistent JRE library redefinition of ' + l.name + ' in ' + existing.suite.dir + ' and ' + l.suite.dir)
            _jreLibs[l.name] = l
        for d in self.dists:
            existing = _dists.get(d.name)
            if existing is not None:
                # allow redefinition, so use path from existing
                # abort('cannot redefine distribution  ' + d.name)
                warn('distribution ' + d.name + ' redefined')
                d.path = existing.path
            _dists[d.name] = d

        # Remove projects and libraries that (recursively) depend on an optional library
        # whose artifact does not exist or on a JRE library that is not present in the
        # JDK for a project. Also remove projects whose Java compliance requirement
        # cannot be satisfied by the configured JDKs.
        #
        # Removed projects and libraries are also removed from
        # distributions in they are listed as dependencies.
        for d in sorted_deps(includeLibs=True):
            if d.isLibrary():
                if d.optional:
                    try:
                        d.optional = False
                        path = d.get_path(resolve=True)
                    except SystemExit:
                        path = None
                    finally:
                        d.optional = True
                    if not path:
                        logv('[omitting optional library {} as {} does not exist]'.format(d, d.path))
                        del _libs[d.name]
                        self.libs.remove(d)
            elif d.isProject():
                if java(d.javaCompliance) is None:
                    logv('[omitting project {} as Java compliance {} cannot be satisfied by configured JDKs]'.format(d, d.javaCompliance))
                    del _projects[d.name]
                    self.projects.remove(d)
                else:
                    for name in list(d.deps):
                        jreLib = _jreLibs.get(name)
                        if jreLib:
                            if not jreLib.is_present_in_jdk(java(d.javaCompliance)):
                                if jreLib.optional:
                                    logv('[omitting project {} as dependency {} is missing]'.format(d, name))
                                    del _projects[d.name]
                                    self.projects.remove(d)
                                else:
                                    abort('JRE library {} required by {} not found'.format(jreLib, d))
                        elif not dependency(name, fatalIfMissing=False):
                            logv('[omitting project {} as dependency {} is missing]'.format(d, name))
                            del _projects[d.name]
                            self.projects.remove(d)
        for dist in _dists.itervalues():
            for name in list(dist.deps):
                if not dependency(name, fatalIfMissing=False):
                    logv('[omitting {} from distribution {}]'.format(name, dist))
                    dist.deps.remove(name)

        if hasattr(self, 'mx_post_parse_cmd_line'):
            self.mx_post_parse_cmd_line(opts)

class XMLElement(xml.dom.minidom.Element):
    def writexml(self, writer, indent="", addindent="", newl=""):
        writer.write(indent + "<" + self.tagName)

        attrs = self._get_attributes()
        a_names = attrs.keys()
        a_names.sort()

        for a_name in a_names:
            writer.write(" %s=\"" % a_name)
            xml.dom.minidom._write_data(writer, attrs[a_name].value)
            writer.write("\"")
        if self.childNodes:
            if not self.ownerDocument.padTextNodeWithoutSiblings and len(self.childNodes) == 1 and isinstance(self.childNodes[0], xml.dom.minidom.Text):
                # if the only child of an Element node is a Text node, then the
                # text is printed without any indentation or new line padding
                writer.write(">")
                self.childNodes[0].writexml(writer)
                writer.write("</%s>%s" % (self.tagName, newl))
            else:
                writer.write(">%s" % (newl))
                for node in self.childNodes:
                    node.writexml(writer, indent + addindent, addindent, newl)
                writer.write("%s</%s>%s" % (indent, self.tagName, newl))
        else:
            writer.write("/>%s" % (newl))

class XMLDoc(xml.dom.minidom.Document):

    def __init__(self):
        xml.dom.minidom.Document.__init__(self)
        self.current = self
        self.padTextNodeWithoutSiblings = False

    def createElement(self, tagName):
        # overwritten to create XMLElement
        e = XMLElement(tagName)
        e.ownerDocument = self
        return e

    def comment(self, txt):
        self.current.appendChild(self.createComment(txt))

    def open(self, tag, attributes=None, data=None):
        if attributes is None:
            attributes = {}
        element = self.createElement(tag)
        for key, value in attributes.items():
            element.setAttribute(key, value)
        self.current.appendChild(element)
        self.current = element
        if data is not None:
            element.appendChild(self.createTextNode(data))
        return self

    def close(self, tag):
        assert self.current != self
        assert tag == self.current.tagName, str(tag) + ' != ' + self.current.tagName
        self.current = self.current.parentNode
        return self

    def element(self, tag, attributes=None, data=None):
        if attributes is None:
            attributes = {}
        return self.open(tag, attributes, data).close(tag)

    def xml(self, indent='', newl='', escape=False, standalone=None):
        assert self.current == self
        result = self.toprettyxml(indent, newl, encoding="UTF-8")
        if escape:
            entities = {'"':  "&quot;", "'":  "&apos;", '\n': '&#10;'}
            result = xml.sax.saxutils.escape(result, entities)
        if standalone is not None:
            result = result.replace('encoding="UTF-8"?>', 'encoding="UTF-8" standalone="' + str(standalone) + '"?>')
        return result

def get_os():
    """
    Get a canonical form of sys.platform.
    """
    if sys.platform.startswith('darwin'):
        return 'darwin'
    elif sys.platform.startswith('linux'):
        return 'linux'
    elif sys.platform.startswith('sunos'):
        return 'solaris'
    elif sys.platform.startswith('win32') or sys.platform.startswith('cygwin'):
        return 'windows'
    else:
        abort('Unknown operating system ' + sys.platform)

def _loadSuite(mxDir, primary=False):
    """
    Load a suite from 'mxDir'.
    """
    for s in _suites.itervalues():
        if s.mxDir == mxDir:
            return s
    # create the new suite
    s = Suite(mxDir, primary)
    return s

def suites(opt_limit_to_suite=False):
    """
    Get the list of all loaded suites.
    """
    return _suites.values()

def suite(name, fatalIfMissing=True):
    """
    Get the suite for a given name.
    """
    s = _suites.get(name)
    if s is None and fatalIfMissing:
        abort('suite named ' + name + ' not found')
    return s


def projects_from_names(projectNames):
    """
    Get the list of projects corresponding to projectNames; all projects if None
    """
    if projectNames is None:
        return projects()
    else:
        return [project(name) for name in projectNames]

def projects(opt_limit_to_suite=False):
    """
    Get the list of all loaded projects limited by --suite option if opt_limit_to_suite == True
    """
    sortedProjects = sorted(_projects.values(), key=lambda p: p.name)
    if opt_limit_to_suite:
        return _projects_opt_limit_to_suites(sortedProjects)
    else:
        return sortedProjects

def projects_opt_limit_to_suites():
    """
    Get the list of all loaded projects optionally limited by --suite option
    """
    return projects(True)

def _projects_opt_limit_to_suites(projects):
    return projects

def annotation_processors():
    """
    Get the list of all loaded projects that define an annotation processor.
    """
    global _annotationProcessors
    if _annotationProcessors is None:
        aps = set()
        for p in projects():
            for ap in p.annotation_processors():
                if project(ap, False):
                    aps.add(ap)
        _annotationProcessors = list(aps)
    return _annotationProcessors

def distribution(name, fatalIfMissing=True):
    """
    Get the distribution for a given name. This will abort if the named distribution does
    not exist and 'fatalIfMissing' is true.
    """
    d = _dists.get(name)
    if d is None and fatalIfMissing:
        abort('distribution named ' + name + ' not found')
    return d

def dependency(name, fatalIfMissing=True):
    """
    Get the project or library for a given name. This will abort if a project  or library does
    not exist for 'name' and 'fatalIfMissing' is true.
    """
    d = _projects.get(name)
    if d is None:
        d = _libs.get(name)
        if d is None:
            d = _jreLibs.get(name)
    if d is None and fatalIfMissing:
        if name in _opts.ignored_projects:
            abort('project named ' + name + ' is ignored')
        abort('project or library named ' + name + ' not found')
    return d

def project(name, fatalIfMissing=True):
    """
    Get the project for a given name. This will abort if the named project does
    not exist and 'fatalIfMissing' is true.
    """
    p = _projects.get(name)
    if p is None and fatalIfMissing:
        if name in _opts.ignored_projects:
            abort('project named ' + name + ' is ignored')
        abort('project named ' + name + ' not found')
    return p

def library(name, fatalIfMissing=True):
    """
    Gets the library for a given name. This will abort if the named library does
    not exist and 'fatalIfMissing' is true.
    """
    l = _libs.get(name)
    if l is None and fatalIfMissing:
        if _projects.get(name):
            abort(name + ' is a project, not a library')
        abort('library named ' + name + ' not found')
    return l

def _as_classpath(deps, resolve):
    cp = []
    if _opts.cp_prefix is not None:
        cp = [_opts.cp_prefix]
    for d in deps:
        d.append_to_classpath(cp, resolve)
    if _opts.cp_suffix is not None:
        cp += [_opts.cp_suffix]
    return os.pathsep.join(cp)

def classpath(names=None, resolve=True, includeSelf=True, includeBootClasspath=False):
    """
    Get the class path for a list of given dependencies and distributions, resolving each entry in the
    path (e.g. downloading a missing library) if 'resolve' is true.
    """
    if names is None:
        deps = sorted_deps(includeLibs=True)
        dists = list(_dists.values())
    else:
        deps = []
        dists = []
        if isinstance(names, types.StringTypes):
            names = [names]
        for n in names:
            dep = dependency(n, fatalIfMissing=False)
            if dep:
                dep.all_deps(deps, True, includeSelf)
            else:
                dist = distribution(n)
                if not dist:
                    abort('project, library or distribution named ' + n + ' not found')
                dists.append(dist)

    if len(dists):
        distsDeps = set()
        for d in dists:
            distsDeps.update(d.sorted_deps())

        # remove deps covered by a dist that will be on the class path
        deps = [d for d in deps if d not in distsDeps]

    result = _as_classpath(deps, resolve)

    # prepend distributions
    if len(dists):
        distsCp = os.pathsep.join(dist.path for dist in dists)
        if len(result):
            result = distsCp + os.pathsep + result
        else:
            result = distsCp

    if includeBootClasspath:
        result = os.pathsep.join([java().bootclasspath(), result])
    return result

def classpath_walk(names=None, resolve=True, includeSelf=True, includeBootClasspath=False):
    """
    Walks the resources available in a given classpath, yielding a tuple for each resource
    where the first member of the tuple is a directory path or ZipFile object for a
    classpath entry and the second member is the qualified path of the resource relative
    to the classpath entry.
    """
    cp = classpath(names, resolve, includeSelf, includeBootClasspath)
    for entry in cp.split(os.pathsep):
        if not exists(entry):
            continue
        if isdir(entry):
            for root, dirs, files in os.walk(entry):
                for d in dirs:
                    entryPath = join(root[len(entry) + 1:], d)
                    yield entry, entryPath
                for f in files:
                    entryPath = join(root[len(entry) + 1:], f)
                    yield entry, entryPath
        elif entry.endswith('.jar') or entry.endswith('.zip'):
            with zipfile.ZipFile(entry, 'r') as zf:
                for zi in zf.infolist():
                    entryPath = zi.filename
                    yield zf, entryPath

def sorted_deps(projectNames=None, includeLibs=False, includeJreLibs=False, includeAnnotationProcessors=False):
    """
    Gets projects and libraries sorted such that dependencies
    are before the projects that depend on them. Unless 'includeLibs' is
    true, libraries are omitted from the result.
    """
    projects = projects_from_names(projectNames)

    return sorted_project_deps(projects, includeLibs=includeLibs, includeJreLibs=includeJreLibs, includeAnnotationProcessors=includeAnnotationProcessors)

def sorted_dists():
    """
    Gets distributions sorted such that each distribution comes after
    any distributions it depends upon.
    """
    dists = []
    def add_dist(dist):
        if not dist in dists:
            for depDist in [distribution(name) for name in dist.distDependencies]:
                add_dist(depDist)
            if not dist in dists:
                dists.append(dist)

    for d in _dists.itervalues():
        add_dist(d)
    return dists

def sorted_project_deps(projects, includeLibs=False, includeJreLibs=False, includeAnnotationProcessors=False):
    deps = []
    for p in projects:
        p.all_deps(deps, includeLibs=includeLibs, includeJreLibs=includeJreLibs, includeAnnotationProcessors=includeAnnotationProcessors)
    return deps

def _handle_missing_java_home():
    if not sys.stdout.isatty():
        abort('Could not find bootstrap JDK. Use --java-home option or ensure JAVA_HOME environment variable is set.')

    candidateJdks = []
    if get_os() == 'darwin':
        base = '/Library/Java/JavaVirtualMachines'
        candidateJdks = [join(base, n, 'Contents/Home') for n in os.listdir(base) if exists(join(base, n, 'Contents/Home'))]
    elif get_os() == 'linux':
        base = '/usr/lib/jvm'
        candidateJdks = [join(base, n) for n in os.listdir(base) if exists(join(base, n, 'jre/lib/rt.jar'))]
    elif get_os() == 'solaris':
        base = '/usr/jdk/instances'
        candidateJdks = [join(base, n) for n in os.listdir(base) if exists(join(base, n, 'jre/lib/rt.jar'))]
    elif get_os() == 'windows':
        base = r'C:\Program Files\Java'
        candidateJdks = [join(base, n) for n in os.listdir(base) if exists(join(base, n, r'jre\lib\rt.jar'))]

    javaHome = None
    if len(candidateJdks) != 0:
        javaHome = select_items(candidateJdks + ['<other>'], allowMultiple=False)
        if javaHome == '<other>':
            javaHome = None

    while javaHome is None:
        javaHome = raw_input('Enter path of bootstrap JDK: ')
        rtJarPath = join(javaHome, 'jre', 'lib', 'rt.jar')
        if not exists(rtJarPath):
            log('Does not appear to be a valid JDK as ' + rtJarPath + ' does not exist')
            javaHome = None
        else:
            break

    envPath = join(_primary_suite.mxDir, 'env')
    if ask_yes_no('Persist this setting by adding "JAVA_HOME=' + javaHome + '" to ' + envPath, 'y'):
        with open(envPath, 'a') as fp:
            print >> fp, 'JAVA_HOME=' + javaHome

    return javaHome

class ArgParser(ArgumentParser):
    # Override parent to append the list of available commands
    def format_help(self):
        return ArgumentParser.format_help(self) + _format_commands()


    def __init__(self):
        self.java_initialized = False
        # this doesn't resolve the right way, but can't figure out how to override _handle_conflict_resolve in _ActionsContainer
        ArgumentParser.__init__(self, prog='mx', conflict_handler='resolve')

        self.add_argument('-v', action='store_true', dest='verbose', help='enable verbose output')
        self.add_argument('-V', action='store_true', dest='very_verbose', help='enable very verbose output')
        self.add_argument('-w', action='store_true', dest='warn', help='enable warning messages')
        self.add_argument('-p', '--primary-suite-path', help='set the primary suite directory', metavar='<path>')
        self.add_argument('--dbg', type=int, dest='java_dbg_port', help='make Java processes wait on <port> for a debugger', metavar='<port>')
        self.add_argument('-d', action='store_const', const=8000, dest='java_dbg_port', help='alias for "-dbg 8000"')
        self.add_argument('--cp-pfx', dest='cp_prefix', help='class path prefix', metavar='<arg>')
        self.add_argument('--cp-sfx', dest='cp_suffix', help='class path suffix', metavar='<arg>')
        self.add_argument('--J', dest='java_args', help='Java VM arguments (e.g. --J @-dsa)', metavar='@<args>')
        self.add_argument('--Jp', action='append', dest='java_args_pfx', help='prefix Java VM arguments (e.g. --Jp @-dsa)', metavar='@<args>', default=[])
        self.add_argument('--Ja', action='append', dest='java_args_sfx', help='suffix Java VM arguments (e.g. --Ja @-dsa)', metavar='@<args>', default=[])
        self.add_argument('--user-home', help='users home directory', metavar='<path>', default=os.path.expanduser('~'))
        self.add_argument('--java-home', help='primary JDK directory (must be JDK 7 or later)', metavar='<path>')
        self.add_argument('--extra-java-homes', help='secondary JDK directories separated by "' + os.pathsep + '"', metavar='<path>')
        self.add_argument('--ignore-project', action='append', dest='ignored_projects', help='name of project to ignore', metavar='<name>', default=[])
        self.add_argument('--kill-with-sigquit', action='store_true', dest='killwithsigquit', help='send sigquit first before killing child processes')
        if get_os() != 'windows':
            # Time outs are (currently) implemented with Unix specific functionality
            self.add_argument('--timeout', help='timeout (in seconds) for command', type=int, default=0, metavar='<secs>')
            self.add_argument('--ptimeout', help='timeout (in seconds) for subprocesses', type=int, default=0, metavar='<secs>')

    def _parse_cmd_line(self, args=None):
        if args is None:
            args = sys.argv[1:]

        self.add_argument('commandAndArgs', nargs=REMAINDER, metavar='command args...')

        opts = self.parse_args()

        # Give the timeout options a default value to avoid the need for hasattr() tests
        opts.__dict__.setdefault('timeout', 0)
        opts.__dict__.setdefault('ptimeout', 0)

        if opts.very_verbose:
            opts.verbose = True

        if opts.java_home is None:
            opts.java_home = os.environ.get('JAVA_HOME')
        if opts.extra_java_homes is None:
            opts.extra_java_homes = os.environ.get('EXTRA_JAVA_HOMES')

        if opts.java_home is None or opts.java_home == '':
            opts.java_home = _handle_missing_java_home()

        if opts.user_home is None or opts.user_home == '':
            abort('Could not find user home. Use --user-home option or ensure HOME environment variable is set.')

        os.environ['JAVA_HOME'] = opts.java_home
        os.environ['HOME'] = opts.user_home

        opts.ignored_projects = opts.ignored_projects + os.environ.get('IGNORED_PROJECTS', '').split(',')

        commandAndArgs = opts.__dict__.pop('commandAndArgs')
        return opts, commandAndArgs

    def _handle_conflict_resolve(self, action, conflicting_actions):
        self._handle_conflict_error(action, conflicting_actions)

def _format_commands():
    msg = '\navailable commands:\n\n'
    for cmd in sorted(_commands.iterkeys()):
        c, _ = _commands[cmd][:2]
        doc = c.__doc__
        if doc is None:
            doc = ''
        msg += ' {0:<20} {1}\n'.format(cmd, doc.split('\n', 1)[0])
    return msg + '\n'

def java(requiredCompliance=None):
    """
    Get a JavaConfig object containing Java commands launch details.
    If requiredCompliance is None, the compliance level specified by --java-home/JAVA_HOME
    is returned. Otherwise, the JavaConfig exactly matching requiredCompliance is returned
    or None if there is no exact match.
    """
    assert _java_homes
    if not requiredCompliance:
        return _java_homes[0]
    for java in _java_homes:
        if java.javaCompliance == requiredCompliance:
            return java
    return None


def run_java(args, nonZeroIsFatal=True, out=None, err=None, cwd=None, addDefaultArgs=True, javaConfig=None):
    if not javaConfig:
        javaConfig = java()
    return run(javaConfig.format_cmd(args, addDefaultArgs), nonZeroIsFatal=nonZeroIsFatal, out=out, err=err, cwd=cwd)

def _kill_process_group(pid, sig):
    if not sig:
        sig = signal.SIGKILL
    pgid = os.getpgid(pid)
    try:
        os.killpg(pgid, sig)
        return True
    except:
        log('Error killing subprocess ' + str(pgid) + ': ' + str(sys.exc_info()[1]))
        return False

def _waitWithTimeout(process, args, timeout):
    def _waitpid(pid):
        while True:
            try:
                return os.waitpid(pid, os.WNOHANG)
            except OSError, e:
                if e.errno == errno.EINTR:
                    continue
                raise

    def _returncode(status):
        if os.WIFSIGNALED(status):
            return -os.WTERMSIG(status)
        elif os.WIFEXITED(status):
            return os.WEXITSTATUS(status)
        else:
            # Should never happen
            raise RuntimeError("Unknown child exit status!")

    end = time.time() + timeout
    delay = 0.0005
    while True:
        (pid, status) = _waitpid(process.pid)
        if pid == process.pid:
            return _returncode(status)
        remaining = end - time.time()
        if remaining <= 0:
            abort('Process timed out after {0} seconds: {1}'.format(timeout, ' '.join(args)))
        delay = min(delay * 2, remaining, .05)
        time.sleep(delay)

# Makes the current subprocess accessible to the abort() function
# This is a list of tuples of the subprocess.Popen or
# multiprocessing.Process object and args.
_currentSubprocesses = []

def _addSubprocess(p, args):
    entry = (p, args)
    _currentSubprocesses.append(entry)
    return entry

def _removeSubprocess(entry):
    if entry and entry in _currentSubprocesses:
        try:
            _currentSubprocesses.remove(entry)
        except:
            pass

def waitOn(p):
    if get_os() == 'windows':
        # on windows use a poll loop, otherwise signal does not get handled
        retcode = None
        while retcode == None:
            retcode = p.poll()
            time.sleep(0.05)
    else:
        retcode = p.wait()
    return retcode

def run(args, nonZeroIsFatal=True, out=None, err=None, cwd=None, timeout=None, env=None):
    """
    Run a command in a subprocess, wait for it to complete and return the exit status of the process.
    If the exit status is non-zero and `nonZeroIsFatal` is true, then mx is exited with
    the same exit status.
    Each line of the standard output and error streams of the subprocess are redirected to
    out and err if they are callable objects.
    """

    assert isinstance(args, types.ListType), "'args' must be a list: " + str(args)
    for arg in args:
        assert isinstance(arg, types.StringTypes), 'argument is not a string: ' + str(arg)

    if env is None:
        env = os.environ

    if _opts.verbose:
        if _opts.very_verbose:
            log('Environment variables:')
            for key in sorted(env.keys()):
                log('    ' + key + '=' + env[key])
        log(' '.join(map(pipes.quote, args)))

    if timeout is None and _opts.ptimeout != 0:
        timeout = _opts.ptimeout

    sub = None
    try:
        # On Unix, the new subprocess should be in a separate group so that a timeout alarm
        # can use os.killpg() to kill the whole subprocess group
        preexec_fn = None
        creationflags = 0
        if get_os() == 'windows':
            creationflags = subprocess.CREATE_NEW_PROCESS_GROUP
        else:
            preexec_fn = os.setsid

        def redirect(stream, f):
            for line in iter(stream.readline, ''):
                f(line)
            stream.close()
        stdout = out if not callable(out) else subprocess.PIPE
        stderr = err if not callable(err) else subprocess.PIPE
        p = subprocess.Popen(args, cwd=cwd, stdout=stdout, stderr=stderr, preexec_fn=preexec_fn, creationflags=creationflags, env=env)
        sub = _addSubprocess(p, args)
        joiners = []
        if callable(out):
            t = Thread(target=redirect, args=(p.stdout, out))
            # Don't make the reader thread a daemon otherwise output can be droppped
            t.start()
            joiners.append(t)
        if callable(err):
            t = Thread(target=redirect, args=(p.stderr, err))
            # Don't make the reader thread a daemon otherwise output can be droppped
            t.start()
            joiners.append(t)
        while any([t.is_alive() for t in joiners]):
            # Need to use timeout otherwise all signals (including CTRL-C) are blocked
            # see: http://bugs.python.org/issue1167930
            for t in joiners:
                t.join(10)
        if timeout is None or timeout == 0:
            retcode = waitOn(p)
        else:
            if get_os() == 'windows':
                abort('Use of timeout not (yet) supported on Windows')
            retcode = _waitWithTimeout(p, args, timeout)
    except OSError as e:
        log('Error executing \'' + ' '.join(args) + '\': ' + str(e))
        if _opts.verbose:
            raise e
        abort(e.errno)
    except KeyboardInterrupt:
        abort(1)
    finally:
        _removeSubprocess(sub)

    if retcode and nonZeroIsFatal:
        if _opts.verbose:
            if _opts.very_verbose:
                raise subprocess.CalledProcessError(retcode, ' '.join(args))
            else:
                log('[exit code: ' + str(retcode) + ']')
        abort(retcode)

    return retcode

def exe_suffix(name):
    """
    Gets the platform specific suffix for an executable
    """
    if get_os() == 'windows':
        return name + '.exe'
    return name

def add_lib_prefix(name):
    """
    Adds the platform specific library prefix to a name
    """
    os = get_os()
    if os == 'linux' or os == 'solaris' or os == 'darwin':
        return 'lib' + name
    return name

def add_lib_suffix(name):
    """
    Adds the platform specific library suffix to a name
    """
    os = get_os()
    if os == 'windows':
        return name + '.dll'
    if os == 'linux' or os == 'solaris':
        return name + '.so'
    if os == 'darwin':
        return name + '.dylib'
    return name

"""
Utility for filtering duplicate lines.
"""
class DuplicateSuppressingStream:
    """
    Creates an object that will suppress duplicate lines sent to 'out'.
    The lines considered for suppression are those that contain one of the
    strings in 'restrictTo' if it is not None.
    """
    def __init__(self, restrictTo=None, out=sys.stdout):
        self.restrictTo = restrictTo
        self.seen = set()
        self.out = out
        self.currentFilteredLineCount = 0
        self.currentFilteredTime = None

    def isSuppressionCandidate(self, line):
        if self.restrictTo:
            for p in self.restrictTo:
                if p in line:
                    return True
            return False
        else:
            return True

    def write(self, line):
        if self.isSuppressionCandidate(line):
            if line in self.seen:
                self.currentFilteredLineCount += 1
                if self.currentFilteredTime:
                    if time.time() - self.currentFilteredTime > 1 * 60:
                        self.out.write("  Filtered " + str(self.currentFilteredLineCount) + " repeated lines...\n")
                        self.currentFilteredTime = time.time()
                else:
                    self.currentFilteredTime = time.time()
                return
            self.seen.add(line)
        self.currentFilteredLineCount = 0
        self.out.write(line)
        self.currentFilteredTime = None

"""
A JavaCompliance simplifies comparing Java compliance values extracted from a JDK version string.
"""
class JavaCompliance:
    def __init__(self, ver):
        m = re.match(r'1\.(\d+).*', ver)
        assert m is not None, 'not a recognized version string: ' + ver
        self.value = int(m.group(1))

    def __str__(self):
        return '1.' + str(self.value)

    def __cmp__(self, other):
        if isinstance(other, types.StringType):
            other = JavaCompliance(other)

        return cmp(self.value, other.value)

    def __hash__(self):
        return self.value.__hash__()

"""
A version specification as defined in JSR-56
"""
class VersionSpec:
    def __init__(self, versionString):
        validChar = r'[\x21-\x25\x27-\x29\x2c\x2f-\x5e\x60-\x7f]'
        separator = r'[.\-_]'
        m = re.match("^" + validChar + '+(' + separator + validChar + '+)*$', versionString)
        assert m is not None, 'not a recognized version string: ' + versionString
        self.versionString = versionString
        self.parts = [int(f) if f.isdigit() else f for f in re.split(separator, versionString)]

    def __str__(self):
        return self.versionString

    def __cmp__(self, other):
        return cmp(self.parts, other.parts)

def _filter_non_existant_paths(paths):
    return os.pathsep.join([path for path in paths.split(os.pathsep) if exists(path)])

"""
A JavaConfig object encapsulates info on how Java commands are run.
"""
class JavaConfig:
    def __init__(self, java_home, java_dbg_port):
        self.jdk = java_home
        self.debug_port = java_dbg_port
        self.jar = exe_suffix(join(self.jdk, 'bin', 'jar'))
        self.java = exe_suffix(join(self.jdk, 'bin', 'java'))
        self.javac = exe_suffix(join(self.jdk, 'bin', 'javac'))
        self.javap = exe_suffix(join(self.jdk, 'bin', 'javap'))
        self.javadoc = exe_suffix(join(self.jdk, 'bin', 'javadoc'))
        self.pack200 = exe_suffix(join(self.jdk, 'bin', 'pack200'))
        self.toolsjar = join(self.jdk, 'lib', 'tools.jar')
        self._bootclasspath = None
        self._extdirs = None
        self._endorseddirs = None

        if not exists(self.java):
            abort('Java launcher does not exist: ' + self.java)

        def delAtAndSplit(s):
            return shlex.split(s.lstrip('@'))

        self.java_args = delAtAndSplit(_opts.java_args) if _opts.java_args else []
        self.java_args_pfx = sum(map(delAtAndSplit, _opts.java_args_pfx), [])
        self.java_args_sfx = sum(map(delAtAndSplit, _opts.java_args_sfx), [])

        # Prepend the -d64 VM option only if the java command supports it
        try:
            output = subprocess.check_output([self.java, '-d64', '-version'], stderr=subprocess.STDOUT)
            self.java_args = ['-d64'] + self.java_args
        except subprocess.CalledProcessError as e:
            try:
                output = subprocess.check_output([self.java, '-version'], stderr=subprocess.STDOUT)
            except subprocess.CalledProcessError as e:
                print e.output
                abort(e.returncode)

        def _checkOutput(out):
            return 'version' in out

        # hotspot can print a warning, e.g. if there's a .hotspot_compiler file in the cwd
        output = output.split('\n')
        version = None
        for o in output:
            if _checkOutput(o):
                assert version is None
                version = o

        self.version = VersionSpec(version.split()[2].strip('"'))
        self.javaCompliance = JavaCompliance(self.version.versionString)

        if self.debug_port is not None:
            self.java_args += ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=' + str(self.debug_port)]

    def _init_classpaths(self):
        myDir = dirname(__file__)
        outDir = join(dirname(__file__), '.jdk' + str(self.version))
        if not exists(outDir):
            os.makedirs(outDir)
        javaSource = join(myDir, 'ClasspathDump.java')
        if not exists(join(outDir, 'ClasspathDump.class')):
            subprocess.check_call([self.javac, '-d', outDir, javaSource], stderr=subprocess.PIPE, stdout=subprocess.PIPE)
        self._bootclasspath, self._extdirs, self._endorseddirs = [x if x != 'null' else None for x in subprocess.check_output([self.java, '-cp', outDir, 'ClasspathDump'], stderr=subprocess.PIPE).split('|')]
        if not self._bootclasspath or not self._extdirs or not self._endorseddirs:
            warn("Could not find all classpaths: boot='" + str(self._bootclasspath) + "' extdirs='" + str(self._extdirs) + "' endorseddirs='" + str(self._endorseddirs) + "'")
        self._bootclasspath = _filter_non_existant_paths(self._bootclasspath)
        self._extdirs = _filter_non_existant_paths(self._extdirs)
        self._endorseddirs = _filter_non_existant_paths(self._endorseddirs)

    def __hash__(self):
        return hash(self.jdk)

    def __cmp__(self, other):
        if isinstance(other, JavaConfig):
            return cmp(self.javaCompliance, other.javaCompliance)
        raise TypeError()

    def format_cmd(self, args, addDefaultArgs):
        if addDefaultArgs:
            return [self.java] + self.processArgs(args)
        else:
            return [self.java] + args

    def processArgs(self, args):
        return self.java_args_pfx + self.java_args + self.java_args_sfx + args

    def bootclasspath(self):
        if self._bootclasspath is None:
            self._init_classpaths()
        return self._bootclasspath

    def extdirs(self):
        if self._extdirs is None:
            self._init_classpaths()
        return self._extdirs

    def endorseddirs(self):
        if self._endorseddirs is None:
            self._init_classpaths()
        return self._endorseddirs

def check_get_env(key):
    """
    Gets an environment variable, aborting with a useful message if it is not set.
    """
    value = get_env(key)
    if value is None:
        abort('Required environment variable ' + key + ' must be set')
    return value

def get_env(key, default=None):
    """
    Gets an environment variable.
    """
    value = os.environ.get(key, default)
    return value

def logv(msg=None):
    if _opts.verbose:
        log(msg)

def log(msg=None):
    """
    Write a message to the console.
    All script output goes through this method thus allowing a subclass
    to redirect it.
    """
    if msg is None:
        print
    else:
        print msg

def expand_project_in_class_path_arg(cpArg):
    cp = []
    for part in cpArg.split(os.pathsep):
        if part.startswith('@'):
            cp += classpath(part[1:]).split(os.pathsep)
        else:
            cp.append(part)
    return os.pathsep.join(cp)

def expand_project_in_args(args):
    for i in range(len(args)):
        if args[i] == '-cp' or args[i] == '-classpath':
            if i + 1 < len(args):
                args[i + 1] = expand_project_in_class_path_arg(args[i + 1])
            return


def gmake_cmd():
    for a in ['make', 'gmake', 'gnumake']:
        try:
            output = subprocess.check_output([a, '--version'])
            if 'GNU' in output:
                return a
        except:
            pass
    abort('Could not find a GNU make executable on the current path.')

def expandvars_in_property(value):
    result = expandvars(value)
    if '$' in result or '%' in result:
        abort('Property contains an undefined environment variable: ' + value)
    return result

def _send_sigquit():
    for p, args in _currentSubprocesses:

        def _isJava():
            if args:
                name = args[0].split(os.sep)[-1]
                return name == "java"
            return False

        if p is not None and _isJava():
            if get_os() == 'windows':
                log("mx: implement me! want to send SIGQUIT to my child process")
            else:
                _kill_process_group(p.pid, sig=signal.SIGQUIT)
            time.sleep(0.1)

def abort(codeOrMessage):
    """
    Aborts the program with a SystemExit exception.
    If 'codeOrMessage' is a plain integer, it specifies the system exit status;
    if it is None, the exit status is zero; if it has another type (such as a string),
    the object's value is printed and the exit status is one.
    """

    if _opts.killwithsigquit:
        _send_sigquit()

    def is_alive(p):
        if isinstance(p, subprocess.Popen):
            return p.poll() is None
        assert isinstance(p, multiprocessing.Process), p
        return p.is_alive()

    for p, args in _currentSubprocesses:
        if is_alive(p):
            try:
                if get_os() == 'windows':
                    p.terminate()
                else:
                    _kill_process_group(p.pid, signal.SIGKILL)
            except BaseException as e:
                if is_alive(p):
                    log('error while killing subprocess {} "{}": {}'.format(p.pid, ' '.join(args), e))

    if _opts and _opts.verbose:
        import traceback
        traceback.print_stack()
    raise SystemExit(codeOrMessage)

def download(path, urls, verbose=False):
    """
    Attempts to downloads content for each URL in a list, stopping after the first successful download.
    If the content cannot be retrieved from any URL, the program is aborted. The downloaded content
    is written to the file indicated by 'path'.
    """
    d = dirname(path)
    if d != '' and not exists(d):
        os.makedirs(d)

    assert not path.endswith(os.sep)

    myDir = dirname(__file__)
    javaSource = join(myDir, 'URLConnectionDownload.java')
    javaClass = join(myDir, 'URLConnectionDownload.class')
    if not exists(javaClass) or getmtime(javaClass) < getmtime(javaSource):
        subprocess.check_call([java().javac, '-d', myDir, javaSource])
    verbose = []
    if sys.stderr.isatty():
        verbose.append("-v")
    if run([java().java, '-cp', myDir, 'URLConnectionDownload', path] + verbose + urls, nonZeroIsFatal=False) == 0:
        return

    abort('Could not download to ' + path + ' from any of the following URLs:\n\n    ' +
              '\n    '.join(urls) + '\n\nPlease use a web browser to do the download manually')

def update_file(path, content):
    """
    Updates a file with some given content if the content differs from what's in
    the file already. The return value indicates if the file was updated.
    """
    existed = exists(path)
    try:
        old = None
        if existed:
            with open(path, 'rb') as f:
                old = f.read()

        if old == content:
            return False

        with open(path, 'wb') as f:
            f.write(content)

        log(('modified ' if existed else 'created ') + path)
        return True
    except IOError as e:
        abort('Error while writing to ' + path + ': ' + str(e))

# Builtin commands

def _defaultEcjPath():
    return get_env('JDT', join(_primary_suite.mxDir, 'ecj.jar'))

class JavaCompileTask:
    def __init__(self, args, proj, reason, javafilelist, jdk, outputDir, jdtJar, deps):
        self.proj = proj
        self.reason = reason
        self.javafilelist = javafilelist
        self.deps = deps
        self.jdk = jdk
        self.outputDir = outputDir
        self.done = False
        self.jdtJar = jdtJar
        self.args = args

    def __str__(self):
        return self.proj.name

    def logCompilation(self, compiler):
        log('Compiling Java sources for {} with {}... [{}]'.format(self.proj.name, compiler, self.reason))

    def execute(self):
        argfileName = join(self.proj.dir, 'javafilelist.txt')
        argfile = open(argfileName, 'wb')
        argfile.write('\n'.join(self.javafilelist))
        argfile.close()

        processorArgs = []

        processorPath = self.proj.annotation_processors_path()
        if processorPath:
            genDir = self.proj.source_gen_dir()
            if exists(genDir):
                shutil.rmtree(genDir)
            os.mkdir(genDir)
            processorArgs += ['-processorpath', join(processorPath), '-s', genDir]
        else:
            processorArgs += ['-proc:none']

        args = self.args
        jdk = self.jdk
        outputDir = self.outputDir
        compliance = str(jdk.javaCompliance)
        cp = classpath(self.proj.name, includeSelf=True)
        toBeDeleted = [argfileName]

        try:
            if not self.jdtJar:
                mainJava = java()
                if not args.error_prone:
                    javac = args.alt_javac if args.alt_javac else mainJava.javac
                    self.logCompilation('javac' if not args.alt_javac else args.alt_javac)
                    javacCmd = [javac, '-g', '-J-Xmx1g', '-source', compliance, '-target', compliance, '-classpath', cp, '-d', outputDir, '-bootclasspath', jdk.bootclasspath(), '-endorseddirs', jdk.endorseddirs(), '-extdirs', jdk.extdirs()]
                    if jdk.debug_port is not None:
                        javacCmd += ['-J-Xdebug', '-J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=' + str(jdk.debug_port)]
                    javacCmd += processorArgs
                    javacCmd += ['@' + argfile.name]

                    if not args.warnAPI:
                        javacCmd.append('-XDignore.symbol.file')
                    run(javacCmd)
                else:
                    self.logCompilation('javac (with error-prone)')
                    javaArgs = ['-Xmx1g']
                    javacArgs = ['-g', '-source', compliance, '-target', compliance, '-classpath', cp, '-d', outputDir, '-bootclasspath', jdk.bootclasspath(), '-endorseddirs', jdk.endorseddirs(), '-extdirs', jdk.extdirs()]
                    javacArgs += processorArgs
                    javacArgs += ['@' + argfile.name]
                    if not args.warnAPI:
                        javacArgs.append('-XDignore.symbol.file')
                    run_java(javaArgs + ['-cp', os.pathsep.join([mainJava.toolsjar, args.error_prone]), 'com.google.errorprone.ErrorProneCompiler'] + javacArgs)
            else:
                self.logCompilation('JDT')

                jdtVmArgs = ['-Xmx1g', '-jar', self.jdtJar]

                jdtArgs = ['-' + compliance,
                         '-cp', cp, '-g', '-enableJavadoc',
                         '-d', outputDir,
                         '-bootclasspath', jdk.bootclasspath(),
                         '-endorseddirs', jdk.endorseddirs(),
                         '-extdirs', jdk.extdirs()]
                jdtArgs += processorArgs

                jdtProperties = join(self.proj.dir, '.settings', 'org.eclipse.jdt.core.prefs')
                rootJdtProperties = join(self.proj.suite.mxDir, 'eclipse-settings', 'org.eclipse.jdt.core.prefs')
                if not exists(jdtProperties) or os.path.getmtime(jdtProperties) < os.path.getmtime(rootJdtProperties):
                    # Try to fix a missing properties file by running eclipseinit
                    _eclipseinit_project(self.proj)
                if not exists(jdtProperties):
                    log('JDT properties file {0} not found'.format(jdtProperties))
                else:
                    with open(jdtProperties) as fp:
                        origContent = fp.read()
                        content = origContent
                        if args.jdt_warning_as_error:
                            content = content.replace('=warning', '=error')
                        if not args.jdt_show_task_tags:
                            content = content + '\norg.eclipse.jdt.core.compiler.problem.tasks=ignore'
                    if origContent != content:
                        jdtPropertiesTmp = jdtProperties + '.tmp'
                        with open(jdtPropertiesTmp, 'w') as fp:
                            fp.write(content)
                        toBeDeleted.append(jdtPropertiesTmp)
                        jdtArgs += ['-properties', jdtPropertiesTmp]
                    else:
                        jdtArgs += ['-properties', jdtProperties]
                jdtArgs.append('@' + argfile.name)

                run_java(jdtVmArgs + jdtArgs)

            # Create annotation processor jar for a project that defines annotation processors
            if self.proj.definedAnnotationProcessorsDist:
                self.proj.definedAnnotationProcessorsDist.make_archive()

        finally:
            for n in toBeDeleted:
                os.remove(n)
            self.done = True

def build(args, parser=None):
    """compile the Java and C sources, linking the latter

    Compile all the Java source code using the appropriate compilers
    and linkers for the various source code types."""

    suppliedParser = parser is not None
    if not suppliedParser:
        parser = ArgumentParser(prog='mx build')

    parser = parser if parser is not None else ArgumentParser(prog='mx build')
    parser.add_argument('-f', action='store_true', dest='force', help='force build (disables timestamp checking)')
    parser.add_argument('-c', action='store_true', dest='clean', help='removes existing build output')
    parser.add_argument('-p', action='store_true', dest='parallelize', help='parallelizes Java compilation')
    parser.add_argument('--source', dest='compliance', help='Java compliance level for projects without an explicit one')
    parser.add_argument('--Wapi', action='store_true', dest='warnAPI', help='show warnings about using internal APIs')
    parser.add_argument('--projects', action='store', help='comma separated projects to build (omit to build all projects)')
    parser.add_argument('--only', action='store', help='comma separated projects to build, without checking their dependencies (omit to build all projects)')
    parser.add_argument('--no-java', action='store_false', dest='java', help='do not build Java projects')
    parser.add_argument('--no-native', action='store_false', dest='native', help='do not build native projects')
    parser.add_argument('--jdt-warning-as-error', action='store_true', help='convert all Eclipse batch compiler warnings to errors')
    parser.add_argument('--jdt-show-task-tags', action='store_true', help='show task tags as Eclipse batch compiler warnings')
    parser.add_argument('--alt-javac', dest='alt_javac', help='path to alternative javac executable', metavar='<path>')
    compilerSelect = parser.add_mutually_exclusive_group()
    compilerSelect.add_argument('--error-prone', dest='error_prone', help='path to error-prone.jar', metavar='<path>')
    compilerSelect.add_argument('--jdt', help='path to ecj.jar, the Eclipse batch compiler', default=_defaultEcjPath(), metavar='<path>')
    compilerSelect.add_argument('--force-javac', action='store_true', dest='javac', help='use javac whether ecj.jar is found or not')

    if suppliedParser:
        parser.add_argument('remainder', nargs=REMAINDER, metavar='...')

    args = parser.parse_args(args)

    jdtJar = None
    if not args.javac and args.jdt is not None:
        if not args.jdt.endswith('.jar'):
            abort('Path for Eclipse batch compiler does not look like a jar file: ' + args.jdt)
        jdtJar = args.jdt
        if not exists(jdtJar):
            if os.path.abspath(jdtJar) == os.path.abspath(_defaultEcjPath()) and get_env('JDT', None) is None:
                # Silently ignore JDT if default location is used but does not exist
                jdtJar = None
            else:
                abort('Eclipse batch compiler jar does not exist: ' + args.jdt)

    if args.only is not None:
        # N.B. This build will not include dependencies including annotation processor dependencies
        sortedProjects = [project(name) for name in args.only.split(',')]
    else:
        if args.projects is not None:
            projectNames = args.projects.split(',')
        else:
            projectNames = None

        projects = _projects_opt_limit_to_suites(projects_from_names(projectNames))
        # N.B. Limiting to a suite only affects the starting set of projects. Dependencies in other suites will still be compiled
        sortedProjects = sorted_project_deps(projects, includeAnnotationProcessors=True)

    if args.java:
        ideinit([], refreshOnly=True, buildProcessorJars=False)

    def prepareOutputDirs(p, clean):
        outputDir = p.output_dir()
        if exists(outputDir):
            if clean:
                log('Cleaning {0}...'.format(outputDir))
                shutil.rmtree(outputDir)
                os.mkdir(outputDir)
        else:
            os.mkdir(outputDir)
        genDir = p.source_gen_dir()
        if genDir != '' and exists(genDir) and clean:
            log('Cleaning {0}...'.format(genDir))
            for f in os.listdir(genDir):
                shutil.rmtree(join(genDir, f))
        return outputDir

    tasks = {}
    updatedAnnotationProcessorDists = set()
    for p in sortedProjects:
        if p.native:
            if args.native:
                log('Calling GNU make {0}...'.format(p.dir))

                if args.clean:
                    run([gmake_cmd(), 'clean'], cwd=p.dir)

                run([gmake_cmd()], cwd=p.dir)
            continue
        else:
            if not args.java:
                continue
            if exists(join(p.dir, 'plugin.xml')):  # eclipse plugin project
                continue

        # skip building this Java project if its Java compliance level is "higher" than the configured JDK
        requiredCompliance = p.javaCompliance if p.javaCompliance else JavaCompliance(args.compliance) if args.compliance else None
        jdk = java(requiredCompliance)
        assert jdk

        outputDir = prepareOutputDirs(p, args.clean)

        sourceDirs = p.source_dirs()
        buildReason = 'forced build' if args.force else None
        taskDeps = []
        for dep in p.all_deps([], includeLibs=False, includeAnnotationProcessors=True):
            taskDep = tasks.get(dep.name)
            if taskDep:
                if not buildReason:
                    buildReason = dep.name + ' rebuilt'
                taskDeps.append(taskDep)

        jasminAvailable = None
        javafilelist = []
        for sourceDir in sourceDirs:
            for root, _, files in os.walk(sourceDir):
                javafiles = [join(root, name) for name in files if name.endswith('.java') and name != 'package-info.java']
                javafilelist += javafiles

                # Copy all non Java resources or assemble Jasmin files
                nonjavafilelist = [join(root, name) for name in files if not name.endswith('.java')]
                for src in nonjavafilelist:
                    if src.endswith('.jasm'):
                        className = None
                        with open(src) as f:
                            for line in f:
                                if line.startswith('.class '):
                                    className = line.split()[-1]
                                    break

                        if className is not None:
                            jasminOutputDir = p.jasmin_output_dir()
                            classFile = join(jasminOutputDir, className.replace('/', os.sep) + '.class')
                            if exists(dirname(classFile)) and (not exists(classFile) or os.path.getmtime(classFile) < os.path.getmtime(src)):
                                if jasminAvailable is None:
                                    try:
                                        with open(os.devnull) as devnull:
                                            subprocess.call('jasmin', stdout=devnull, stderr=subprocess.STDOUT)
                                        jasminAvailable = True
                                    except OSError:
                                        jasminAvailable = False

                                if jasminAvailable:
                                    log('Assembling Jasmin file ' + src)
                                    run(['jasmin', '-d', jasminOutputDir, src])
                                else:
                                    log('The jasmin executable could not be found - skipping ' + src)
                                    with file(classFile, 'a'):
                                        os.utime(classFile, None)

                        else:
                            log('could not file .class directive in Jasmin source: ' + src)
                    else:
                        dst = join(outputDir, src[len(sourceDir) + 1:])
                        if not exists(dirname(dst)):
                            os.makedirs(dirname(dst))
                        if exists(dirname(dst)) and (not exists(dst) or os.path.getmtime(dst) < os.path.getmtime(src)):
                            shutil.copyfile(src, dst)

                if not buildReason:
                    for javafile in javafiles:
                        classfile = TimeStampFile(outputDir + javafile[len(sourceDir):-len('java')] + 'class')
                        if not classfile.exists() or classfile.isOlderThan(javafile):
                            buildReason = 'class file(s) out of date'
                            break

        apsOutOfDate = p.update_current_annotation_processors_file()
        if apsOutOfDate:
            buildReason = 'annotation processor(s) changed'

        if not buildReason:
            logv('[all class files for {0} are up to date - skipping]'.format(p.name))
            continue

        if len(javafilelist) == 0:
            logv('[no Java sources for {0} - skipping]'.format(p.name))
            continue

        task = JavaCompileTask(args, p, buildReason, javafilelist, jdk, outputDir, jdtJar, taskDeps)
        if p.definedAnnotationProcessorsDist:
            updatedAnnotationProcessorDists.add(p.definedAnnotationProcessorsDist)

        if args.parallelize:
            # Best to initialize class paths on main process
            jdk.bootclasspath()
            task.proc = None
            tasks[p.name] = task
        else:
            task.execute()

    if args.parallelize:

        def joinTasks(tasks):
            failed = []
            for t in tasks:
                t.proc.join()
                _removeSubprocess(t.sub)
                if t.proc.exitcode != 0:
                    failed.append(t)
            return failed

        def checkTasks(tasks):
            active = []
            for t in tasks:
                if t.proc.is_alive():
                    active.append(t)
                else:
                    if t.proc.exitcode != 0:
                        return ([], joinTasks(tasks))
            return (active, [])

        def remainingDepsDepth(task):
            if task._d is None:
                incompleteDeps = [d for d in task.deps if d.proc is None or d.proc.is_alive()]
                if len(incompleteDeps) == 0:
                    task._d = 0
                else:
                    task._d = max([remainingDepsDepth(t) for t in incompleteDeps]) + 1
            return task._d

        def compareTasks(t1, t2):
            d = remainingDepsDepth(t1) - remainingDepsDepth(t2)
            if d == 0:
                t1Work = (1 + len(t1.proj.annotation_processors())) * len(t1.javafilelist)
                t2Work = (1 + len(t2.proj.annotation_processors())) * len(t2.javafilelist)
                d = t1Work - t2Work
            return d

        def sortWorklist(tasks):
            for t in tasks:
                t._d = None
            return sorted(tasks, compareTasks)

        cpus = multiprocessing.cpu_count()
        worklist = sortWorklist(tasks.values())
        active = []
        failed = []
        while len(worklist) != 0:
            while True:
                active, failed = checkTasks(active)
                if len(failed) != 0:
                    assert not active, active
                    break
                if len(active) == cpus:
                    # Sleep for 1 second
                    time.sleep(1)
                else:
                    break

            if len(failed) != 0:
                break

            def executeTask(task):
                # Clear sub-process list cloned from parent process
                del _currentSubprocesses[:]
                task.execute()

            def depsDone(task):
                for d in task.deps:
                    if d.proc is None or d.proc.exitcode is None:
                        return False
                return True

            for task in worklist:
                if depsDone(task):
                    worklist.remove(task)
                    task.proc = multiprocessing.Process(target=executeTask, args=(task,))
                    task.proc.start()
                    active.append(task)
                    task.sub = _addSubprocess(task.proc, ['JavaCompileTask', str(task)])
                if len(active) == cpus:
                    break

            worklist = sortWorklist(worklist)

        failed += joinTasks(active)
        if len(failed):
            for t in failed:
                log('Compiling {} failed'.format(t.proj.name))
            abort('{} Java compilation tasks failed'.format(len(failed)))

    if args.java:
        for dist in sorted_dists():
            if dist not in updatedAnnotationProcessorDists:
                archive(['@' + dist.name])

    if suppliedParser:
        return args
    return None

def _chunk_files_for_command_line(files, limit=None, pathFunction=None):
    """
    Returns a generator for splitting up a list of files into chunks such that the
    size of the space separated file paths in a chunk is less than a given limit.
    This is used to work around system command line length limits.
    """
    chunkSize = 0
    chunkStart = 0
    if limit is None:
        commandLinePrefixAllowance = 3000
        if get_os() == 'windows':
            # The CreateProcess function on Windows limits the length of a command line to
            # 32,768 characters (http://msdn.microsoft.com/en-us/library/ms682425%28VS.85%29.aspx)
            limit = 32768 - commandLinePrefixAllowance
        else:
            # Using just SC_ARG_MAX without extra downwards adjustment
            # results in "[Errno 7] Argument list too long" on MacOS.
            syslimit = os.sysconf('SC_ARG_MAX') - 20000
            limit = syslimit - commandLinePrefixAllowance
    for i in range(len(files)):
        path = files[i] if pathFunction is None else pathFunction(files[i])
        size = len(path) + 1
        if chunkSize + size < limit:
            chunkSize += size
        else:
            assert i > chunkStart
            yield files[chunkStart:i]
            chunkStart = i
            chunkSize = 0
    if chunkStart == 0:
        assert chunkSize < limit
        yield files

def eclipseformat(args):
    """run the Eclipse Code Formatter on the Java sources

    The exit code 1 denotes that at least one file was modified."""

    parser = ArgumentParser(prog='mx eclipseformat')
    parser.add_argument('-e', '--eclipse-exe', help='location of the Eclipse executable')
    parser.add_argument('-C', '--no-backup', action='store_false', dest='backup', help='do not save backup of modified files')
    parser.add_argument('--projects', action='store', help='comma separated projects to process (omit to process all projects)')

    args = parser.parse_args(args)
    if args.eclipse_exe is None:
        args.eclipse_exe = os.environ.get('ECLIPSE_EXE')
    if args.eclipse_exe is None:
        abort('Could not find Eclipse executable. Use -e option or ensure ECLIPSE_EXE environment variable is set.')

    # Maybe an Eclipse installation dir was specified - look for the executable in it
    if isdir(args.eclipse_exe):
        args.eclipse_exe = join(args.eclipse_exe, exe_suffix('eclipse'))
        warn("The eclipse-exe was a directory, now using " + args.eclipse_exe)

    if not os.path.isfile(args.eclipse_exe):
        abort('File does not exist: ' + args.eclipse_exe)
    if not os.access(args.eclipse_exe, os.X_OK):
        abort('Not an executable file: ' + args.eclipse_exe)

    eclipseinit([], buildProcessorJars=False)

    # build list of projects to be processed
    projects = sorted_deps()
    if args.projects is not None:
        projects = [project(name) for name in args.projects.split(',')]

    class Batch:
        def __init__(self, settingsDir, javaCompliance):
            self.path = join(settingsDir, 'org.eclipse.jdt.core.prefs')
            self.javaCompliance = javaCompliance
            self.javafiles = list()
            with open(join(settingsDir, 'org.eclipse.jdt.ui.prefs')) as fp:
                jdtUiPrefs = fp.read()
            self.removeTrailingWhitespace = 'sp_cleanup.remove_trailing_whitespaces_all=true' in jdtUiPrefs
            if self.removeTrailingWhitespace:
                assert 'sp_cleanup.remove_trailing_whitespaces=true' in jdtUiPrefs and 'sp_cleanup.remove_trailing_whitespaces_ignore_empty=false' in jdtUiPrefs

        def settings(self):
            with open(self.path) as fp:
                return fp.read() + java(self.javaCompliance).java + str(self.removeTrailingWhitespace)

    class FileInfo:
        def __init__(self, path):
            self.path = path
            with open(path) as fp:
                self.content = fp.read()
            self.times = (os.path.getatime(path), os.path.getmtime(path))

        def update(self, removeTrailingWhitespace):
            with open(self.path) as fp:
                content = fp.read()

            if self.content != content:
                # Only apply *after* formatting to match the order in which the IDE does it
                if removeTrailingWhitespace:
                    content, n = re.subn(r'[ \t]+$', '', content, flags=re.MULTILINE)
                    if n != 0 and self.content == content:
                        # undo on-disk changes made by the Eclipse formatter
                        with open(self.path, 'w') as fp:
                            fp.write(content)

                if self.content != content:
                    self.diff = difflib.unified_diff(self.content.splitlines(1), content.splitlines(1))
                    self.content = content
                    return True

            # reset access and modification time of file
            os.utime(self.path, self.times)

    modified = list()
    batches = dict()  # all sources with the same formatting settings are formatted together
    for p in projects:
        if p.native:
            continue
        sourceDirs = p.source_dirs()

        batch = Batch(join(p.dir, '.settings'), p.javaCompliance)

        if not exists(batch.path):
            if _opts.verbose:
                log('[no Eclipse Code Formatter preferences at {0} - skipping]'.format(batch.path))
            continue

        for sourceDir in sourceDirs:
            for root, _, files in os.walk(sourceDir):
                for f in [join(root, name) for name in files if name.endswith('.java')]:
                    batch.javafiles.append(FileInfo(f))
        if len(batch.javafiles) == 0:
            logv('[no Java sources in {0} - skipping]'.format(p.name))
            continue

        res = batches.setdefault(batch.settings(), batch)
        if res is not batch:
            res.javafiles = res.javafiles + batch.javafiles

    log("we have: " + str(len(batches)) + " batches")
    for batch in batches.itervalues():
        for chunk in _chunk_files_for_command_line(batch.javafiles, pathFunction=lambda f: f.path):
            run([args.eclipse_exe,
                '-nosplash',
                '-application',
                'org.eclipse.jdt.core.JavaCodeFormatter',
                '-vm', java(batch.javaCompliance).java,
                '-config', batch.path]
                + [f.path for f in chunk])
            for fi in chunk:
                if fi.update(batch.removeTrailingWhitespace):
                    modified.append(fi)

    log('{0} files were modified'.format(len(modified)))

    if len(modified) != 0:
        arcbase = _primary_suite.dir
        if args.backup:
            backup = os.path.abspath('eclipseformat.backup.zip')
            zf = zipfile.ZipFile(backup, 'w', zipfile.ZIP_DEFLATED)
        for fi in modified:
            name = os.path.relpath(fi.path, arcbase)
            log(' - {0}'.format(name))
            log('Changes:')
            log(''.join(fi.diff))
            if args.backup:
                arcname = name.replace(os.sep, '/')
                zf.writestr(arcname, fi.content)
        if args.backup:
            zf.close()
            log('Wrote backup of {0} modified files to {1}'.format(len(modified), backup))
        return 1
    return 0

def processorjars():
    for s in suites(True):
        _processorjars_suite(s)

def _processorjars_suite(s):
    projs = [p for p in s.projects if p.definedAnnotationProcessors is not None]
    if len(projs) <= 0:
        return []

    pnames = [p.name for p in projs]
    build(['--jdt-warning-as-error', '--projects', ",".join(pnames)])
    return [p.definedAnnotationProcessorsDist.path for p in s.projects if p.definedAnnotationProcessorsDist is not None]

def pylint(args):
    """run pylint (if available) over Python source files (found by 'hg locate' or by tree walk with -walk)"""

    parser = ArgumentParser(prog='mx pylint')
    parser.add_argument('--walk', action='store_true', help='use tree walk find .py files')
    args = parser.parse_args(args)

    rcfile = join(dirname(__file__), '.pylintrc')
    if not exists(rcfile):
        log('pylint configuration file does not exist: ' + rcfile)
        return

    try:
        output = subprocess.check_output(['pylint', '--version'], stderr=subprocess.STDOUT)
        m = re.match(r'.*pylint (\d+)\.(\d+)\.(\d+).*', output, re.DOTALL)
        if not m:
            log('could not determine pylint version from ' + output)
            return
        major, minor, micro = (int(m.group(1)), int(m.group(2)), int(m.group(3)))
        if major < 1:
            log('require pylint version >= 1 (got {0}.{1}.{2})'.format(major, minor, micro))
            return
    except BaseException:
        log('pylint is not available')
        return

    def findfiles_by_walk():
        result = []
        for suite in suites(True):
            for root, dirs, files in os.walk(suite.dir):
                for f in files:
                    if f.endswith('.py'):
                        pyfile = join(root, f)
                        result.append(pyfile)
                if 'bin' in dirs:
                    dirs.remove('bin')
                if 'lib' in dirs:
                    # avoids downloaded .py files
                    dirs.remove('lib')
        return result

    def findfiles_by_hg():
        result = []
        for suite in suites(True):
            versioned = subprocess.check_output(['hg', 'locate', '-f'], stderr=subprocess.STDOUT, cwd=suite.dir).split(os.linesep)
            for f in versioned:
                if f.endswith('.py') and exists(f):
                    result.append(f)
        return result

    # Perhaps we should just look in suite.mxDir directories for .py files?
    if args.walk:
        pyfiles = findfiles_by_walk()
    else:
        pyfiles = findfiles_by_hg()

    env = os.environ.copy()

    pythonpath = dirname(__file__)
    for suite in suites(True):
        pythonpath = os.pathsep.join([pythonpath, suite.mxDir])

    env['PYTHONPATH'] = pythonpath

    for pyfile in pyfiles:
        log('Running pylint on ' + pyfile + '...')
        run(['pylint', '--reports=n', '--rcfile=' + rcfile, pyfile], env=env)

"""
Utility for creating and updating a zip file atomically.
"""
class Archiver:
    def __init__(self, path):
        self.path = path

    def __enter__(self):
        if self.path:
            if not isdir(dirname(self.path)):
                os.makedirs(dirname(self.path))
            fd, tmp = tempfile.mkstemp(suffix='', prefix=basename(self.path) + '.', dir=dirname(self.path))
            self.tmpFd = fd
            self.tmpPath = tmp
            self.zf = zipfile.ZipFile(tmp, 'w')
        else:
            self.tmpFd = None
            self.tmpPath = None
            self.zf = None
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        if self.zf:
            self.zf.close()
            os.close(self.tmpFd)
            # Correct the permissions on the temporary file which is created with restrictive permissions
            os.chmod(self.tmpPath, 0o666 & ~currentUmask)
            # Atomic on Unix
            shutil.move(self.tmpPath, self.path)

def _archive(args):
    archive(args)
    return 0

def archive(args):
    """create jar files for projects and distributions"""
    parser = ArgumentParser(prog='mx archive')
    parser.add_argument('names', nargs=REMAINDER, metavar='[<project>|@<distribution>]...')
    args = parser.parse_args(args)

    archives = []
    for name in args.names:
        if name.startswith('@'):
            dname = name[1:]
            d = distribution(dname)
            d.make_archive()
            archives.append(d.path)
        else:
            p = project(name)
            archives.append(p.make_archive())

    logv("generated archives: " + str(archives))
    return archives

def canonicalizeprojects(args):
    """process all project files to canonicalize the dependencies

    The exit code of this command reflects how many files were updated."""

    changedFiles = 0
    for s in suites(True):
        projectsFile = join(s.mxDir, 'projects')
        if not exists(projectsFile):
            continue
        with open(projectsFile) as f:
            out = StringIO.StringIO()
            pattern = re.compile('project@([^@]+)@dependencies=.*')
            lineNo = 1
            for line in f:
                line = line.strip()
                m = pattern.match(line)
                p = project(m.group(1), fatalIfMissing=False) if m else None
                if m is None or p is None:
                    out.write(line + '\n')
                else:
                    for pkg in p.defined_java_packages():
                        if not pkg.startswith(p.name):
                            abort('package in {0} does not have prefix matching project name: {1}'.format(p, pkg))

                    ignoredDeps = set([name for name in p.deps if project(name, False) is not None])
                    for pkg in p.imported_java_packages():
                        for name in p.deps:
                            dep = project(name, False)
                            if dep is None:
                                ignoredDeps.discard(name)
                            else:
                                if pkg in dep.defined_java_packages():
                                    ignoredDeps.discard(name)
                                if pkg in dep.extended_java_packages():
                                    ignoredDeps.discard(name)
                    if len(ignoredDeps) != 0:
                        candidates = set()
                        # Compute dependencies based on projects required by p
                        for d in sorted_deps():
                            if not d.defined_java_packages().isdisjoint(p.imported_java_packages()):
                                candidates.add(d)
                        # Remove non-canonical candidates
                        for c in list(candidates):
                            candidates.difference_update(c.all_deps([], False, False))
                        candidates = [d.name for d in candidates]

                        abort('{0}:{1}: {2} does not use any packages defined in these projects: {3}\nComputed project dependencies: {4}'.format(
                            projectsFile, lineNo, p, ', '.join(ignoredDeps), ','.join(candidates)))

                    out.write('project@' + m.group(1) + '@dependencies=' + ','.join(p.canonical_deps()) + '\n')
                lineNo = lineNo + 1
            content = out.getvalue()
        if update_file(projectsFile, content):
            changedFiles += 1
    return changedFiles

class TimeStampFile:
    def __init__(self, path):
        self.path = path
        self.timestamp = os.path.getmtime(path) if exists(path) else None

    def isOlderThan(self, arg):
        if not self.timestamp:
            return True
        if isinstance(arg, TimeStampFile):
            if arg.timestamp is None:
                return False
            else:
                return arg.timestamp > self.timestamp
        elif isinstance(arg, types.ListType):
            files = arg
        else:
            files = [arg]
        for f in files:
            if os.path.getmtime(f) > self.timestamp:
                return True
        return False

    def exists(self):
        return exists(self.path)

    def touch(self):
        if exists(self.path):
            os.utime(self.path, None)
        else:
            if not isdir(dirname(self.path)):
                os.makedirs(dirname(self.path))
            file(self.path, 'a')

def checkstyle(args):
    """run Checkstyle on the Java sources

   Run Checkstyle over the Java sources. Any errors or warnings
   produced by Checkstyle result in a non-zero exit code."""

    parser = ArgumentParser(prog='mx checkstyle')

    parser.add_argument('-f', action='store_true', dest='force', help='force checking (disables timestamp checking)')
    args = parser.parse_args(args)

    totalErrors = 0
    for p in projects_opt_limit_to_suites():
        if p.native:
            continue
        sourceDirs = p.source_dirs()

        csConfig = join(p.dir, '.checkstyle_checks.xml')
        if not exists(csConfig):
            abort('ERROR: Checkstyle configuration for project {} is missing: {}'.format(p.name, csConfig))

        # skip checking this Java project if its Java compliance level is "higher" than the configured JDK
        jdk = java(p.javaCompliance)
        assert jdk

        for sourceDir in sourceDirs:
            javafilelist = []
            for root, _, files in os.walk(sourceDir):
                javafilelist += [join(root, name) for name in files if name.endswith('.java') and name != 'package-info.java']
            if len(javafilelist) == 0:
                logv('[no Java sources in {0} - skipping]'.format(sourceDir))
                continue

            timestamp = TimeStampFile(join(p.suite.mxDir, 'checkstyle-timestamps', sourceDir[len(p.suite.dir) + 1:].replace(os.sep, '_') + '.timestamp'))
            mustCheck = False
            if not args.force and timestamp.exists():
                mustCheck = timestamp.isOlderThan(javafilelist)
            else:
                mustCheck = True

            if not mustCheck:
                if _opts.verbose:
                    log('[all Java sources in {0} already checked - skipping]'.format(sourceDir))
                continue

            dotCheckstyleXML = xml.dom.minidom.parse(csConfig)
            localCheckConfig = dotCheckstyleXML.getElementsByTagName('local-check-config')[0]
            configLocation = localCheckConfig.getAttribute('location')
            configType = localCheckConfig.getAttribute('type')
            if configType == 'project':
                # Eclipse plugin "Project Relative Configuration" format:
                #
                #  '/<project_name>/<suffix>'
                #
                if configLocation.startswith('/'):
                    name, _, suffix = configLocation.lstrip('/').partition('/')
                    config = join(project(name).dir, suffix)
                else:
                    config = join(p.dir, configLocation)
            else:
                logv('[unknown Checkstyle configuration type "' + configType + '" in {0} - skipping]'.format(sourceDir))
                continue

            exclude = join(p.dir, '.checkstyle.exclude')

            if exists(exclude):
                with open(exclude) as f:
                    # Convert patterns to OS separators
                    patterns = [name.rstrip().replace('/', os.sep) for name in f.readlines()]
                def match(name):
                    for p in patterns:
                        if p in name:
                            if _opts.verbose:
                                log('excluding: ' + name)
                            return True
                    return False

                javafilelist = [name for name in javafilelist if not match(name)]

            auditfileName = join(p.dir, 'checkstyleOutput.txt')
            log('Running Checkstyle on {0} using {1}...'.format(sourceDir, config))

            try:
                for chunk in _chunk_files_for_command_line(javafilelist):
                    try:
                        run_java(['-Xmx1g', '-jar', library('CHECKSTYLE').get_path(True), '-f', 'xml', '-c', config, '-o', auditfileName] + chunk, nonZeroIsFatal=False)
                    finally:
                        if exists(auditfileName):
                            errors = []
                            source = [None]
                            def start_element(name, attrs):
                                if name == 'file':
                                    source[0] = attrs['name']
                                elif name == 'error':
                                    errors.append('{}:{}: {}'.format(source[0], attrs['line'], attrs['message']))

                            xp = xml.parsers.expat.ParserCreate()
                            xp.StartElementHandler = start_element
                            with open(auditfileName) as fp:
                                xp.ParseFile(fp)
                            if len(errors) != 0:
                                map(log, errors)
                                totalErrors = totalErrors + len(errors)
                            else:
                                timestamp.touch()
            finally:
                if exists(auditfileName):
                    os.unlink(auditfileName)
    return totalErrors

def clean(args, parser=None):
    """remove all class files, images, and executables

    Removes all files created by a build, including Java class files, executables, and
    generated images.
    """

    suppliedParser = parser is not None

    parser = parser if suppliedParser else ArgumentParser(prog='mx clean')
    parser.add_argument('--no-native', action='store_false', dest='native', help='do not clean native projects')
    parser.add_argument('--no-java', action='store_false', dest='java', help='do not clean Java projects')
    parser.add_argument('--no-dist', action='store_false', dest='dist', help='do not delete distributions')

    args = parser.parse_args(args)

    def _rmtree(dirPath):
        path = dirPath
        if get_os() == 'windows':
            path = unicode("\\\\?\\" + dirPath)
        shutil.rmtree(path)

    def _rmIfExists(name):
        if name and os.path.isfile(name):
            os.unlink(name)

    for p in projects_opt_limit_to_suites():
        if p.native:
            if args.native:
                run([gmake_cmd(), '-C', p.dir, 'clean'])
        else:
            if args.java:
                genDir = p.source_gen_dir()
                if genDir != '' and exists(genDir):
                    log('Clearing {0}...'.format(genDir))
                    for f in os.listdir(genDir):
                        _rmtree(join(genDir, f))


                outputDir = p.output_dir()
                if outputDir != '' and exists(outputDir):
                    log('Removing {0}...'.format(outputDir))
                    _rmtree(outputDir)

            for configName in ['netbeans-config.zip', 'eclipse-config.zip']:
                config = TimeStampFile(join(p.suite.mxDir, configName))
                if config.exists():
                    os.unlink(config.path)

    if args.dist:
        for d in _dists.keys():
            log('Removing distribution {0}...'.format(d))
            _rmIfExists(distribution(d).path)
            _rmIfExists(distribution(d).sourcesPath)

    if suppliedParser:
        return args

def about(args):
    """show the 'man page' for mx"""
    print __doc__

def help_(args):
    """show help for a given command

With no arguments, print a list of commands and short help for each command.

Given a command name, print help for that command."""
    if len(args) == 0:
        _argParser.print_help()
        return

    name = args[0]
    if not _commands.has_key(name):
        hits = [c for c in _commands.iterkeys() if c.startswith(name)]
        if len(hits) == 1:
            name = hits[0]
        elif len(hits) == 0:
            abort('mx: unknown command \'{0}\'\n{1}use "mx help" for more options'.format(name, _format_commands()))
        else:
            abort('mx: command \'{0}\' is ambiguous\n    {1}'.format(name, ' '.join(hits)))

    value = _commands[name]
    (func, usage) = value[:2]
    doc = func.__doc__
    if len(value) > 2:
        docArgs = value[2:]
        fmtArgs = []
        for d in docArgs:
            if isinstance(d, Callable):
                fmtArgs += [d()]
            else:
                fmtArgs += [str(d)]
        doc = doc.format(*fmtArgs)
    print 'mx {0} {1}\n\n{2}\n'.format(name, usage, doc)

def projectgraph(args, suite=None):
    """create graph for project structure ("mx projectgraph | dot -Tpdf -oprojects.pdf" or "mx projectgraph --igv")"""

    parser = ArgumentParser(prog='mx projectgraph')
    parser.add_argument('--igv', action='store_true', help='output to IGV listening on 127.0.0.1:4444')
    parser.add_argument('--igv-format', action='store_true', help='output graph in IGV format')

    args = parser.parse_args(args)

    if args.igv or args.igv_format:
        ids = {}
        nextToIndex = {}
        igv = XMLDoc()
        igv.open('graphDocument')
        igv.open('group')
        igv.open('properties')
        igv.element('p', {'name' : 'name'}, 'GraalProjectDependencies')
        igv.close('properties')
        igv.open('graph', {'name' : 'dependencies'})
        igv.open('nodes')
        for p in sorted_deps(includeLibs=True, includeJreLibs=True):
            ident = len(ids)
            ids[p.name] = str(ident)
            igv.open('node', {'id' : str(ident)})
            igv.open('properties')
            igv.element('p', {'name' : 'name'}, p.name)
            igv.close('properties')
            igv.close('node')
        igv.close('nodes')
        igv.open('edges')
        for p in projects():
            fromIndex = 0
            for dep in p.canonical_deps():
                toIndex = nextToIndex.get(dep, 0)
                nextToIndex[dep] = toIndex + 1
                igv.element('edge', {'from' : ids[p.name], 'fromIndex' : str(fromIndex), 'to' : ids[dep], 'toIndex' : str(toIndex), 'label' : 'dependsOn'})
                fromIndex = fromIndex + 1
        igv.close('edges')
        igv.close('graph')
        igv.close('group')
        igv.close('graphDocument')

        if args.igv:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(('127.0.0.1', 4444))
            s.send(igv.xml())
        else:
            print igv.xml(indent='  ', newl='\n')
        return

    print 'digraph projects {'
    print 'rankdir=BT;'
    print 'node [shape=rect];'
    for p in projects():
        for dep in p.canonical_deps():
            print '"' + p.name + '"->"' + dep + '"'
    print '}'

def _source_locator_memento(deps):
    slm = XMLDoc()
    slm.open('sourceLookupDirector')
    slm.open('sourceContainers', {'duplicates' : 'false'})

    javaCompliance = None
    for dep in deps:
        if dep.isLibrary():
            if hasattr(dep, 'eclipse.container'):
                memento = XMLDoc().element('classpathContainer', {'path' : getattr(dep, 'eclipse.container')}).xml(standalone='no')
                slm.element('classpathContainer', {'memento' : memento, 'typeId':'org.eclipse.jdt.launching.sourceContainer.classpathContainer'})
            elif dep.get_source_path(resolve=True):
                memento = XMLDoc().element('archive', {'detectRoot' : 'true', 'path' : dep.get_source_path(resolve=True)}).xml(standalone='no')
                slm.element('container', {'memento' : memento, 'typeId':'org.eclipse.debug.core.containerType.externalArchive'})
        elif dep.isProject():
            memento = XMLDoc().element('javaProject', {'name' : dep.name}).xml(standalone='no')
            slm.element('container', {'memento' : memento, 'typeId':'org.eclipse.jdt.launching.sourceContainer.javaProject'})
            if javaCompliance is None or dep.javaCompliance > javaCompliance:
                javaCompliance = dep.javaCompliance

    if javaCompliance:
        memento = XMLDoc().element('classpathContainer', {'path' : 'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-' + str(javaCompliance)}).xml(standalone='no')
        slm.element('classpathContainer', {'memento' : memento, 'typeId':'org.eclipse.jdt.launching.sourceContainer.classpathContainer'})
    else:
        memento = XMLDoc().element('classpathContainer', {'path' : 'org.eclipse.jdt.launching.JRE_CONTAINER'}).xml(standalone='no')
        slm.element('classpathContainer', {'memento' : memento, 'typeId':'org.eclipse.jdt.launching.sourceContainer.classpathContainer'})

    slm.close('sourceContainers')
    slm.close('sourceLookupDirector')
    return slm

def make_eclipse_attach(suite, hostname, port, name=None, deps=None):
    """
    Creates an Eclipse launch configuration file for attaching to a Java process.
    """
    if deps is None:
        deps = []
    slm = _source_locator_memento(deps)
    launch = XMLDoc()
    launch.open('launchConfiguration', {'type' : 'org.eclipse.jdt.launching.remoteJavaApplication'})
    launch.element('stringAttribute', {'key' : 'org.eclipse.debug.core.source_locator_id', 'value' : 'org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector'})
    launch.element('stringAttribute', {'key' : 'org.eclipse.debug.core.source_locator_memento', 'value' : '%s'})
    launch.element('booleanAttribute', {'key' : 'org.eclipse.jdt.launching.ALLOW_TERMINATE', 'value' : 'true'})
    launch.open('mapAttribute', {'key' : 'org.eclipse.jdt.launching.CONNECT_MAP'})
    launch.element('mapEntry', {'key' : 'hostname', 'value' : hostname})
    launch.element('mapEntry', {'key' : 'port', 'value' : port})
    launch.close('mapAttribute')
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.PROJECT_ATTR', 'value' : ''})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.VM_CONNECTOR_ID', 'value' : 'org.eclipse.jdt.launching.socketAttachConnector'})
    launch.close('launchConfiguration')
    launch = launch.xml(newl='\n', standalone='no') % slm.xml(escape=True, standalone='no')

    if name is None:
        if len(suites()) == 1:
            suitePrefix = ''
        else:
            suitePrefix = suite.name + '-'
        name = suitePrefix + 'attach-' + hostname + '-' + port
    eclipseLaunches = join(suite.mxDir, 'eclipse-launches')
    if not exists(eclipseLaunches):
        os.makedirs(eclipseLaunches)
    launchFile = join(eclipseLaunches, name + '.launch')
    return update_file(launchFile, launch), launchFile

def make_eclipse_launch(javaArgs, jre, name=None, deps=None):
    """
    Creates an Eclipse launch configuration file for running/debugging a Java command.
    """
    if deps is None:
        deps = []
    mainClass = None
    vmArgs = []
    appArgs = []
    cp = None
    argsCopy = list(reversed(javaArgs))
    while len(argsCopy) != 0:
        a = argsCopy.pop()
        if a == '-jar':
            mainClass = '-jar'
            appArgs = list(reversed(argsCopy))
            break
        if a == '-cp' or a == '-classpath':
            assert len(argsCopy) != 0
            cp = argsCopy.pop()
            vmArgs.append(a)
            vmArgs.append(cp)
        elif a.startswith('-'):
            vmArgs.append(a)
        else:
            mainClass = a
            appArgs = list(reversed(argsCopy))
            break

    if mainClass is None:
        log('Cannot create Eclipse launch configuration without main class or jar file: java ' + ' '.join(javaArgs))
        return False

    if name is None:
        if mainClass == '-jar':
            name = basename(appArgs[0])
            if len(appArgs) > 1 and not appArgs[1].startswith('-'):
                name = name + '_' + appArgs[1]
        else:
            name = mainClass
        name = time.strftime('%Y-%m-%d-%H%M%S_' + name)

    if cp is not None:
        for e in cp.split(os.pathsep):
            for s in suites():
                deps += [p for p in s.projects if e == p.output_dir()]
                deps += [l for l in s.libs if e == l.get_path(False)]

    slm = _source_locator_memento(deps)

    launch = XMLDoc()
    launch.open('launchConfiguration', {'type' : 'org.eclipse.jdt.launching.localJavaApplication'})
    launch.element('stringAttribute', {'key' : 'org.eclipse.debug.core.source_locator_id', 'value' : 'org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector'})
    launch.element('stringAttribute', {'key' : 'org.eclipse.debug.core.source_locator_memento', 'value' : '%s'})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.JRE_CONTAINER', 'value' : 'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/' + jre})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.MAIN_TYPE', 'value' : mainClass})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.PROGRAM_ARGUMENTS', 'value' : ' '.join(appArgs)})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.PROJECT_ATTR', 'value' : ''})
    launch.element('stringAttribute', {'key' : 'org.eclipse.jdt.launching.VM_ARGUMENTS', 'value' : ' '.join(vmArgs)})
    launch.close('launchConfiguration')
    launch = launch.xml(newl='\n', standalone='no') % slm.xml(escape=True, standalone='no')

    eclipseLaunches = join('mx', 'eclipse-launches')
    if not exists(eclipseLaunches):
        os.makedirs(eclipseLaunches)
    return update_file(join(eclipseLaunches, name + '.launch'), launch)

def eclipseinit(args, buildProcessorJars=True, refreshOnly=False):
    """(re)generate Eclipse project configurations and working sets"""
    for s in suites(True):
        _eclipseinit_suite(args, s, buildProcessorJars, refreshOnly)

    generate_eclipse_workingsets()

def _check_ide_timestamp(suite, configZip, ide):
    """return True if and only if the projects file, eclipse-settings files, and mx itself are all older than configZip"""
    projectsFile = join(suite.mxDir, 'projects')
    if configZip.isOlderThan(projectsFile):
        return False
    # Assume that any mx change might imply changes to the generated IDE files
    if configZip.isOlderThan(__file__):
        return False

    if ide == 'eclipse':
        eclipseSettingsDir = join(suite.mxDir, 'eclipse-settings')
        if exists(eclipseSettingsDir):
            for name in os.listdir(eclipseSettingsDir):
                path = join(eclipseSettingsDir, name)
                if configZip.isOlderThan(path):
                    return False
    return True

def _eclipseinit_project(p, files=None, libFiles=None):
    assert java(p.javaCompliance)

    if not exists(p.dir):
        os.makedirs(p.dir)

    out = XMLDoc()
    out.open('classpath')

    for src in p.srcDirs:
        srcDir = join(p.dir, src)
        if not exists(srcDir):
            os.mkdir(srcDir)
        out.element('classpathentry', {'kind' : 'src', 'path' : src})

    if len(p.annotation_processors()) > 0:
        genDir = p.source_gen_dir()
        if not exists(genDir):
            os.mkdir(genDir)
        out.element('classpathentry', {'kind' : 'src', 'path' : 'src_gen'})
        if files:
            files.append(genDir)

    # Every Java program depends on a JRE
    out.element('classpathentry', {'kind' : 'con', 'path' : 'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-' + str(p.javaCompliance)})

    if exists(join(p.dir, 'plugin.xml')):  # eclipse plugin project
        out.element('classpathentry', {'kind' : 'con', 'path' : 'org.eclipse.pde.core.requiredPlugins'})

    containerDeps = set()
    libraryDeps = set()
    projectDeps = set()

    for dep in p.all_deps([], True):
        if dep == p:
            continue
        if dep.isLibrary():
            if hasattr(dep, 'eclipse.container'):
                container = getattr(dep, 'eclipse.container')
                containerDeps.add(container)
                libraryDeps -= set(dep.all_deps([], True))
            else:
                libraryDeps.add(dep)
        elif dep.isProject():
            projectDeps.add(dep)

    for dep in containerDeps:
        out.element('classpathentry', {'exported' : 'true', 'kind' : 'con', 'path' : dep})

    for dep in libraryDeps:
        path = dep.path
        dep.get_path(resolve=True)

        # Relative paths for "lib" class path entries have various semantics depending on the Eclipse
        # version being used (e.g. see https://bugs.eclipse.org/bugs/show_bug.cgi?id=274737) so it's
        # safest to simply use absolute paths.
        path = _make_absolute(path, p.suite.dir)

        attributes = {'exported' : 'true', 'kind' : 'lib', 'path' : path}

        sourcePath = dep.get_source_path(resolve=True)
        if sourcePath is not None:
            attributes['sourcepath'] = sourcePath
        out.element('classpathentry', attributes)
        if libFiles:
            libFiles.append(path)

    for dep in projectDeps:
        out.element('classpathentry', {'combineaccessrules' : 'false', 'exported' : 'true', 'kind' : 'src', 'path' : '/' + dep.name})

    out.element('classpathentry', {'kind' : 'output', 'path' : getattr(p, 'eclipse.output', 'bin')})
    out.close('classpath')
    classpathFile = join(p.dir, '.classpath')
    update_file(classpathFile, out.xml(indent='\t', newl='\n'))
    if files:
        files.append(classpathFile)

    csConfig = join(project(p.checkstyleProj).dir, '.checkstyle_checks.xml')
    if exists(csConfig):
        out = XMLDoc()

        dotCheckstyle = join(p.dir, ".checkstyle")
        checkstyleConfigPath = '/' + p.checkstyleProj + '/.checkstyle_checks.xml'
        out.open('fileset-config', {'file-format-version' : '1.2.0', 'simple-config' : 'true'})
        out.open('local-check-config', {'name' : 'Checks', 'location' : checkstyleConfigPath, 'type' : 'project', 'description' : ''})
        out.element('additional-data', {'name' : 'protect-config-file', 'value' : 'false'})
        out.close('local-check-config')
        out.open('fileset', {'name' : 'all', 'enabled' : 'true', 'check-config-name' : 'Checks', 'local' : 'true'})
        out.element('file-match-pattern', {'match-pattern' : '.', 'include-pattern' : 'true'})
        out.close('fileset')
        out.open('filter', {'name' : 'all', 'enabled' : 'true', 'check-config-name' : 'Checks', 'local' : 'true'})
        out.element('filter-data', {'value' : 'java'})
        out.close('filter')

        exclude = join(p.dir, '.checkstyle.exclude')
        if exists(exclude):
            out.open('filter', {'name' : 'FilesFromPackage', 'enabled' : 'true'})
            with open(exclude) as f:
                for line in f:
                    if not line.startswith('#'):
                        line = line.strip()
                        exclDir = join(p.dir, line)
                        assert isdir(exclDir), 'excluded source directory listed in ' + exclude + ' does not exist or is not a directory: ' + exclDir
                    out.element('filter-data', {'value' : line})
            out.close('filter')

        out.close('fileset-config')
        update_file(dotCheckstyle, out.xml(indent='  ', newl='\n'))
        if files:
            files.append(dotCheckstyle)
    else:
        # clean up existing .checkstyle file
        dotCheckstyle = join(p.dir, ".checkstyle")
        if exists(dotCheckstyle):
            os.unlink(dotCheckstyle)

    out = XMLDoc()
    out.open('projectDescription')
    out.element('name', data=p.name)
    out.element('comment', data='')
    out.element('projects', data='')
    out.open('buildSpec')
    out.open('buildCommand')
    out.element('name', data='org.eclipse.jdt.core.javabuilder')
    out.element('arguments', data='')
    out.close('buildCommand')
    if exists(csConfig):
        out.open('buildCommand')
        out.element('name', data='net.sf.eclipsecs.core.CheckstyleBuilder')
        out.element('arguments', data='')
        out.close('buildCommand')
    if exists(join(p.dir, 'plugin.xml')):  # eclipse plugin project
        for buildCommand in ['org.eclipse.pde.ManifestBuilder', 'org.eclipse.pde.SchemaBuilder']:
            out.open('buildCommand')
            out.element('name', data=buildCommand)
            out.element('arguments', data='')
            out.close('buildCommand')

    if p.definedAnnotationProcessorsDist:
        # Create a launcher that will (re)build the annotation processor
        # jar any time one of its sources is modified.
        dist = p.definedAnnotationProcessorsDist

        distProjects = [d for d in dist.sorted_deps(transitive=True) if d.isProject()]
        relevantResources = []
        for p in distProjects:
            for srcDir in p.source_dirs():
                relevantResources.append(join(p.name, os.path.relpath(srcDir, p.dir)))
            relevantResources.append(join(p.name, os.path.relpath(p.output_dir(), p.dir)))

        # The path should always be p.name/dir independent of where the workspace actually is.
        # So we use the parent folder of the project, whatever that is, to generate such a relative path.
        logicalWorkspaceRoot = os.path.dirname(p.dir)
        refreshFile = os.path.relpath(p.definedAnnotationProcessorsDist.path, logicalWorkspaceRoot)
        _genEclipseBuilder(out, p, 'CreateAnnotationProcessorJar', 'archive @' + dist.name, refresh=True, refreshFile=refreshFile, relevantResources=relevantResources, async=True, xmlIndent='', xmlStandalone='no')

    out.close('buildSpec')
    out.open('natures')
    out.element('nature', data='org.eclipse.jdt.core.javanature')
    if exists(csConfig):
        out.element('nature', data='net.sf.eclipsecs.core.CheckstyleNature')
    if exists(join(p.dir, 'plugin.xml')):  # eclipse plugin project
        out.element('nature', data='org.eclipse.pde.PluginNature')
    out.close('natures')
    out.close('projectDescription')
    projectFile = join(p.dir, '.project')
    update_file(projectFile, out.xml(indent='\t', newl='\n'))
    if files:
        files.append(projectFile)

    settingsDir = join(p.dir, ".settings")
    if not exists(settingsDir):
        os.mkdir(settingsDir)

    # collect the defaults from mxtool
    defaultEclipseSettingsDir = join(dirname(__file__), 'eclipse-settings')
    esdict = {}
    if exists(defaultEclipseSettingsDir):
        for name in os.listdir(defaultEclipseSettingsDir):
            if isfile(join(defaultEclipseSettingsDir, name)):
                esdict[name] = os.path.abspath(join(defaultEclipseSettingsDir, name))

    # check for suite overrides
    eclipseSettingsDir = join(p.suite.mxDir, 'eclipse-settings')
    if exists(eclipseSettingsDir):
        for name in os.listdir(eclipseSettingsDir):
            if isfile(join(eclipseSettingsDir, name)):
                esdict[name] = os.path.abspath(join(eclipseSettingsDir, name))

    # check for project overrides
    projectSettingsDir = join(p.dir, 'eclipse-settings')
    if exists(projectSettingsDir):
        for name in os.listdir(projectSettingsDir):
            if isfile(join(projectSettingsDir, name)):
                esdict[name] = os.path.abspath(join(projectSettingsDir, name))

    # copy a possibly modified file to the project's .settings directory
    for name, path in esdict.iteritems():
        # ignore this file altogether if this project has no annotation processors
        if name == "org.eclipse.jdt.apt.core.prefs" and not len(p.annotation_processors()) > 0:
            continue

        with open(path) as f:
            content = f.read()
        content = content.replace('${javaCompliance}', str(p.javaCompliance))
        if len(p.annotation_processors()) > 0:
            content = content.replace('org.eclipse.jdt.core.compiler.processAnnotations=disabled', 'org.eclipse.jdt.core.compiler.processAnnotations=enabled')
        update_file(join(settingsDir, name), content)
        if files:
            files.append(join(settingsDir, name))

    processorPath = p.annotation_processors_path()
    if processorPath:
        out = XMLDoc()
        out.open('factorypath')
        out.element('factorypathentry', {'kind' : 'PLUGIN', 'id' : 'org.eclipse.jst.ws.annotations.core', 'enabled' : 'true', 'runInBatchMode' : 'false'})
        for e in processorPath.split(os.pathsep):
            out.element('factorypathentry', {'kind' : 'EXTJAR', 'id' : e, 'enabled' : 'true', 'runInBatchMode' : 'false'})
        out.close('factorypath')
        update_file(join(p.dir, '.factorypath'), out.xml(indent='\t', newl='\n'))
        if files:
            files.append(join(p.dir, '.factorypath'))

def _eclipseinit_suite(args, suite, buildProcessorJars=True, refreshOnly=False):
    configZip = TimeStampFile(join(suite.mxDir, 'eclipse-config.zip'))
    configLibsZip = join(suite.mxDir, 'eclipse-config-libs.zip')
    if refreshOnly and not configZip.exists():
        return

    if _check_ide_timestamp(suite, configZip, 'eclipse'):
        logv('[Eclipse configurations are up to date - skipping]')
        return

    files = []
    libFiles = []
    if buildProcessorJars:
        files += _processorjars_suite(suite)

    for p in suite.projects:
        if p.native:
            continue
        _eclipseinit_project(p, files, libFiles)

    _, launchFile = make_eclipse_attach(suite, 'localhost', '8000', deps=sorted_deps(projectNames=None, includeLibs=True))
    files.append(launchFile)

    # Create an Eclipse project for each distribution that will create/update the archive
    # for the distribution whenever any (transitively) dependent project of the
    # distribution is updated.
    for dist in suite.dists:
        projectDir = dist.get_ide_project_dir()
        if not projectDir:
            continue
        if not exists(projectDir):
            os.makedirs(projectDir)
        distProjects = [d for d in dist.sorted_deps(transitive=True) if d.isProject()]
        relevantResources = []
        for p in distProjects:
            for srcDir in p.source_dirs():
                relevantResources.append(join(p.name, os.path.relpath(srcDir, p.dir)))
            relevantResources.append(join(p.name, os.path.relpath(p.output_dir(), p.dir)))
        out = XMLDoc()
        out.open('projectDescription')
        out.element('name', data=dist.name)
        out.element('comment', data='Updates ' + dist.path + ' if a project dependency of ' + dist.name + ' is updated')
        out.open('projects')
        for p in distProjects:
            out.element('project', data=p.name)
        for d in dist.distDependencies:
            out.element('project', data=d)
        out.close('projects')
        out.open('buildSpec')
        dist.dir = projectDir
        dist.javaCompliance = max([p.javaCompliance for p in distProjects])
        _genEclipseBuilder(out, dist, 'Create' + dist.name + 'Dist', 'archive @' + dist.name, relevantResources=relevantResources, logToFile=True, refresh=False, async=True)
        out.close('buildSpec')
        out.open('natures')
        out.element('nature', data='org.eclipse.jdt.core.javanature')
        out.close('natures')
        out.close('projectDescription')
        projectFile = join(projectDir, '.project')
        update_file(projectFile, out.xml(indent='\t', newl='\n'))
        files.append(projectFile)

    _zip_files(files, suite.dir, configZip.path)
    _zip_files(libFiles, suite.dir, configLibsZip)

def _zip_files(files, baseDir, zipPath):
    fd, tmp = tempfile.mkstemp(suffix='', prefix=basename(zipPath), dir=baseDir)
    try:
        zf = zipfile.ZipFile(tmp, 'w')
        for f in sorted(set(files)):
            relpath = os.path.relpath(f, baseDir)
            arcname = relpath.replace(os.sep, '/')
            zf.write(f, arcname)
        zf.close()
        os.close(fd)
        # Atomic on Unix
        shutil.move(tmp, zipPath)
        # Correct the permissions on the temporary file which is created with restrictive permissions
        os.chmod(zipPath, 0o666 & ~currentUmask)
    finally:
        if exists(tmp):
            os.remove(tmp)

def _genEclipseBuilder(dotProjectDoc, p, name, mxCommand, refresh=True, refreshFile=None, relevantResources=None, async=False, logToConsole=False, logToFile=False, appendToLogFile=True, xmlIndent='\t', xmlStandalone=None):
    externalToolDir = join(p.dir, '.externalToolBuilders')
    launchOut = XMLDoc()
    consoleOn = 'true' if logToConsole else 'false'
    launchOut.open('launchConfiguration', {'type' : 'org.eclipse.ui.externaltools.ProgramBuilderLaunchConfigurationType'})
    launchOut.element('booleanAttribute', {'key' : 'org.eclipse.debug.core.capture_output', 'value': consoleOn})
    launchOut.open('mapAttribute', {'key' : 'org.eclipse.debug.core.environmentVariables'})
    launchOut.element('mapEntry', {'key' : 'JAVA_HOME', 'value' : java(p.javaCompliance).jdk})
    launchOut.element('mapEntry', {'key' : 'EXTRA_JAVA_HOMES', 'value' : _opts.extra_java_homes})
    launchOut.close('mapAttribute')

    if refresh:
        if refreshFile is None:
            refreshScope = '${project}'
        else:
            refreshScope = '${working_set:<?xml version="1.0" encoding="UTF-8"?><resources><item path="' + refreshFile + '" type="1"/></resources>}'

        launchOut.element('booleanAttribute', {'key' : 'org.eclipse.debug.core.ATTR_REFRESH_RECURSIVE', 'value':  'false'})
        launchOut.element('stringAttribute', {'key' : 'org.eclipse.debug.core.ATTR_REFRESH_SCOPE', 'value':  refreshScope})

    if relevantResources is not None:
        resources = '${working_set:<?xml version="1.0" encoding="UTF-8"?><resources>'
        for relevantResource in relevantResources:
            resources += '<item path="' + relevantResource + '" type="2" />'
        resources += '</resources>}'
        launchOut.element('stringAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE', 'value': resources})


    launchOut.element('booleanAttribute', {'key' : 'org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON', 'value': consoleOn})
    launchOut.element('booleanAttribute', {'key' : 'org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND', 'value': 'true' if async else 'false'})
    if logToFile:
        logFile = join(externalToolDir, name + '.log')
        launchOut.element('stringAttribute', {'key' : 'org.eclipse.debug.ui.ATTR_CAPTURE_IN_FILE', 'value': logFile})
        launchOut.element('booleanAttribute', {'key' : 'org.eclipse.debug.ui.ATTR_APPEND_TO_FILE', 'value': 'true' if appendToLogFile else 'false'})

    # expect to find the OS command to invoke mx in the same directory
    baseDir = dirname(os.path.abspath(__file__))

    cmd = 'mx.sh'
    if get_os() == 'windows':
        cmd = 'mx.cmd'
    cmdPath = join(baseDir, cmd)
    if not os.path.exists(cmdPath):
        # backwards compatibility for when the commands lived in parent of mxtool
        cmdPath = join(dirname(baseDir), cmd)
        if not os.path.exists(cmdPath):
            abort('cannot locate ' + cmd)

    launchOut.element('stringAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_LOCATION', 'value':  cmdPath})
    launchOut.element('stringAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS', 'value': 'full,incremental,auto,'})
    launchOut.element('stringAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS', 'value': mxCommand})
    launchOut.element('booleanAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED', 'value': 'true'})
    launchOut.element('stringAttribute', {'key' : 'org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY', 'value': p.suite.dir})


    launchOut.close('launchConfiguration')

    if not exists(externalToolDir):
        os.makedirs(externalToolDir)
    update_file(join(externalToolDir, name + '.launch'), launchOut.xml(indent=xmlIndent, standalone=xmlStandalone, newl='\n'))

    dotProjectDoc.open('buildCommand')
    dotProjectDoc.element('name', data='org.eclipse.ui.externaltools.ExternalToolBuilder')
    dotProjectDoc.element('triggers', data='auto,full,incremental,')
    dotProjectDoc.open('arguments')
    dotProjectDoc.open('dictionary')
    dotProjectDoc.element('key', data='LaunchConfigHandle')
    dotProjectDoc.element('value', data='<project>/.externalToolBuilders/' + name + '.launch')
    dotProjectDoc.close('dictionary')
    dotProjectDoc.open('dictionary')
    dotProjectDoc.element('key', data='incclean')
    dotProjectDoc.element('value', data='true')
    dotProjectDoc.close('dictionary')
    dotProjectDoc.close('arguments')
    dotProjectDoc.close('buildCommand')

def generate_eclipse_workingsets():
    """
    Populate the workspace's working set configuration with working sets generated from project data for the primary suite
    If the workspace already contains working set definitions, the existing ones will be retained and extended.
    In case mx/env does not contain a WORKSPACE definition pointing to the workspace root directory, a parent search from the primary suite directory is performed.
    If no workspace root directory can be identified, the primary suite directory is used and the user has to place the workingsets.xml file by hand.
    """

    # identify the location where to look for workingsets.xml
    wsfilename = 'workingsets.xml'
    wsloc = '.metadata/.plugins/org.eclipse.ui.workbench'
    if os.environ.has_key('WORKSPACE'):
        expected_wsroot = os.environ['WORKSPACE']
    else:
        expected_wsroot = _primary_suite.dir

    wsroot = _find_eclipse_wsroot(expected_wsroot)
    if wsroot is None:
        # failed to find it
        wsroot = expected_wsroot

    wsdir = join(wsroot, wsloc)
    if not exists(wsdir):
        wsdir = wsroot
        logv('Could not find Eclipse metadata directory. Please place ' + wsfilename + ' in ' + wsloc + ' manually.')
    wspath = join(wsdir, wsfilename)

    # gather working set info from project data
    workingSets = dict()
    for p in projects():
        if p.workingSets is None:
            continue
        for w in p.workingSets.split(","):
            if not workingSets.has_key(w):
                workingSets[w] = [p.name]
            else:
                workingSets[w].append(p.name)

    if exists(wspath):
        wsdoc = _copy_workingset_xml(wspath, workingSets)
    else:
        wsdoc = _make_workingset_xml(workingSets)

    update_file(wspath, wsdoc.xml(newl='\n'))

def _find_eclipse_wsroot(wsdir):
    md = join(wsdir, '.metadata')
    if exists(md):
        return wsdir
    split = os.path.split(wsdir)
    if split[0] == wsdir:  # root directory
        return None
    else:
        return _find_eclipse_wsroot(split[0])

def _make_workingset_xml(workingSets):
    wsdoc = XMLDoc()
    wsdoc.open('workingSetManager')

    for w in sorted(workingSets.keys()):
        _workingset_open(wsdoc, w)
        for p in workingSets[w]:
            _workingset_element(wsdoc, p)
        wsdoc.close('workingSet')

    wsdoc.close('workingSetManager')
    return wsdoc

def _copy_workingset_xml(wspath, workingSets):
    target = XMLDoc()
    target.open('workingSetManager')

    parser = xml.parsers.expat.ParserCreate()

    class ParserState(object):
        def __init__(self):
            self.current_ws_name = 'none yet'
            self.current_ws = None
            self.seen_ws = list()
            self.seen_projects = list()
            self.aggregate_ws = False
            self.nested_ws = False

    ps = ParserState()

    # parsing logic
    def _ws_start(name, attributes):
        if name == 'workingSet':
            if attributes.has_key('name'):
                ps.current_ws_name = attributes['name']
                if attributes.has_key('aggregate') and attributes['aggregate'] == 'true':
                    ps.aggregate_ws = True
                    ps.current_ws = None
                elif workingSets.has_key(ps.current_ws_name):
                    ps.current_ws = workingSets[ps.current_ws_name]
                    ps.seen_ws.append(ps.current_ws_name)
                    ps.seen_projects = list()
                else:
                    ps.current_ws = None
            target.open(name, attributes)
            parser.StartElementHandler = _ws_item

    def _ws_end(name):
        closeAndResetHandler = False
        if name == 'workingSet':
            if ps.aggregate_ws:
                if ps.nested_ws:
                    ps.nested_ws = False
                else:
                    ps.aggregate_ws = False
                    closeAndResetHandler = True
            else:
                if not ps.current_ws is None:
                    for p in ps.current_ws:
                        if not p in ps.seen_projects:
                            _workingset_element(target, p)
                closeAndResetHandler = True
            if closeAndResetHandler:
                target.close('workingSet')
                parser.StartElementHandler = _ws_start
        elif name == 'workingSetManager':
            # process all working sets that are new to the file
            for w in sorted(workingSets.keys()):
                if not w in ps.seen_ws:
                    _workingset_open(target, w)
                    for p in workingSets[w]:
                        _workingset_element(target, p)
                    target.close('workingSet')

    def _ws_item(name, attributes):
        if name == 'item':
            if ps.current_ws is None:
                target.element(name, attributes)
            elif not attributes.has_key('elementID') and attributes.has_key('factoryID') and attributes.has_key('path') and attributes.has_key('type'):
                target.element(name, attributes)
                p_name = attributes['path'][1:]  # strip off the leading '/'
                ps.seen_projects.append(p_name)
            else:
                p_name = attributes['elementID'][1:]  # strip off the leading '='
                _workingset_element(target, p_name)
                ps.seen_projects.append(p_name)
        elif name == 'workingSet':
            ps.nested_ws = True
            target.element(name, attributes)

    # process document
    parser.StartElementHandler = _ws_start
    parser.EndElementHandler = _ws_end
    with open(wspath, 'r') as wsfile:
        parser.ParseFile(wsfile)

    target.close('workingSetManager')
    return target

def _workingset_open(wsdoc, ws):
    wsdoc.open('workingSet', {'editPageID': 'org.eclipse.jdt.ui.JavaWorkingSetPage', 'factoryID': 'org.eclipse.ui.internal.WorkingSetFactory', 'id': 'wsid_' + ws, 'label': ws, 'name': ws})

def _workingset_element(wsdoc, p):
    wsdoc.element('item', {'elementID': '=' + p, 'factoryID': 'org.eclipse.jdt.ui.PersistableJavaElementFactory'})

def netbeansinit(args, refreshOnly=False, buildProcessorJars=True):
    """(re)generate NetBeans project configurations"""

    for suite in suites(True):
        _netbeansinit_suite(args, suite, refreshOnly, buildProcessorJars)

def _netbeansinit_suite(args, suite, refreshOnly=False, buildProcessorJars=True):
    configZip = TimeStampFile(join(suite.mxDir, 'netbeans-config.zip'))
    configLibsZip = join(suite.mxDir, 'eclipse-config-libs.zip')
    if refreshOnly and not configZip.exists():
        return

    if _check_ide_timestamp(suite, configZip, 'netbeans'):
        logv('[NetBeans configurations are up to date - skipping]')
        return

    updated = False
    files = []
    libFiles = []
    jdks = set()
    for p in suite.projects:
        if p.native:
            continue

        if exists(join(p.dir, 'plugin.xml')):  # eclipse plugin project
            continue

        if not exists(join(p.dir, 'nbproject')):
            os.makedirs(join(p.dir, 'nbproject'))

        jdk = java(p.javaCompliance)
        assert jdk

        jdks.add(jdk)

        out = XMLDoc()
        out.open('project', {'name' : p.name, 'default' : 'default', 'basedir' : '.'})
        out.element('description', data='Builds, tests, and runs the project ' + p.name + '.')
        out.element('import', {'file' : 'nbproject/build-impl.xml'})
        out.open('target', {'name' : '-post-compile'})
        out.open('exec', {'executable' : sys.executable})
        out.element('env', {'key' : 'JAVA_HOME', 'value' : jdk.jdk})
        out.element('arg', {'value' : os.path.abspath(__file__)})
        out.element('arg', {'value' : 'archive'})
        out.element('arg', {'value' : '@GRAAL'})
        out.close('exec')
        out.close('target')
        out.close('project')
        updated = update_file(join(p.dir, 'build.xml'), out.xml(indent='\t', newl='\n')) or updated
        files.append(join(p.dir, 'build.xml'))

        out = XMLDoc()
        out.open('project', {'xmlns' : 'http://www.netbeans.org/ns/project/1'})
        out.element('type', data='org.netbeans.modules.java.j2seproject')
        out.open('configuration')
        out.open('data', {'xmlns' : 'http://www.netbeans.org/ns/j2se-project/3'})
        out.element('name', data=p.name)
        out.element('explicit-platform', {'explicit-source-supported' : 'true'})
        out.open('source-roots')
        out.element('root', {'id' : 'src.dir'})
        if len(p.annotation_processors()) > 0:
            out.element('root', {'id' : 'src.ap-source-output.dir'})
        out.close('source-roots')
        out.open('test-roots')
        out.close('test-roots')
        out.close('data')

        firstDep = True
        for dep in p.all_deps([], True):
            if dep == p:
                continue

            if dep.isProject():
                n = dep.name.replace('.', '_')
                if firstDep:
                    out.open('references', {'xmlns' : 'http://www.netbeans.org/ns/ant-project-references/1'})
                    firstDep = False

                out.open('reference')
                out.element('foreign-project', data=n)
                out.element('artifact-type', data='jar')
                out.element('script', data='build.xml')
                out.element('target', data='jar')
                out.element('clean-target', data='clean')
                out.element('id', data='jar')
                out.close('reference')

        if not firstDep:
            out.close('references')

        out.close('configuration')
        out.close('project')
        updated = update_file(join(p.dir, 'nbproject', 'project.xml'), out.xml(indent='    ', newl='\n')) or updated
        files.append(join(p.dir, 'nbproject', 'project.xml'))

        out = StringIO.StringIO()
        jdkPlatform = 'JDK_' + str(jdk.version)

        annotationProcessorEnabled = "false"
        annotationProcessorReferences = ""
        annotationProcessorSrcFolder = ""
        if len(p.annotation_processors()) > 0:
            annotationProcessorEnabled = "true"
            annotationProcessorSrcFolder = "src.ap-source-output.dir=${build.generated.sources.dir}/ap-source-output"

        content = """
annotation.processing.enabled=""" + annotationProcessorEnabled + """
annotation.processing.enabled.in.editor=""" + annotationProcessorEnabled + """
annotation.processing.processors.list=
annotation.processing.run.all.processors=true
application.title=""" + p.name + """
application.vendor=mx
build.classes.dir=${build.dir}
build.classes.excludes=**/*.java,**/*.form
# This directory is removed when the project is cleaned:
build.dir=bin
build.generated.dir=${build.dir}/generated
build.generated.sources.dir=${build.dir}/generated-sources
# Only compile against the classpath explicitly listed here:
build.sysclasspath=ignore
build.test.classes.dir=${build.dir}/test/classes
build.test.results.dir=${build.dir}/test/results
# Uncomment to specify the preferred debugger connection transport:
#debug.transport=dt_socket
debug.classpath=\\
    ${run.classpath}
debug.test.classpath=\\
    ${run.test.classpath}
# This directory is removed when the project is cleaned:
dist.dir=dist
dist.jar=${dist.dir}/""" + p.name + """.jar
dist.javadoc.dir=${dist.dir}/javadoc
endorsed.classpath=
excludes=
includes=**
jar.compress=false
# Space-separated list of extra javac options
javac.compilerargs=
javac.deprecation=false
javac.source=""" + str(p.javaCompliance) + """
javac.target=""" + str(p.javaCompliance) + """
javac.test.classpath=\\
    ${javac.classpath}:\\
    ${build.classes.dir}
javadoc.additionalparam=
javadoc.author=false
javadoc.encoding=${source.encoding}
javadoc.noindex=false
javadoc.nonavbar=false
javadoc.notree=false
javadoc.private=false
javadoc.splitindex=true
javadoc.use=true
javadoc.version=false
javadoc.windowtitle=
main.class=
manifest.file=manifest.mf
meta.inf.dir=${src.dir}/META-INF
mkdist.disabled=false
platforms.""" + jdkPlatform + """.home=""" + jdk.jdk + """
platform.active=""" + jdkPlatform + """
run.classpath=\\
    ${javac.classpath}:\\
    ${build.classes.dir}
# Space-separated list of JVM arguments used when running the project
# (you may also define separate properties like run-sys-prop.name=value instead of -Dname=value
# or test-sys-prop.name=value to set system properties for unit tests):
run.jvmargs=
run.test.classpath=\\
    ${javac.test.classpath}:\\
    ${build.test.classes.dir}
test.src.dir=./test
""" + annotationProcessorSrcFolder + """
source.encoding=UTF-8""".replace(':', os.pathsep).replace('/', os.sep)
        print >> out, content

        mainSrc = True
        for src in p.srcDirs:
            srcDir = join(p.dir, src)
            if not exists(srcDir):
                os.mkdir(srcDir)
            ref = 'file.reference.' + p.name + '-' + src
            print >> out, ref + '=' + src
            if mainSrc:
                print >> out, 'src.dir=${' + ref + '}'
                mainSrc = False
            else:
                print >> out, 'src.' + src + '.dir=${' + ref + '}'

        javacClasspath = []

        deps = p.all_deps([], True)
        annotationProcessorOnlyDeps = []
        if len(p.annotation_processors()) > 0:
            for ap in p.annotation_processors():
                apDep = dependency(ap)
                if not apDep in deps:
                    deps.append(apDep)
                    annotationProcessorOnlyDeps.append(apDep)

        annotationProcessorReferences = []

        for dep in deps:
            if dep == p:
                continue

            if dep.isLibrary():
                path = dep.get_path(resolve=True)
                if path:
                    if os.sep == '\\':
                        path = path.replace('\\', '\\\\')
                    ref = 'file.reference.' + dep.name + '-bin'
                    print >> out, ref + '=' + path
                    libFiles.append(path)

            elif dep.isProject():
                n = dep.name.replace('.', '_')
                relDepPath = os.path.relpath(dep.dir, p.dir).replace(os.sep, '/')
                ref = 'reference.' + n + '.jar'
                print >> out, 'project.' + n + '=' + relDepPath
                print >> out, ref + '=${project.' + n + '}/dist/' + dep.name + '.jar'

            if not dep in annotationProcessorOnlyDeps:
                javacClasspath.append('${' + ref + '}')
            else:
                annotationProcessorReferences.append('${' + ref + '}')

        print >> out, 'javac.classpath=\\\n    ' + (os.pathsep + '\\\n    ').join(javacClasspath)
        print >> out, 'javac.processorpath=' + (os.pathsep + '\\\n    ').join(['${javac.classpath}'] + annotationProcessorReferences)
        print >> out, 'javac.test.processorpath=' + (os.pathsep + '\\\n    ').join(['${javac.test.classpath}'] + annotationProcessorReferences)

        updated = update_file(join(p.dir, 'nbproject', 'project.properties'), out.getvalue()) or updated
        out.close()
        files.append(join(p.dir, 'nbproject', 'project.properties'))

    if updated:
        log('If using NetBeans:')
        log('  1. Ensure that the following platform(s) are defined (Tools -> Java Platforms):')
        for jdk in jdks:
            log('        JDK_' + str(jdk.version))
        log('  2. Open/create a Project Group for the directory containing the projects (File -> Project Group -> New Group... -> Folder of Projects)')

    _zip_files(files, suite.dir, configZip.path)
    _zip_files(libFiles, suite.dir, configLibsZip)

def intellijinit(args, refreshOnly=False):
    """(re)generate Intellij project configurations"""

    for suite in suites(True):
        _intellij_suite(args, suite, refreshOnly)

def _intellij_suite(args, suite, refreshOnly=False):

    libraries = set()

    ideaProjectDirectory = join(suite.dir, '.idea')

    if not exists(ideaProjectDirectory):
        os.mkdir(ideaProjectDirectory)
    nameFile = join(ideaProjectDirectory, '.name')
    update_file(nameFile, "Graal")
    modulesXml = XMLDoc()
    modulesXml.open('project', attributes={'version': '4'})
    modulesXml.open('component', attributes={'name': 'ProjectModuleManager'})
    modulesXml.open('modules')


    def _intellij_exclude_if_exists(xml, p, name):
        path = join(p.dir, name)
        if exists(path):
            xml.element('excludeFolder', attributes={'url':'file://$MODULE_DIR$/' + name})

    annotationProcessorProfiles = {}

    def _complianceToIntellijLanguageLevel(compliance):
        return 'JDK_1_' + str(compliance.value)

    # create the modules (1 module  = 1 Intellij project)
    for p in suite.projects:
        if p.native:
            continue

        assert java(p.javaCompliance)

        if not exists(p.dir):
            os.makedirs(p.dir)

        annotationProcessorProfileKey = tuple(p.annotation_processors())

        if not annotationProcessorProfileKey in annotationProcessorProfiles:
            annotationProcessorProfiles[annotationProcessorProfileKey] = [p]
        else:
            annotationProcessorProfiles[annotationProcessorProfileKey].append(p)

        intellijLanguageLevel = _complianceToIntellijLanguageLevel(p.javaCompliance)

        moduleXml = XMLDoc()
        moduleXml.open('module', attributes={'type': 'JAVA_MODULE', 'version': '4'})

        moduleXml.open('component', attributes={'name': 'NewModuleRootManager', 'LANGUAGE_LEVEL': intellijLanguageLevel, 'inherit-compiler-output': 'false'})
        moduleXml.element('output', attributes={'url': 'file://$MODULE_DIR$/bin'})
        moduleXml.element('exclude-output')

        moduleXml.open('content', attributes={'url': 'file://$MODULE_DIR$'})
        for src in p.srcDirs:
            srcDir = join(p.dir, src)
            if not exists(srcDir):
                os.mkdir(srcDir)
            moduleXml.element('sourceFolder', attributes={'url':'file://$MODULE_DIR$/' + src, 'isTestSource': 'false'})

        if len(p.annotation_processors()) > 0:
            genDir = p.source_gen_dir()
            if not exists(genDir):
                os.mkdir(genDir)
            moduleXml.element('sourceFolder', attributes={'url':'file://$MODULE_DIR$/' + os.path.relpath(genDir, p.dir), 'isTestSource': 'false'})

        for name in ['.externalToolBuilders', '.settings', 'nbproject']:
            _intellij_exclude_if_exists(moduleXml, p, name)
        moduleXml.close('content')

        moduleXml.element('orderEntry', attributes={'type': 'jdk', 'jdkType': 'JavaSDK', 'jdkName': str(p.javaCompliance)})
        moduleXml.element('orderEntry', attributes={'type': 'sourceFolder', 'forTests': 'false'})

        deps = p.all_deps([], True, includeAnnotationProcessors=True)
        for dep in deps:
            if dep == p:
                continue

            if dep.isLibrary():
                libraries.add(dep)
                moduleXml.element('orderEntry', attributes={'type': 'library', 'name': dep.name, 'level': 'project'})
            elif dep.isProject():
                moduleXml.element('orderEntry', attributes={'type': 'module', 'module-name': dep.name})

        moduleXml.close('component')
        moduleXml.close('module')
        moduleFile = join(p.dir, p.name + '.iml')
        update_file(moduleFile, moduleXml.xml(indent='  ', newl='\n'))

        moduleFilePath = "$PROJECT_DIR$/" + os.path.relpath(moduleFile, suite.dir)
        modulesXml.element('module', attributes={'fileurl': 'file://' + moduleFilePath, 'filepath': moduleFilePath})

    modulesXml.close('modules')
    modulesXml.close('component')
    modulesXml.close('project')
    moduleXmlFile = join(ideaProjectDirectory, 'modules.xml')
    update_file(moduleXmlFile, modulesXml.xml(indent='  ', newl='\n'))

    # TODO What about cross-suite dependencies?

    librariesDirectory = join(ideaProjectDirectory, 'libraries')

    if not exists(librariesDirectory):
        os.mkdir(librariesDirectory)

    # Setup the libraries that were used above
    # TODO: setup all the libraries from the suite regardless of usage?
    for library in libraries:
        libraryXml = XMLDoc()

        libraryXml.open('component', attributes={'name': 'libraryTable'})
        libraryXml.open('library', attributes={'name': library.name})
        libraryXml.open('CLASSES')
        libraryXml.element('root', attributes={'url': 'jar://$PROJECT_DIR$/' + os.path.relpath(library.get_path(True), suite.dir) + '!/'})
        libraryXml.close('CLASSES')
        libraryXml.element('JAVADOC')
        if library.sourcePath:
            libraryXml.open('SOURCES')
            libraryXml.element('root', attributes={'url': 'jar://$PROJECT_DIR$/' + os.path.relpath(library.get_source_path(True), suite.dir) + '!/'})
            libraryXml.close('SOURCES')
        else:
            libraryXml.element('SOURCES')
        libraryXml.close('library')
        libraryXml.close('component')

        libraryFile = join(librariesDirectory, library.name + '.xml')
        update_file(libraryFile, libraryXml.xml(indent='  ', newl='\n'))



    # Set annotation processor profiles up, and link them to modules in compiler.xml
    compilerXml = XMLDoc()
    compilerXml.open('project', attributes={'version': '4'})
    compilerXml.open('component', attributes={'name': 'CompilerConfiguration'})

    compilerXml.element('option', attributes={'name': "DEFAULT_COMPILER", 'value': 'Javac'})
    compilerXml.element('resourceExtensions')
    compilerXml.open('wildcardResourcePatterns')
    compilerXml.element('entry', attributes={'name': '!?*.java'})
    compilerXml.close('wildcardResourcePatterns')

    if annotationProcessorProfiles:
        compilerXml.open('annotationProcessing')
        for processors, modules in annotationProcessorProfiles.items():
            compilerXml.open('profile', attributes={'default': 'false', 'name': '-'.join(processors), 'enabled': 'true'})
            compilerXml.element('sourceOutputDir', attributes={'name': 'src_gen'})  # TODO use p.source_gen_dir() ?
            compilerXml.element('outputRelativeToContentRoot', attributes={'value': 'true'})
            compilerXml.open('processorPath', attributes={'useClasspath': 'false'})
            for apName in processors:
                pDep = dependency(apName)
                for entry in pDep.all_deps([], True):
                    if entry.isLibrary():
                        compilerXml.element('entry', attributes={'name': '$PROJECT_DIR$/' + os.path.relpath(entry.path, suite.dir)})
                    elif entry.isProject():
                        assert entry.isProject()
                        compilerXml.element('entry', attributes={'name': '$PROJECT_DIR$/' + os.path.relpath(entry.output_dir(), suite.dir)})
            compilerXml.close('processorPath')
            for module in modules:
                compilerXml.element('module', attributes={'name': module.name})
            compilerXml.close('profile')
        compilerXml.close('annotationProcessing')

    compilerXml.close('component')
    compilerXml.close('project')
    compilerFile = join(ideaProjectDirectory, 'compiler.xml')
    update_file(compilerFile, compilerXml.xml(indent='  ', newl='\n'))

    # Wite misc.xml for global JDK config
    miscXml = XMLDoc()
    miscXml.open('project', attributes={'version': '4'})
    miscXml.element('component', attributes={'name': 'ProjectRootManager', 'version': '2', 'languageLevel': _complianceToIntellijLanguageLevel(java().javaCompliance), 'project-jdk-name': str(java().javaCompliance), 'project-jdk-type': 'JavaSDK'})
    miscXml.close('project')
    miscFile = join(ideaProjectDirectory, 'misc.xml')
    update_file(miscFile, miscXml.xml(indent='  ', newl='\n'))


    # TODO look into copyright settings
    # TODO should add vcs.xml support

def ideclean(args):
    """remove all Eclipse and NetBeans project configurations"""
    def rm(path):
        if exists(path):
            os.remove(path)

    for s in suites():
        rm(join(s.mxDir, 'eclipse-config.zip'))
        rm(join(s.mxDir, 'netbeans-config.zip'))
        shutil.rmtree(join(s.dir, '.idea'), ignore_errors=True)

    for p in projects():
        if p.native:
            continue

        shutil.rmtree(join(p.dir, '.settings'), ignore_errors=True)
        shutil.rmtree(join(p.dir, '.externalToolBuilders'), ignore_errors=True)
        shutil.rmtree(join(p.dir, 'nbproject'), ignore_errors=True)
        rm(join(p.dir, '.classpath'))
        rm(join(p.dir, '.checkstyle'))
        rm(join(p.dir, '.project'))
        rm(join(p.dir, '.factorypath'))
        rm(join(p.dir, p.name + '.iml'))
        rm(join(p.dir, 'build.xml'))
        rm(join(p.dir, 'eclipse-build.xml'))
        try:
            rm(join(p.dir, p.name + '.jar'))
        except:
            log("Error removing {0}".format(p.name + '.jar'))

    for d in _dists.itervalues():
        if d.get_ide_project_dir():
            shutil.rmtree(d.get_ide_project_dir(), ignore_errors=True)

def ideinit(args, refreshOnly=False, buildProcessorJars=True):
    """(re)generate Eclipse, NetBeans and Intellij project configurations"""
    eclipseinit(args, refreshOnly=refreshOnly, buildProcessorJars=buildProcessorJars)
    netbeansinit(args, refreshOnly=refreshOnly, buildProcessorJars=buildProcessorJars)
    intellijinit(args, refreshOnly=refreshOnly)
    if not refreshOnly:
        fsckprojects([])

def fsckprojects(args):
    """find directories corresponding to deleted Java projects and delete them"""
    for suite in suites(True):
        projectDirs = [p.dir for p in suite.projects]
        for dirpath, dirnames, files in os.walk(suite.dir):
            if dirpath == suite.dir:
                # no point in traversing .hg or lib/
                dirnames[:] = [d for d in dirnames if d not in ['.hg', 'lib']]
            elif dirpath in projectDirs:
                # don't traverse subdirs of an existing project in this suite
                dirnames[:] = []
            else:
                projectConfigFiles = frozenset(['.classpath', 'nbproject'])
                indicators = projectConfigFiles.intersection(files)
                if len(indicators) != 0:
                    if not sys.stdout.isatty() or ask_yes_no(dirpath + ' looks like a removed project -- delete it', 'n'):
                        shutil.rmtree(dirpath)
                        log('Deleted ' + dirpath)

def javadoc(args, parser=None, docDir='javadoc', includeDeps=True, stdDoclet=True):
    """generate javadoc for some/all Java projects"""

    parser = ArgumentParser(prog='mx javadoc') if parser is None else parser
    parser.add_argument('-d', '--base', action='store', help='base directory for output')
    parser.add_argument('--unified', action='store_true', help='put javadoc in a single directory instead of one per project')
    parser.add_argument('--force', action='store_true', help='(re)generate javadoc even if package-list file exists')
    parser.add_argument('--projects', action='store', help='comma separated projects to process (omit to process all projects)')
    parser.add_argument('--Wapi', action='store_true', dest='warnAPI', help='show warnings about using internal APIs')
    parser.add_argument('--argfile', action='store', help='name of file containing extra javadoc options')
    parser.add_argument('--arg', action='append', dest='extra_args', help='extra Javadoc arguments (e.g. --arg @-use)', metavar='@<arg>', default=[])
    parser.add_argument('-m', '--memory', action='store', help='-Xmx value to pass to underlying JVM')
    parser.add_argument('--packages', action='store', help='comma separated packages to process (omit to process all packages)')
    parser.add_argument('--exclude-packages', action='store', help='comma separated packages to exclude')

    args = parser.parse_args(args)

    # build list of projects to be processed
    if args.projects is not None:
        candidates = [project(name) for name in args.projects.split(',')]
    else:
        candidates = projects_opt_limit_to_suites()

    # optionally restrict packages within a project
    packages = []
    if args.packages is not None:
        packages = [name for name in args.packages.split(',')]

    exclude_packages = []
    if args.exclude_packages is not None:
        exclude_packages = [name for name in args.exclude_packages.split(',')]

    def outDir(p):
        if args.base is None:
            return join(p.dir, docDir)
        return join(args.base, p.name, docDir)

    def check_package_list(p):
        return not exists(join(outDir(p), 'package-list'))

    def assess_candidate(p, projects):
        if p in projects:
            return False
        if args.force or args.unified or check_package_list(p):
            projects.append(p)
            return True
        return False

    projects = []
    for p in candidates:
        if not p.native:
            if includeDeps:
                deps = p.all_deps([], includeLibs=False, includeSelf=False)
                for d in deps:
                    assess_candidate(d, projects)
            if not assess_candidate(p, projects):
                logv('[package-list file exists - skipping {0}]'.format(p.name))


    def find_packages(sourceDirs, pkgs=None):
        if pkgs is None:
            pkgs = set()
        for sourceDir in sourceDirs:
            for root, _, files in os.walk(sourceDir):
                if len([name for name in files if name.endswith('.java')]) != 0:
                    pkg = root[len(sourceDir) + 1:].replace(os.sep, '.')
                    if len(packages) == 0 or pkg in packages:
                        if len(exclude_packages) == 0 or not pkg in exclude_packages:
                            pkgs.add(pkg)
        return pkgs

    extraArgs = [a.lstrip('@') for a in args.extra_args]
    if args.argfile is not None:
        extraArgs += ['@' + args.argfile]
    memory = '2g'
    if args.memory is not None:
        memory = args.memory
    memory = '-J-Xmx' + memory

    if not args.unified:
        for p in projects:
            # The project must be built to ensure javadoc can find class files for all referenced classes
            build(['--no-native', '--projects', p.name])

            pkgs = find_packages(p.source_dirs(), set())
            deps = p.all_deps([], includeLibs=False, includeSelf=False)
            links = ['-link', 'http://docs.oracle.com/javase/' + str(p.javaCompliance.value) + '/docs/api/']
            out = outDir(p)
            for d in deps:
                depOut = outDir(d)
                links.append('-link')
                links.append(os.path.relpath(depOut, out))
            cp = classpath(p.name, includeSelf=True)
            sp = os.pathsep.join(p.source_dirs())
            overviewFile = join(p.dir, 'overview.html')
            delOverviewFile = False
            if not exists(overviewFile):
                with open(overviewFile, 'w') as fp:
                    print >> fp, '<html><body>Documentation for the <code>' + p.name + '</code> project.</body></html>'
                delOverviewFile = True
            nowarnAPI = []
            if not args.warnAPI:
                nowarnAPI.append('-XDignore.symbol.file')

            # windowTitle onloy applies to the standard doclet processor
            windowTitle = []
            if stdDoclet:
                windowTitle = ['-windowtitle', p.name + ' javadoc']
            try:
                log('Generating {2} for {0} in {1}'.format(p.name, out, docDir))
                projectJava = java(p.javaCompliance)

                # Once https://bugs.openjdk.java.net/browse/JDK-8041628 is fixed,
                # this should be reverted to:
                # javadocExe = java().javadoc
                javadocExe = projectJava.javadoc

                run([javadocExe, memory,
                     '-XDignore.symbol.file',
                     '-classpath', cp,
                     '-quiet',
                     '-d', out,
                     '-overview', overviewFile,
                     '-sourcepath', sp,
                     '-source', str(projectJava.javaCompliance),
                     '-bootclasspath', projectJava.bootclasspath(),
                     '-extdirs', projectJava.extdirs()] +
                     ([] if projectJava.javaCompliance < JavaCompliance('1.8') else ['-Xdoclint:none']) +
                     links +
                     extraArgs +
                     nowarnAPI +
                     windowTitle +
                     list(pkgs))
                log('Generated {2} for {0} in {1}'.format(p.name, out, docDir))
            finally:
                if delOverviewFile:
                    os.remove(overviewFile)

    else:
        # The projects must be built to ensure javadoc can find class files for all referenced classes
        build(['--no-native'])

        pkgs = set()
        sp = []
        names = []
        for p in projects:
            find_packages(p.source_dirs(), pkgs)
            sp += p.source_dirs()
            names.append(p.name)

        links = ['-link', 'http://docs.oracle.com/javase/' + str(java().javaCompliance.value) + '/docs/api/']
        out = join(_primary_suite.dir, docDir)
        if args.base is not None:
            out = join(args.base, docDir)
        cp = classpath()
        sp = os.pathsep.join(sp)
        nowarnAPI = []
        if not args.warnAPI:
            nowarnAPI.append('-XDignore.symbol.file')
        log('Generating {2} for {0} in {1}'.format(', '.join(names), out, docDir))
        run([java().javadoc, memory,
             '-classpath', cp,
             '-quiet',
             '-d', out,
             '-sourcepath', sp] +
             ([] if java().javaCompliance < JavaCompliance('1.8') else ['-Xdoclint:none']) +
             links +
             extraArgs +
             nowarnAPI +
             list(pkgs))
        log('Generated {2} for {0} in {1}'.format(', '.join(names), out, docDir))

def site(args):
    """creates a website containing javadoc and the project dependency graph"""

    parser = ArgumentParser(prog='site')
    parser.add_argument('-d', '--base', action='store', help='directory for generated site', required=True, metavar='<dir>')
    parser.add_argument('--tmp', action='store', help='directory to use for intermediate results', metavar='<dir>')
    parser.add_argument('--name', action='store', help='name of overall documentation', required=True, metavar='<name>')
    parser.add_argument('--overview', action='store', help='path to the overview content for overall documentation', required=True, metavar='<path>')
    parser.add_argument('--projects', action='store', help='comma separated projects to process (omit to process all projects)')
    parser.add_argument('--jd', action='append', help='extra Javadoc arguments (e.g. --jd @-use)', metavar='@<arg>', default=[])
    parser.add_argument('--exclude-packages', action='store', help='comma separated packages to exclude', metavar='<pkgs>')
    parser.add_argument('--dot-output-base', action='store', help='base file name (relative to <dir>/all) for project dependency graph .svg and .jpg files generated by dot (omit to disable dot generation)', metavar='<path>')
    parser.add_argument('--title', action='store', help='value used for -windowtitle and -doctitle javadoc args for overall documentation (default: "<name>")', metavar='<title>')
    args = parser.parse_args(args)

    args.base = os.path.abspath(args.base)
    tmpbase = args.tmp if args.tmp else  tempfile.mkdtemp(prefix=basename(args.base) + '.', dir=dirname(args.base))
    unified = join(tmpbase, 'all')

    exclude_packages_arg = []
    if args.exclude_packages is not None:
        exclude_packages_arg = ['--exclude-packages', args.exclude_packages]

    projects = sorted_deps()
    projects_arg = []
    if args.projects is not None:
        projects_arg = ['--projects', args.projects]
        projects = [project(name) for name in args.projects.split(',')]

    extra_javadoc_args = []
    for a in args.jd:
        extra_javadoc_args.append('--arg')
        extra_javadoc_args.append('@' + a)

    try:
        # Create javadoc for each project
        javadoc(['--base', tmpbase] + exclude_packages_arg + projects_arg + extra_javadoc_args)

        # Create unified javadoc for all projects
        with open(args.overview) as fp:
            content = fp.read()
            idx = content.rfind('</body>')
            if idx != -1:
                args.overview = join(tmpbase, 'overview_with_projects.html')
                with open(args.overview, 'w') as fp2:
                    print >> fp2, content[0:idx]
                    print >> fp2, """<div class="contentContainer">
<table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" summary="Projects table">
<caption><span>Projects</span><span class="tabEnd">&nbsp;</span></caption>
<tr><th class="colFirst" scope="col">Project</th><th class="colLast" scope="col">&nbsp;</th></tr>
<tbody>"""
                    color = 'row'
                    for p in projects:
                        print >> fp2, '<tr class="{1}Color"><td class="colFirst"><a href="../{0}/javadoc/index.html",target = "_top">{0}</a></td><td class="colLast">&nbsp;</td></tr>'.format(p.name, color)
                        color = 'row' if color == 'alt' else 'alt'

                    print >> fp2, '</tbody></table></div>'
                    print >> fp2, content[idx:]

        title = args.title if args.title is not None else args.name
        javadoc(['--base', tmpbase,
                 '--unified',
                 '--arg', '@-windowtitle', '--arg', '@' + title,
                 '--arg', '@-doctitle', '--arg', '@' + title,
                 '--arg', '@-overview', '--arg', '@' + args.overview] + exclude_packages_arg + projects_arg + extra_javadoc_args)

        if exists(unified):
            shutil.rmtree(unified)
        os.rename(join(tmpbase, 'javadoc'), unified)

        # Generate dependency graph with Graphviz
        if args.dot_output_base is not None:
            dotErr = None
            try:
                if not 'version' in subprocess.check_output(['dot', '-V'], stderr=subprocess.STDOUT):
                    dotErr = 'dot -V does not print a string containing "version"'
            except subprocess.CalledProcessError as e:
                dotErr = 'error calling "dot -V": {}'.format(e)
            except OSError as e:
                dotErr = 'error calling "dot -V": {}'.format(e)

            if dotErr != None:
                abort('cannot generate dependency graph: ' + dotErr)

            dot = join(tmpbase, 'all', str(args.dot_output_base) + '.dot')
            svg = join(tmpbase, 'all', str(args.dot_output_base) + '.svg')
            jpg = join(tmpbase, 'all', str(args.dot_output_base) + '.jpg')
            html = join(tmpbase, 'all', str(args.dot_output_base) + '.html')
            with open(dot, 'w') as fp:
                dim = len(projects)
                print >> fp, 'digraph projects {'
                print >> fp, 'rankdir=BT;'
                print >> fp, 'size = "' + str(dim) + ',' + str(dim) + '";'
                print >> fp, 'node [shape=rect, fontcolor="blue"];'
                # print >> fp, 'edge [color="green"];'
                for p in projects:
                    print >> fp, '"' + p.name + '" [URL = "../' + p.name + '/javadoc/index.html", target = "_top"]'
                    for dep in p.canonical_deps():
                        if dep in [proj.name for proj in projects]:
                            print >> fp, '"' + p.name + '" -> "' + dep + '"'
                depths = dict()
                for p in projects:
                    d = p.max_depth()
                    depths.setdefault(d, list()).append(p.name)
                print >> fp, '}'

            run(['dot', '-Tsvg', '-o' + svg, '-Tjpg', '-o' + jpg, dot])

            # Post-process generated SVG to remove title elements which most browsers
            # render as redundant (and annoying) tooltips.
            with open(svg, 'r') as fp:
                content = fp.read()
            content = re.sub('<title>.*</title>', '', content)
            content = re.sub('xlink:title="[^"]*"', '', content)
            with open(svg, 'w') as fp:
                fp.write(content)

            # Create HTML that embeds the svg file in an <object> frame
            with open(html, 'w') as fp:
                print >> fp, '<html><body><object data="{}.svg" type="image/svg+xml"></object></body></html>'.format(args.dot_output_base)

        if exists(args.base):
            shutil.rmtree(args.base)
        if args.tmp:
            shutil.copytree(tmpbase, args.base)
        else:
            shutil.move(tmpbase, args.base)

        print 'Created website - root is ' + join(args.base, 'all', 'index.html')

    finally:
        if not args.tmp and exists(tmpbase):
            shutil.rmtree(tmpbase)

def _kwArg(kwargs):
    if len(kwargs) > 0:
        return kwargs.pop(0)
    return None

def findclass(args, logToConsole=True, matcher=lambda string, classname: string in classname):
    """find all classes matching a given substring"""
    matches = []
    for entry, filename in classpath_walk(includeBootClasspath=True):
        if filename.endswith('.class'):
            if isinstance(entry, zipfile.ZipFile):
                classname = filename.replace('/', '.')
            else:
                classname = filename.replace(os.sep, '.')
            classname = classname[:-len('.class')]
            for a in args:
                if matcher(a, classname):
                    matches.append(classname)
                    if logToConsole:
                        log(classname)
    return matches

def select_items(items, descriptions=None, allowMultiple=True):
    """
    Presents a command line interface for selecting one or more (if allowMultiple is true) items.

    """
    if len(items) <= 1:
        return items
    else:
        if allowMultiple:
            log('[0] <all>')
        for i in range(0, len(items)):
            if descriptions is None:
                log('[{0}] {1}'.format(i + 1, items[i]))
            else:
                assert len(items) == len(descriptions)
                wrapper = textwrap.TextWrapper(subsequent_indent='    ')
                log('\n'.join(wrapper.wrap('[{0}] {1} - {2}'.format(i + 1, items[i], descriptions[i]))))
        while True:
            if allowMultiple:
                s = raw_input('Enter number(s) of selection (separate multiple choices with spaces): ').split()
            else:
                s = [raw_input('Enter number of selection: ')]
            try:
                s = [int(x) for x in s]
            except:
                log('Selection contains non-numeric characters: "' + ' '.join(s) + '"')
                continue

            if allowMultiple and 0 in s:
                return items

            indexes = []
            for n in s:
                if n not in range(1, len(items) + 1):
                    log('Invalid selection: ' + str(n))
                    continue
                else:
                    indexes.append(n - 1)
            if allowMultiple:
                return [items[i] for i in indexes]
            if len(indexes) == 1:
                return items[indexes[0]]
            return None

def exportlibs(args):
    """export libraries to an archive file"""

    parser = ArgumentParser(prog='exportlibs')
    parser.add_argument('-b', '--base', action='store', help='base name of archive (default: libs)', default='libs', metavar='<path>')
    parser.add_argument('-a', '--include-all', action='store_true', help="include all defined libaries")
    parser.add_argument('--arc', action='store', choices=['tgz', 'tbz2', 'tar', 'zip'], default='tgz', help='the type of the archive to create')
    parser.add_argument('--no-sha1', action='store_false', dest='sha1', help='do not create SHA1 signature of archive')
    parser.add_argument('--no-md5', action='store_false', dest='md5', help='do not create MD5 signature of archive')
    parser.add_argument('--include-system-libs', action='store_true', help='include system libraries (i.e., those not downloaded from URLs)')
    parser.add_argument('extras', nargs=REMAINDER, help='extra files and directories to add to archive', metavar='files...')
    args = parser.parse_args(args)

    def createArchive(addMethod):
        entries = {}
        def add(path, arcname):
            apath = os.path.abspath(path)
            if not entries.has_key(arcname):
                entries[arcname] = apath
                logv('[adding ' + path + ']')
                addMethod(path, arcname=arcname)
            elif entries[arcname] != apath:
                logv('[warning: ' + apath + ' collides with ' + entries[arcname] + ' as ' + arcname + ']')
            else:
                logv('[already added ' + path + ']')

        libsToExport = set()
        if args.include_all:
            for lib in _libs.itervalues():
                libsToExport.add(lib)
        else:
            def isValidLibrary(dep):
                if dep in _libs.iterkeys():
                    lib = _libs[dep]
                    if len(lib.urls) != 0 or args.include_system_libs:
                        return lib
                return None

            # iterate over all project dependencies and find used libraries
            for p in _projects.itervalues():
                for dep in p.deps:
                    r = isValidLibrary(dep)
                    if r:
                        libsToExport.add(r)

            # a library can have other libraries as dependency
            size = 0
            while size != len(libsToExport):
                size = len(libsToExport)
                for lib in libsToExport.copy():
                    for dep in lib.deps:
                        r = isValidLibrary(dep)
                        if r:
                            libsToExport.add(r)

        for lib in libsToExport:
            add(lib.get_path(resolve=True), lib.path)
            if lib.sha1:
                add(lib.get_path(resolve=True) + ".sha1", lib.path + ".sha1")
            if lib.sourcePath:
                add(lib.get_source_path(resolve=True), lib.sourcePath)
                if lib.sourceSha1:
                    add(lib.get_source_path(resolve=True) + ".sha1", lib.sourcePath + ".sha1")

        if args.extras:
            for e in args.extras:
                if os.path.isdir(e):
                    for root, _, filenames in os.walk(e):
                        for name in filenames:
                            f = join(root, name)
                            add(f, f)
                else:
                    add(e, e)

    if args.arc == 'zip':
        path = args.base + '.zip'
        with zipfile.ZipFile(path, 'w') as zf:
            createArchive(zf.write)
    else:
        path = args.base + '.tar'
        mode = 'w'
        if args.arc != 'tar':
            sfx = args.arc[1:]
            mode = mode + ':' + sfx
            path = path + '.' + sfx
        with tarfile.open(path, mode) as tar:
            createArchive(tar.add)
    log('created ' + path)

    def digest(enabled, path, factory, suffix):
        if enabled:
            d = factory()
            with open(path, 'rb') as f:
                while True:
                    buf = f.read(4096)
                    if not buf:
                        break
                    d.update(buf)
            with open(path + '.' + suffix, 'w') as fp:
                print >> fp, d.hexdigest()
            log('created ' + path + '.' + suffix)

    digest(args.sha1, path, hashlib.sha1, 'sha1')
    digest(args.md5, path, hashlib.md5, 'md5')

def javap(args):
    """disassemble classes matching given pattern with javap"""

    javapExe = java().javap
    if not exists(javapExe):
        abort('The javap executable does not exists: ' + javapExe)
    else:
        candidates = findclass(args, logToConsole=False)
        if len(candidates) == 0:
            log('no matches')
        selection = select_items(candidates)
        run([javapExe, '-private', '-verbose', '-classpath', classpath()] + selection)

def show_projects(args):
    """show all loaded projects"""
    for s in suites():
        projectsFile = join(s.mxDir, 'projects')
        if exists(projectsFile):
            log(projectsFile)
            for p in s.projects:
                log('\t' + p.name)

def ask_yes_no(question, default=None):
    """"""
    assert not default or default == 'y' or default == 'n'
    if not sys.stdout.isatty():
        if default:
            return default
        else:
            abort("Can not answer '" + question + "?' if stdout is not a tty")
    questionMark = '? [yn]: '
    if default:
        questionMark = questionMark.replace(default, default.upper())
    answer = raw_input(question + questionMark) or default
    while not answer:
        answer = raw_input(question + questionMark)
    return answer.lower().startswith('y')

def add_argument(*args, **kwargs):
    """
    Define how a single command-line argument.
    """
    assert _argParser is not None
    _argParser.add_argument(*args, **kwargs)

def update_commands(suite, new_commands):
    for key, value in new_commands.iteritems():
        if _commands.has_key(key):
            warn("redefining command '" + key + "' in suite " + suite.name)
        _commands[key] = value

def warn(msg):
    if _warn:
        print 'WARNING: ' + msg

# Table of commands in alphabetical order.
# Keys are command names, value are lists: [<function>, <usage msg>, <format args to doc string of function>...]
# If any of the format args are instances of Callable, then they are called with an 'env' are before being
# used in the call to str.format().
# Suite extensions should not update this table directly, but use update_commands
_commands = {
    'about': [about, ''],
    'build': [build, '[options]'],
    'checkstyle': [checkstyle, ''],
    'canonicalizeprojects': [canonicalizeprojects, ''],
    'clean': [clean, ''],
    'eclipseinit': [eclipseinit, ''],
    'eclipseformat': [eclipseformat, ''],
    'exportlibs': [exportlibs, ''],
    'findclass': [findclass, ''],
    'fsckprojects': [fsckprojects, ''],
    'help': [help_, '[command]'],
    'ideclean': [ideclean, ''],
    'ideinit': [ideinit, ''],
    'intellijinit': [intellijinit, ''],
    'archive': [_archive, '[options]'],
    'projectgraph': [projectgraph, ''],
    'pylint': [pylint, ''],
    'javap': [javap, '<class name patterns>'],
    'javadoc': [javadoc, '[options]'],
    'site': [site, '[options]'],
    'netbeansinit': [netbeansinit, ''],
    'projects': [show_projects, ''],
}

_argParser = ArgParser()

def _suitename(mxDir):
    base = os.path.basename(mxDir)
    parts = base.split('.')
    # temporary workaround until mx.graal exists
    if len(parts) == 1:
        return 'graal'
    else:
        return parts[1]

def _is_suite_dir(d, mxDirName=None):
    """
    Checks if d contains a suite.
    If mxDirName is None, matches any suite name, otherwise checks for exactly that suite.
    """
    if os.path.isdir(d):
        for f in os.listdir(d):
            if (mxDirName == None and (f == 'mx' or fnmatch.fnmatch(f, 'mx.*'))) or f == mxDirName:
                mxDir = join(d, f)
                if exists(mxDir) and isdir(mxDir) and exists(join(mxDir, 'projects')):
                    return mxDir

def _check_primary_suite():
    if _primary_suite is None:
        abort('no primary suite found')
    else:
        return _primary_suite

def _findPrimarySuiteMxDirFrom(d):
    """ search for a suite directory upwards from 'd' """
    while d:
        mxDir = _is_suite_dir(d)
        if mxDir is not None:
            return mxDir
        parent = dirname(d)
        if d == parent:
            return None
        d = parent

    return None

def _findPrimarySuiteMxDir():
    # check for explicit setting
    if _primary_suite_path is not None:
        mxDir = _is_suite_dir(_primary_suite_path)
        if mxDir is not None:
            return mxDir
        else:
            abort(_primary_suite_path + ' does not contain an mx suite')

    # try current working directory first
    mxDir = _findPrimarySuiteMxDirFrom(os.getcwd())
    if mxDir is not None:
        return mxDir
    # backwards compatibility: search from path of this file
    return _findPrimarySuiteMxDirFrom(dirname(__file__))

def main():
    primarySuiteMxDir = _findPrimarySuiteMxDir()
    if primarySuiteMxDir:
        global _primary_suite
        _primary_suite = _loadSuite(primarySuiteMxDir, True)
    else:
        abort('no primary suite found')

    opts, commandAndArgs = _argParser._parse_cmd_line()

    global _opts, _java_homes
    _opts = opts
    defaultJdk = JavaConfig(opts.java_home, opts.java_dbg_port)
    _java_homes = [defaultJdk]
    if opts.extra_java_homes:
        for java_home in opts.extra_java_homes.split(os.pathsep):
            extraJdk = JavaConfig(java_home, opts.java_dbg_port)
            if extraJdk > defaultJdk:
                abort('Secondary JDK ' + extraJdk.jdk + ' has higher compliance level than default JDK ' + defaultJdk.jdk)
            _java_homes.append(extraJdk)

    for s in suites():
        s._post_init(opts)

    if len(commandAndArgs) == 0:
        _argParser.print_help()
        return

    command = commandAndArgs[0]
    command_args = commandAndArgs[1:]

    if not _commands.has_key(command):
        hits = [c for c in _commands.iterkeys() if c.startswith(command)]
        if len(hits) == 1:
            command = hits[0]
        elif len(hits) == 0:
            abort('mx: unknown command \'{0}\'\n{1}use "mx help" for more options'.format(command, _format_commands()))
        else:
            abort('mx: command \'{0}\' is ambiguous\n    {1}'.format(command, ' '.join(hits)))

    c, _ = _commands[command][:2]
    def term_handler(signum, frame):
        abort(1)
    signal.signal(signal.SIGTERM, term_handler)

    def quit_handler(signum, frame):
        _send_sigquit()
    if get_os() != 'windows':
        signal.signal(signal.SIGQUIT, quit_handler)

    try:
        if opts.timeout != 0:
            def alarm_handler(signum, frame):
                abort('Command timed out after ' + str(opts.timeout) + ' seconds: ' + ' '.join(commandAndArgs))
            signal.signal(signal.SIGALRM, alarm_handler)
            signal.alarm(opts.timeout)
        retcode = c(command_args)
        if retcode is not None and retcode != 0:
            abort(retcode)
    except KeyboardInterrupt:
        # no need to show the stack trace when the user presses CTRL-C
        abort(1)

version = VersionSpec("1.0")

currentUmask = None

if __name__ == '__main__':
    # rename this module as 'mx' so it is not imported twice by the commands.py modules
    sys.modules['mx'] = sys.modules.pop('__main__')

    # Capture the current umask since there's no way to query it without mutating it.
    currentUmask = os.umask(0)
    os.umask(currentUmask)

    main()
