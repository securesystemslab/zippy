#! /usr/bin/env python
"""This script is similar to Maxine's "max" script. It enables us to quickly execute
the MBS interpreter on top of Maxine or the system default JVM.
"""
import os, sys, time
import fnmatch

from optparse import OptionParser
import sys
from pprint import pprint
from itertools import product
from measure import measure  # # is from the computer language shootout game...
from tempfile import mkstemp
from collections import defaultdict
import shlex
import datetime
import pickle
from math import *
import shutil
import csv
import numpy
import glob
import subprocess

# pip install scipy is currently broken on Lion
# import scipy.stats
import benchmark

def get_env():
    """This function returns a dictionary that contains the actual values for environment
    variables. This is platform specific (since we are relying on the platform identifier
    in Maxine's build directory. Currently, Linux [verified on Ubuntu 10.04+] and MacOS X
    are supported.
    NOTE: This function is not supposed to be called directly, rather use one of the pre-
    defined access functions that bind the environment and the key as a default argument,
    e.g.: get_graal_executable().
    """
    platform = sys.platform  # # returns 'darwin' on macosx, 'linux2' on ubuntu 10.04
    if platform == 'linux2':
        platform = 'linux'

    currentDir = os.path.abspath('.')
    graalVMDir = os.path.abspath('../../..')
    jdkDirs = glob.glob(graalVMDir + '/jdk1.7*')
    # assert len(jdkDirs) == 1, "there are more than one JDK directories"
    numOfJDKDirs = len(jdkDirs)
    graalVMExe = jdkDirs[numOfJDKDirs - 1] + "/product/bin/java -graal "

    jythonProjectPath = ".."
    jythonScriptsPath = currentDir + "/benchmarks"

    # Jython stuff
    jythonDir = os.path.join(graalVMDir, 'graal/zippy')
    jythonDist = os.path.join(jythonDir, 'dist')
    jythonDistJavaLib = os.path.join(jythonDist, 'javalib')

    classpath = []

    if os.path.exists(jythonDistJavaLib):
        os.chdir(jythonDistJavaLib)
        classpath = [os.path.join(jythonDistJavaLib, cp) for cp in glob.glob('*.jar')]
        os.chdir(jythonDist)
        jythonJarFile = os.path.join(jythonDist, glob.glob('jython-dev.jar')[0])
        classpath.append(jythonJarFile)
        os.chdir(currentDir)

    # truffle
    truffle_dir = os.path.join(graalVMDir, 'graal/com.oracle.truffle.api')
    truffle_api_jar = os.path.join(truffle_dir, 'com.oracle.truffle.api.jar')
    truffle_codegen_dir = os.path.join(graalVMDir, 'graal/com.oracle.truffle.api.codegen')
    truffle_codegen_api_jar = os.path.join(truffle_codegen_dir, 'com.oracle.truffle.api.codegen.jar')

    classpath.append(truffle_api_jar)
    classpath.append(truffle_codegen_api_jar)

    # Options for remote debugging
    debug_option = "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y "

    return {
        'executable' : graalVMExe,
        'prj'        : jythonProjectPath,
        'scripts'    : jythonScriptsPath,
        'cp'         : classpath,
        'current'    : currentDir,
        'debug_option' : debug_option,
        }

def get_graal_executable(env=get_env(), key='executable'):
    return env[key]

def get_graal_prj(env=get_env(), key='prj'):
    return env[key]

def get_jython_scripts(env=get_env(), key='scripts'):
    return env[key]

def get_jython_classpath(env=get_env(), key='cp'):
    return ':'.join(env[key])

def get_cwd(env=get_env(), key='current'):
    return env[key]

def get_debug_option(env=get_env(), key='debug_option'):
    return env[key]

def get_unmarshal_script():
    return os.path.join(get_jython_scripts(), 'unmarshall.py')

def get_jython_classname():
    return "org.python.util.jython"

def check_config():
    """This function verifies that some paths exist in the current configuration. If
    it fails, it reports an error to the caller and exits.
    """
    rules = [
        (get_graal_prj(), "Invalid project path"),
        (get_jython_scripts(), "Invalid scripts path"),
        ]
    for (p, errmsg) in rules:
        if not os.path.exists(p):
            exit("%s: %s" % (errmsg, p))


def parse_cmd_line_args():
    """This defines the command line arguments supported by our tool.
    """
    p = OptionParser()
    p.add_option("-o", "--host",            action="store", dest="host", default="", help="options to pass to the Host VM")
    p.add_option("-O", "--client",          action="store", dest="client", default="", help="options to pass to the Client VM")
    p.add_option("-x", "--execbench",       action="store", dest="execute", default="", help="executes a specified script: {" + ", ".join(os.listdir(get_jython_scripts())) + "}")
    p.add_option("-r", "--run",             action="store_true", dest="run", default=False, help="run all scripts")
    p.add_option("-g", "--graal",           action="store_true", dest="graal", default=False, help="run on top of GraalVM")
    p.add_option("-u", "--unmarshall",      action="store_true", dest="unmarshall", default=False, help="Unmarshall a .pyc file and run in on bytecode interpreter")
    p.add_option("-z", "--bench",           action="store", dest="bench", type='int', default=0, help="run benchmarks and print results")
    p.add_option("-d", "--debug",           action="store_true", dest="debug", default=False, help="option to debug remotely from eclipse")
    p.add_option("-t", "--test, ",          action="store", dest="test", default="", help="run unit test")
    p.add_option("-X", "--execute",         action="store", dest="execspecific", default="", help="executes a specified script")
    p.add_option("-R", "--runfolder",       action="store", dest="runfolder", default="", help="run all scripts on the provided folder using .run file")
    p.add_option("-c", "--check",           action="store", dest="checking", default="", help="check output correctness with python 3")
    p.add_option("-s", "--checksingle",     action="store", dest="checksingle", default="", help="check output correctness of single file with python 3")
    p.add_option("-w", "--checkwexist",     action="store", dest="checkwexist", default="", help="check output correctness with an already produced results")
    p.add_option("-p", "--produceresult",     action="store", dest="produceresult", default="", help="produced results to use in future testing")

    return p.parse_args()

def get_exec(cmd_class=get_jython_classname(), action_args=[]):
    """This function returns a closure that holds partially bound arguments for executing
    programs using the "os.system" invocation function. The closure is used so that we can
    late bind arguments depending on the passed command line options. Furthermore, the when
    we execute the closure it will keep track of the exit codes and populate the corresponding
    "errors" list.
    """
    def exec_closure(args=[], host_vm_opts='', client_vm_opts='', use_graal=False, errors=[]):
        executable = get_graal_executable() if use_graal else 'java'

        if opts.unmarshall:
            arguments = rearrange_arguments_for_unmarshalling(action_args)
        else:
            arguments = ' '.join(action_args + args)

        if opts.debug:
            host_vm_opts += get_debug_option()

        command = "%s %s -cp %s %s %s %s" % (executable, host_vm_opts, get_jython_classpath(),
                                         cmd_class, client_vm_opts, arguments)
        #print executable
        #print get_jython_classpath()
        # print cmd_class
        
        # print "Executing:", command
        exit_code = os.system(command)
#         print "Exit Code:", exit_code

        if exit_code:
            errors.append((executable, host_vm_opts, client_vm_opts, cmd_class, arguments))

    return exec_closure


def test_exec(cmd_class=get_jython_classname(), action_args=[]):
    """This function returns a closure that holds partially bound arguments for executing
    programs using the "os.system" invocation function. The closure is used so that we can
    late bind arguments depending on the passed command line options. Furthermore, the when
    we execute the closure it will keep track of the exit codes and populate the corresponding
    "errors" list.
    """
    def exec_closure(args=[], host_vm_opts='', client_vm_opts='', use_graal=False, errors=[]):
        executable = get_graal_executable() if use_graal else 'java'


        if opts.debug:
            host_vm_opts += get_debug_option()

        if action_args[0] == "all":
            command = "%s %s -cp %s org.junit.runner.JUnitCore truffle.test.test" % (executable,
                                             host_vm_opts,
                                             get_jython_classpath())
        else:
            command = "%s %s -cp %s org.junit.runner.JUnitCore truffle.test.%s" % (executable,
                                         host_vm_opts,
                                         get_jython_classpath(), action_args[0])
                                         
                                        
        # print executable
        # print get_jython_classpath()
        # print cmd_class
        
        # print "Executing:", command
        exit_code = os.system(command)
        # if exit_code:
            # errors.append( (executable, host_vm_opts, client_vm_opts, cmd_class) )

    return exec_closure

def rearrange_arguments_for_unmarshalling(arguments):
    """
    action_args += 'c'
    special trick to keep sys.argv[1] in the unmarshalled scripts to
    be the real argument we want to pass to the benchmark not the
    bootstrapping /  unmarshalling wrapper script
    also that .py is patched to be .pyc in the script name
    """
    rearranged_action_args = ''.join(arguments)
    rearranged_action_args_list = rearranged_action_args.split(' ')
    rearranged_action_args_list.reverse()
    arguments = ' '.join(args + rearranged_action_args_list) + 'c'
    unmarshalScript = get_unmarshal_script()
    arguments = get_unmarshal_script() + ' ' + arguments
    return arguments

def enum_scripts(filter_fn=lambda x: True, script_path=get_jython_scripts()):
    """This function is used to enumerate the scripts found in the script path. At the
    same time you can supply a custom "filter_fn" function to filter out unwanted entries
    from the input directory.
    NOTE: If this function finds a file called ".mbsignore" in the directory specified by
    the named "script_path" argument, it will exclude all files listed in there.
    """
    files_to_run = "%s/.run" % (script_path)

    if not os.path.exists(files_to_run):
        exit(files_to_run + " does not exist")

    with open(files_to_run) as input:
        for s in input:
            if filter_fn(s):
                script = os.path.join(script_path, s.strip())
                yield script

def run_gn(script_path=os.path.abspath(get_jython_scripts())):
    for s in enum_scripts(filter_fn=lambda x: not x.startswith('#')):
        if opts.unmarshall:
            print "Unmarshalling ... %s" % (s)
        else:
            print "Running ... %s" % (s)

        yield get_exec(action_args=[s])

def expand_file_information(tuples):
    """This function takes in a list of tuples as generated by "run_bmarks_gn" and
    expands the information stored within the temporary files generated by "run_bmarks_gn".
    The protocol is that the last line contains the running time of the benchmark as calculated
    in the benchmark (this is rather fuzzy though, as it is still in the interpreter and should
    actually be measured on the virtual machine level; probably a more accurate measurement
    technique would be to measure whole process execution as stored in the measure record "r" and
    subtract the VM start-up time.). All other information present in the file can be used for
    verification purposes, i.e., the results can later be compared to the outputs of other
    interpreters.
    (NOTE: printing to stdout is going to make most of the benchmark measurements useless)

    Finally, we generate a new list of tuples, with the information conveniently expanded
    for use by other functions.
    """
    results = []
    for (native, host, s, path, r) in tuples:
        with open(path) as f:
            lines = f.readlines()
            try:
                # if last line is time, the second last line is also ignored for the timer label
                prog_output, prog_runtime = lines[:-2], float(lines[-1])
                results.append((native, host, s, prog_runtime, prog_output, path, r))
            except:
                # bypass time extraction and carry on
                # if last line is not time, all lines are taken
                prog_output, prog_runtime = lines, float(0)
                results.append((native, host, s, prog_runtime, prog_output, path, r))

    return results

def escape_r(v):
    if v == None:
        return "NA"
    try:
        return str(float(v))
    except:
        return "\"" + str(v) + "\""

def run_bmarks_gn(script_path=os.path.abspath(get_jython_scripts())):
    # set num_of_runs in benchmark
    benchmark.num_of_runs = opts.bench
    
    native_vm_runtimes = [
          ("hotspot", "java")
          , ("hotspot", "java")
#          ("graal", get_graal_executable() + '')
#          , ("graal", get_graal_executable() + '')
        ]

    host_vms = [
          ("jython", "org.python.util.jython -trun", ""),
          ("thon", "org.python.util.jython -interpretast -specialize -optimizenode -trun", "")
          # , ("jython", "org.python.util.jython -tcompile", "")
          # , ("bi", "org.python.util.jython -tinterpret " + os.path.join(get_jython_scripts(), 'unmarshall.py'), "unmarshal")
#          ("si", "org.python.util.jython -si -tinterpret " + os.path.join(get_jython_scripts(), 'unmarshall.py'), "unmarshal")
          # ("di", "org.python.util.jython -di -tinterpret " + os.path.join(get_jython_scripts(), 'unmarshall.py'), "unmarshal")
#          , ("oi", "org.python.util.jython -oi -tinterpret " + os.path.join(get_jython_scripts(), 'unmarshall.py'), "unmarshal")
        ]

    results = []
    try:
        index = 0
        with open('/dev/null', 'w') as throwaway:
            for (native, host) in zip(native_vm_runtimes, host_vms):
                print "Benchmarking %s on %s" % (host, native)
                for s in enum_scripts(filter_fn=lambda x: not x.startswith('#')):
                    job = s
                    if host[2] == "unmarshal":
                        # reverse the job string when unmarshalling
                        script_list = s.split(' ')
                        script_list.reverse()
                        job = ' '.join(script_list) + 'c'
                        print "\tunmarshalling %35s" % (job.split(' ')[0] + ' ' + job.split('/')[-1]),
                    else:
                        print "\trunning %35s" % (job.split('/')[-1]),


                    cmd_line = "%s -cp %s %s %s" % (native[1],
                                                   get_jython_classpath(),
                                                   host[1],
                                                   job)
                    cmd = shlex.split(cmd_line)

                    print " -- ",
                    for iteration in range(0, opts.bench):
                        print "%d," % iteration,
                        sys.stdout.flush()
                        retry = True
                        while retry:
                            (output, path) = mkstemp(prefix="mbs")  # #prefix="%s-%s-%s" % (native, host, s))
                            result = measure(index, cmd, 0, 1000000, outFile=throwaway, errFile=output)
                            os.close(output)
                            if result.isOkay():
                                results.append((native, host, s, path, result))
                                index += 1
                                retry = False
                            else:
                                print "(faulted; retrying)",

                    print
        print "Gathering data complete..."
        results = expand_file_information(results)

        print "Saving results to file ..."
        with open("res.out", 'w') as output:
            output.write(pickle.dumps(results))
    except:
        if os.path.exists("res.out"):
            print "Loading results from file ..."
            with open("res.out", 'r') as input:
                results = pickle.loads(input.read())

    benchmark.process(results, host_vms)
    return []

def execute_script(script_filename, script_path=os.path.abspath(get_jython_scripts())):
    """This function returns another closure so that we can bind the name of the script
    to be executed ("script_filename") to the execution closure. This is necessary because
    this filename comes from the OptionParser and needs to be made available to the
    execution closure in one way or another.
    """
    def closure():
        path = "%s/%s" % (script_path, script_filename)
        if not os.path.exists(path):
            exit('Specified script does not exist: %s' % path)

        yield get_exec(action_args=[path])
    return closure

def execute_test(script_filename, script_path=os.path.abspath(get_jython_scripts())):
    """This function returns another closure so that we can bind the name of the script
    to be executed ("script_filename") to the execution closure. This is necessary because
    this filename comes from the OptionParser and needs to be made available to the
    execution closure in one way or another.
    """
    def closure():
        # path= "%s/%s" % (script_path, script_filename)
        # if not os.path.exists(path):
            # exit('Specified script does not exist: %s' % path)
        yield test_exec(action_args=[script_filename])
    return closure

def run_gn_folder(script_path):
    
    def closure():
        for s in enum_scripts(filter_fn=lambda x: not x.startswith('#'), script_path=script_path):
            if opts.unmarshall:
                print "Unmarshalling ... %s" % (s)
            else:
                print "Running ... %s" % (s)
    
            yield get_exec(action_args=[s])
    return closure

def get_exec_check(cmd_class=get_jython_classname(), action_args=[]): 
    
    def exec_closure(args=[], host_vm_opts='', client_vm_opts='', use_graal=False, errors=[]):
#         print "%-45s" % action_args[0],
        
        if(len(args) == 0):
            args = ["5"]
            client_vm_opts = "-interpretast -specialize"
        
        executable = get_graal_executable() if use_graal else 'java'
        arguments = ' '.join(action_args + args)

        if opts.debug:
            host_vm_opts += get_debug_option()
        error_log = action_args[0].replace(".py", "_error.log")
        command = "%s %s -cp %s %s %s %s 2> %s" % (executable, host_vm_opts, get_jython_classpath(),
                                         cmd_class, client_vm_opts, arguments, error_log)
        outputResult = os.popen(command).read()
        bool_pass_or_fail = False
        statinfo = os.stat(error_log)
        python3outputResult = ""
        
        if (statinfo.st_size == 0):
            os.popen(command).read()
            if(len(args) == 2):
                command = "%s %s %s 2> /dev/null" % (args[1] , action_args[0], args[0])
            else:
                command = "python3.3 %s %s 2> /dev/null" % (action_args[0], args[0])
#             print command
            python3outputResult = os.popen(command).read()
            if(cmp(outputResult,python3outputResult) == 0):
                bool_pass_or_fail = True
            
#         print bool_pass_or_fail
        if (bool_pass_or_fail): 
            pass_or_fail = "\033[92mPASS\033[0m"
            pass_or_fail_note = ""
            command = "rm %s" % error_log
            os.popen(command).read()        
        else:
            pass_or_fail = "\033[91mFAIL\033[0m"
            pass_or_fail_note = "Please See %s for details" % error_log
            errorLogFile = open(error_log, "rw+")
            errorLogFile.write("\nPython output:\n")
            errorLogFile.write(python3outputResult)
            errorLogFile.write("\nJython output:\n")
            errorLogFile.write(outputResult)
        
        print "[%s] %s" % (pass_or_fail, pass_or_fail_note)
    return exec_closure

def execute_single(script_filename):
    """
    
    """
    def closure():
#         path = "%s" % (script_path, script_filename)

        if not os.path.exists(script_filename):
            exit('Specified script does not exist: %s' % script_filename)

        yield get_exec(action_args=[script_filename])
    return closure

def check_single(script_filename):
    """
    
    """
    def closure():

        if not os.path.exists(script_filename):
            exit('Specified script does not exist: %s' % script_filename)
        
        sys.stdout.write("%-45s" % script_filename)
        yield get_exec_check(action_args=[script_filename])
    return closure

def check_all(script_path):
    if(script_path == "unit"):
        script_path = "benchmarks/UnitTest"
    def closure():
        listFiles = glob.glob(script_path + "/*.py")
        listFilesCount = len(listFiles)
        for s in listFiles:
#             if opts.unmarshall:
#                 print "Unmarshalling ... %s" % (s)
#             else:
#                 print "Running ... %s" % (s)
            sys.stdout.write("%-45s" % s)
            yield get_exec_check(action_args=[s])

    return closure

def get_exec_check_all_w_exist(cmd_class=get_jython_classname(), action_args=[]):
    
    def exec_closure(args=[], host_vm_opts='', client_vm_opts='', use_graal=False, errors=[]):
        
        executable = get_graal_executable() if use_graal else 'java'
        arguments = ' '.join(action_args + args)

        if opts.debug:
            host_vm_opts += get_debug_option()
        error_log = action_args[0].replace(".py", "_error.log")
        command = "%s %s -cp %s %s %s %s 2> %s" % (executable, host_vm_opts, get_jython_classpath(),
                                         cmd_class, client_vm_opts, arguments, error_log)
        outputResult = os.popen(command).read()
        bool_pass_or_fail = False
        statinfo = os.stat(error_log)
        python3outputResult = ""
        
        if (statinfo.st_size == 0):
            outputFilename = action_args[0].replace(".py", ".txt")
            outputfile = open(outputFilename, "r")
            python3outputResult = outputfile.read()
            if(cmp(outputResult,python3outputResult) == 0):
                bool_pass_or_fail = True
            
#         print bool_pass_or_fail
        if (bool_pass_or_fail): 
            pass_or_fail = "\033[92mPASS\033[0m"
            pass_or_fail_note = ""
            command = "rm %s" % error_log
            os.popen(command).read()
        else:
            pass_or_fail = "\033[91mFAIL\033[0m"
            pass_or_fail_note = "Please See %s for details" % error_log
            errorLogFile = open(error_log, "w")
            errorLogFile.write("\nPython output:\n")
            errorLogFile.write(python3outputResult)
            errorLogFile.write("\nJython output:\n")
            errorLogFile.write(outputResult)
        
        print "%-45s [%s] %s" % (action_args[0], pass_or_fail, pass_or_fail_note)
    return exec_closure

def check_all_w_exist(script_path):
    def closure():
        listFiles = glob.glob(script_path + "/*.py")
        listFilesCount = len(listFiles)
        for s in listFiles:
#             if opts.unmarshall:
#                 print "Unmarshalling ... %s" % (s)
#             else:
#                 print "Running ... %s" % (s)
        
            yield get_exec_check_all_w_exist(action_args=[s])
    return closure

def get_exec_produce_results(cmd_class=get_jython_classname(), action_args=[]):
    
    def exec_closure(args=[], host_vm_opts='', client_vm_opts='', use_graal=False, errors=[]):
        if(len(args) == 2):
            command = "%s %s %s 2> /dev/null" % (args[1] , action_args[0], args[0])
        else:
            command = "python3.2 %s %s 2> /dev/null" % (action_args[0], args[0])
    #             print command
    
        python3outputResult = os.popen(command).read()
        outputFilename = action_args[0].replace(".py", ".txt")
        outputfile = open(outputFilename, "w")
        outputfile.write(python3outputResult)
        print "%-45s [DONE]" % action_args[0]
    return exec_closure

def produce_results(script_path):
    def closure():
        listFiles = glob.glob(script_path + "/*.py")
#         print listFiles
        for s in listFiles:

            yield get_exec_produce_results(action_args=[s])

    return closure
        
def show_errors(errors, total_execs):
    print "#" * 80
    print "Summary:"
    print "\t%d out of %d execs failed:" % (len(errors), total_execs)
    for (executable, host_vm_opts, client_vm_opts, cmd_class, arguments) in errors:
        print "\t\t%s" % (arguments)

if __name__ == "__main__":
    check_config()
     
    (opts, args) = parse_cmd_line_args()
    driver = {
        # # binds a command line argument to an action...
        'execute'       : execute_script(opts.execute),
        'run'           : run_gn,
        'bench'         : run_bmarks_gn,
        'test'          : execute_test(opts.test),
        'runfolder'     : run_gn_folder(opts.runfolder),
        'execspecific'  : execute_single(opts.execspecific),
        'checksingle'   : check_single(opts.checksingle),
        'checking'      : check_all(opts.checking),
        'checkwexist'   : check_all_w_exist(opts.checkwexist),
        'produceresult' : produce_results(opts.produceresult)
        }

    # # the protocol is fairly easy and enables flexible arrangement of various actions
    # # and keeping passing of options and arguments hassle-free, too.
    # # the procedure is:
    # # 1) iterate over the driver-table and find which actions are specified,
    # # 2) execute the function specified in the table, these functions *MUST*
    # #    return an iterable data structure of callables (i.e., either a list or
    # #    a generator of functions),
    # # 3) call all functions of the list of step (2), supplying the action-independent
    # #    options and arguments.
    errors = []
    total_execs = 0
    for (key, iter_fn) in driver.iteritems():
        if hasattr(opts, key) and getattr(opts, key):
            for f in iter_fn():
                f(args=args, host_vm_opts=opts.host, client_vm_opts=opts.client,
                    use_graal=opts.graal, errors=errors)
                total_execs += 1
#     print "total exec : %s" % total_execs
    if errors:
        show_errors(errors, total_execs)
