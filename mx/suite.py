from suite_helper import maven

suite = {
  "mxversion" : "1.0",
  "name" : "graal",
  "libraries" : {
    "JUNIT" : {
      "path" : "lib/junit-4.11.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/junit-4.11.jar",
        maven("junit/junit/4.11/junit-4.11.jar"),
      ],
      "sha1" : "4e031bb61df09069aeb2bffb4019e7a5034a4ee0",
      "eclipse.container" : "org.eclipse.jdt.junit.JUNIT_CONTAINER/4",
      "sourcePath" : "lib/junit-4.11-sources.jar",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/junit-4.11-sources.jar",
        maven("junit/junit/4.11/junit-4.11-sources.jar"),
      ],
      "sourceSha1" : "28e0ad201304e4a4abf999ca0570b7cffc352c3c",
      "dependencies" : ["HAMCREST"],
    },

    "HAMCREST" : {
      "path" : "lib/hamcrest-core-1.3.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/hamcrest-core-1.3.jar",
        maven("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"),
      ],
      "sha1" : "42a25dc3219429f0e5d060061f71acb49bf010a0",
      "sourcePath" : "lib/hamcrest-core-1.3-sources.jar",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/hamcrest-core-1.3-sources.jar",
        maven("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3-sources.jar"),
      ],
      "sourceSha1" : "1dc37250fbc78e23a65a67fbbaf71d2e9cbc3c0b",
    },

    "HCFDIS" : {
      "path" : "lib/hcfdis-2.jar",
      "urls" : ["http://lafo.ssw.uni-linz.ac.at/hcfdis-2.jar"],
      "sha1" : "bc8b2253436485e9dbaf81771c259ccfa1a24c80",
    },

    "FINDBUGS_DIST" : {
      "path" : "lib/findbugs-dist-3.0.0.zip",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/findbugs-3.0.0.zip",
        "http://sourceforge.net/projects/findbugs/files/findbugs/3.0.0/findbugs-3.0.0.zip/download",
      ],
      "sha1" : "6e56d67f238dbcd60acb88a81655749aa6419c5b",
    },

    "C1VISUALIZER_DIST" : {
      "path" : "lib/c1visualizer_2014-04-22.zip",
      "urls" : ["https://java.net/downloads/c1visualizer/c1visualizer_2014-04-22.zip"],
      "sha1" : "220488d87affb569b893c7201f8ce5d2b0e03141",
    },

    "JOL_INTERNALS" : {
      "path" : "lib/jol-internals.jar",
      "urls" : ["http://lafo.ssw.uni-linz.ac.at/truffle/jol/jol-internals.jar"],
      "sha1" : "508bcd26a4d7c4c44048990c6ea789a3b11a62dc",
    },

    "FINDBUGS" : {
      "path" : "lib/findbugs-3.0.0.jar",
      "urls" : [
        "jar:http://lafo.ssw.uni-linz.ac.at/graal-external-deps/findbugs-3.0.0.zip!/findbugs-3.0.0/lib/findbugs.jar",
        "jar:http://sourceforge.net/projects/findbugs/files/findbugs/3.0.0/findbugs-3.0.0.zip/download!/findbugs-3.0.0/lib/findbugs.jar",
      ],
      "sha1" : "e9a938f0cb34e2ab5853f9ecb1989f6f590ee385",
    },

    "DACAPO" : {
      "path" : "lib/dacapo-9.12-bach.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/dacapo-9.12-bach.jar",
        "http://softlayer.dl.sourceforge.net/project/dacapobench/9.12-bach/dacapo-9.12-bach.jar",
      ],
      "sha1" : "2626a9546df09009f6da0df854e6dc1113ef7dd4",
    },

    "JACOCOAGENT" : {
      "path" : "lib/jacocoagent.jar",
      "urls" : ["http://lafo.ssw.uni-linz.ac.at/jacoco/jacocoagent-0.7.1-1.jar"],
      "sha1" : "2f73a645b02e39290e577ce555f00b02004650b0",
    },

    "JACOCOREPORT" : {
      "path" : "lib/jacocoreport.jar",
      "urls" : ["http://lafo.ssw.uni-linz.ac.at/jacoco/jacocoreport-0.7.1-2.jar"],
      "sha1" : "a630436391832d697a12c8f7daef8655d7a1efd2",
    },

    "DACAPO_SCALA" : {
      "path" : "lib/dacapo-scala-0.1.0-20120216.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/dacapo-scala-0.1.0-20120216.jar",
        "http://repo.scalabench.org/snapshots/org/scalabench/benchmarks/scala-benchmark-suite/0.1.0-SNAPSHOT/scala-benchmark-suite-0.1.0-20120216.103539-3.jar",
      ],
      "sha1" : "59b64c974662b5cf9dbd3cf9045d293853dd7a51",
    },

    "OKRA" : {
      "path" : "lib/okra-1.10.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/okra-1.10.jar",
        "http://cr.openjdk.java.net/~tdeneau/okra-1.10.jar",
      ],
      "sha1" : "96eb3c0ec808ed944ba88d1eb9311058fe0f3d1e",
      "sourcePath" : "lib/okra-1.10-src.jar",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/okra-1.10-src.jar",
        "http://cr.openjdk.java.net/~tdeneau/okra-1.10-src.jar",
      ],
      "sourceSha1" : "75751bb148fcebaba78ff590f883a114b2b09176",
    },

    "OKRA_WITH_SIM" : {
      "path" : "lib/okra-1.10-with-sim.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/okra-1.10-with-sim.jar",
        "http://cr.openjdk.java.net/~tdeneau/okra-1.10-with-sim.jar",
      ],
      "sha1" : "7b8db879f1dbcf571290add78d9af24e15a2a50d",
      "sourcePath" : "lib/okra-1.10-with-sim-src.jar",
      "sourceSha1" : "7eefd94f16a3e3fd3b8f470cf91e265c6f5e7767",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/okra-1.10-with-sim-src.jar",
        "http://cr.openjdk.java.net/~tdeneau/okra-1.10-with-sim-src.jar",
      ],
    },

    "ASM" : {
      "path" : "lib/asm-5.0.3.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/asm-5.0.3.jar",
        maven("org/ow2/asm/asm/5.0.3/asm-5.0.3.jar"),
      ],
      "sha1" : "dcc2193db20e19e1feca8b1240dbbc4e190824fa",
      "sourcePath" : "lib/asm-5.0.3-sources.jar",
      "sourceSha1" : "f0f24f6666c1a15c7e202e91610476bd4ce59368",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/asm-5.0.3-sources.jar",
        maven("org/ow2/asm/asm/5.0.3/asm-5.0.3-sources.jar"),
      ],
    },

    "JAVA_ALLOCATION_INSTRUMENTER" : {
      "path" : "lib/java-allocation-instrumenter.jar",
      "sourcePath" : "lib/java-allocation-instrumenter.jar",
      "urls" : ["http://lafo.ssw.uni-linz.ac.at/java-allocation-instrumenter/java-allocation-instrumenter-8f0db117e64e.jar"],
      "sha1" : "476d9a44cd19d6b55f81571077dfa972a4f8a083",
      "bootClassPathAgent" : "true",
    },

    "VECMATH" : {
      "path" : "lib/vecmath-1.3.1.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/vecmath-1.3.1.jar",
        maven("java3d/vecmath/1.3.1/vecmath-1.3.1.jar"),
      ],
      "sha1" : "a0ae4f51da409fa0c20fa0ca59e6bbc9413ae71d",
    }
  },

  "jrelibraries" : {
    "JFR" : {
      "jar" : "jfr.jar",
    }
  },

  "projects" : {
    "com.oracle.nfi" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.7",
    },

    "com.oracle.nfi.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["test"],
      "dependencies" : [
        "com.oracle.nfi",
        "com.oracle.graal.compiler.common",
        "JUNIT",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.7",
    },

    "com.oracle.graal.api.collections" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal",
    },

    "com.oracle.graal.api.runtime" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal",
    },

    "com.oracle.graal.api.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.api.runtime",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal,Test",
    },

    "com.oracle.graal.api.meta" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal",
    },

    "com.oracle.graal.api.meta.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.runtime",
        "com.oracle.graal.java",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal,Test",
    },

    "com.oracle.graal.api.code" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.meta"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal",
    },

    "com.oracle.graal.api.replacements" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.meta"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal,Replacements",
    },

    "com.oracle.graal.service.processor" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.runtime"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Codegen,HotSpot",
    },

    "com.oracle.graal.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.code"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,AMD64",
    },

    "com.oracle.graal.ptx" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.code"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,PTX",
    },

    "com.oracle.graal.sparc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.code"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,SPARC",
    },

    "com.oracle.graal.hotspotvmconfig" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.common"],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.replacements",
        "com.oracle.graal.runtime",
        "com.oracle.graal.printer",
        "com.oracle.graal.baseline",
        "com.oracle.graal.hotspotvmconfig",
        "com.oracle.nfi",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : [
        "com.oracle.graal.replacements.verifier",
        "com.oracle.graal.service.processor",
      ],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot.loader" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot.sourcegen" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.hotspot"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot.jfr" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.hotspot",
        "JFR",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "profile" : "",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler.amd64",
        "com.oracle.graal.hotspot",
        "com.oracle.graal.replacements.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,AMD64",
    },

    "com.oracle.graal.hotspot.sparc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.sparc"],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,SPARC",
    },

    "com.oracle.graal.hotspot.ptx" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.ptx",
        "com.oracle.graal.compiler.ptx",
        "com.oracle.graal.hotspot",
        "com.oracle.graal.gpu",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,PTX",
    },

    "com.oracle.graal.hotspot.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.replacements.hsail",
        "com.oracle.graal.hotspot",
        "com.oracle.graal.gpu",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,PTX",
    },

    "com.oracle.graal.hotspot.server" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.hotspot"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot",
    },

    "com.oracle.graal.hotspot.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.replacements.test",
        "com.oracle.graal.hotspot",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,Test",
    },

    "com.oracle.graal.hotspot.amd64.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.asm.amd64",
        "com.oracle.graal.compiler.test",
        "com.oracle.graal.hotspot",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,HotSpot,AMD64,Test",
    },

    "com.oracle.graal.options" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Codegen",
    },

    "com.oracle.graal.options.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.options",
        "JUNIT",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal",
    },

    "com.oracle.graal.nodeinfo" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.nodeinfo.processor" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "dependencies" : [
        "com.oracle.graal.nodeinfo",
        "com.oracle.truffle.dsl.processor",
      ],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.graph" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.nodeinfo",
        "com.oracle.graal.debug",
        "com.oracle.graal.compiler.common",
        "com.oracle.graal.api.collections",
        "com.oracle.graal.api.runtime",
        "FINDBUGS",
      ],
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.nodeinfo.processor"],
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.graph.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.graph",
      ],
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Graph,Test",
    },

    "com.oracle.graal.debug" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Debug",
    },

    "com.oracle.graal.debug.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.debug",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Debug,Test",
    },

    "com.oracle.graal.lir" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler.common",
        "com.oracle.graal.asm",
        "com.oracle.graal.debug",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,LIR",
    },

    "com.oracle.graal.lir.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.lir",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,LIR",
    },

    "com.oracle.graal.lir.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.lir",
        "com.oracle.graal.asm.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,LIR,AMD64",
    },

    "com.oracle.graal.lir.ptx" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.asm.ptx"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,LIR,PTX",
    },

    "com.oracle.graal.lir.sparc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.asm.sparc"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,LIR,SPARC",
    },

    "com.oracle.graal.alloc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.common"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal",
    },

    "com.oracle.graal.word" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.phases"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "API,Graal",
    },

    "com.oracle.graal.replacements" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler",
        "com.oracle.graal.java",
        "com.oracle.graal.word",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : [
        "com.oracle.graal.replacements.verifier",
        "com.oracle.graal.service.processor",
      ],
      "workingSets" : "Graal,Replacements",
    },

    "com.oracle.graal.replacements.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.replacements"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "workingSets" : "Graal,Replacements,AMD64",
    },

    "com.oracle.graal.replacements.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.hsail"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Replacements,HSAIL",
    },

    "com.oracle.graal.replacements.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler.test",
        "com.oracle.graal.replacements",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Replacements,Test",
    },

    "com.oracle.graal.replacements.verifier" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.api.replacements",
        "com.oracle.graal.graph",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Replacements",
    },

    "com.oracle.graal.nodes" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.graph",
        "com.oracle.graal.api.replacements",
        "com.oracle.graal.lir",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.replacements.verifier"],
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.nodes.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.test"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.phases" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.nodes"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Phases",
    },

    "com.oracle.graal.phases.common" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.phases"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Phases",
    },

    "com.oracle.graal.virtual" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.phases.common"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Phases",
    },

    "com.oracle.graal.loop" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.phases.common"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Phases",
    },

    "com.oracle.graal.compiler" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.virtual",
        "com.oracle.graal.loop",
        "com.oracle.graal.alloc",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "workingSets" : "Graal",
    },

    "com.oracle.graal.compiler.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler",
        "com.oracle.graal.lir.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,AMD64",
    },

    "com.oracle.graal.compiler.amd64.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.test"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,AMD64,Test",
    },

    "com.oracle.graal.compiler.ptx" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.lir.ptx",
        "com.oracle.graal.compiler",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,PTX",
    },

    "com.oracle.graal.compiler.ptx.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.hotspot.ptx",
        "com.oracle.graal.compiler.test",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,PTX,Test",
    },

    "com.oracle.graal.compiler.sparc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.lir.sparc"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,SPARC",
    },

    "com.oracle.graal.compiler.sparc.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler.test"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,SPARC,Test",
    },

    "com.oracle.graal.runtime" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.compiler"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal",
    },

    "com.oracle.graal.bytecode" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Java",
    },

    "com.oracle.graal.java" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.phases",
        "com.oracle.graal.bytecode",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Java",
    },

    "com.oracle.graal.compiler.common" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.api.code",
        "com.oracle.graal.options",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Java",
    },

    "com.oracle.graal.baseline" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler",
        "com.oracle.graal.java",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Java",
    },

    "com.oracle.graal.java.decompiler" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.java"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal",
    },

    "com.oracle.graal.java.decompiler.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.printer",
        "com.oracle.graal.runtime",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Test",
    },

    "com.oracle.graal.printer" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.java.decompiler",
        "com.oracle.graal.compiler",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Graph",
    },

    "com.oracle.graal.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "JUNIT",
        "com.oracle.graal.debug",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Test",
    },

    "com.oracle.graal.compiler.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.test",
        "com.oracle.graal.printer",
        "com.oracle.graal.runtime",
        "com.oracle.graal.baseline",
        "JAVA_ALLOCATION_INSTRUMENTER",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Test",
    },

    "com.oracle.graal.jtt" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler.test",
        "ASM",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Test",
    },

    "com.oracle.graal.asm" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.code"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler",
    },

    "com.oracle.graal.asm.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.test",
        "com.oracle.graal.runtime",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler,Test",
    },

    "com.oracle.graal.asm.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.asm",
        "com.oracle.graal.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler,AMD64",
    },

    "com.oracle.graal.asm.amd64.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.asm.test",
        "com.oracle.graal.asm.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler,AMD64,Test",
    },

    "com.oracle.graal.gpu" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.nodes"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.api.code"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.lir.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.lir",
        "com.oracle.graal.asm.hsail",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.compiler.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler",
        "com.oracle.graal.lir.hsail",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.compiler.hsail.test.infra" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.test",
        "com.oracle.graal.hotspot.hsail",
        "OKRA_WITH_SIM",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.compiler.hsail.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.compiler.hsail.test.infra",
        "com.oracle.graal.compiler.test",
        "VECMATH",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.asm.hsail" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.hsail",
        "OKRA",
        "com.oracle.graal.asm",
        "com.oracle.graal.compiler.common",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
    },

    "com.oracle.graal.asm.ptx" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.graal.lir"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler,PTX",
    },

    "com.oracle.graal.asm.sparc" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.hotspot",
        "com.oracle.graal.sparc",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Assembler,SPARC",
    },

    "com.oracle.truffle.api" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "javaCompliance" : "1.7",
      "workingSets" : "API,Truffle",
    },

    "com.oracle.truffle.api.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.api",
        "JUNIT",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.7",
      "workingSets" : "API,Truffle,Test",
    },

    "com.oracle.truffle.api.dsl" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.truffle.api"],
      "checkstyle" : "com.oracle.truffle.api",
      "javaCompliance" : "1.7",
      "workingSets" : "API,Truffle,Codegen",
    },

    "com.oracle.truffle.api.dsl.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.api.dsl",
        "JUNIT",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.7",
      "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "workingSets" : "API,Truffle,Codegen,Test",
    },

    "com.oracle.truffle.dsl.processor" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.truffle.api.dsl"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.7",
      "workingSets" : "Truffle,Codegen",
    },

    "com.oracle.truffle.sl" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.truffle.api.dsl"],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "workingSets" : "Truffle,SimpleLanguage",
    },

    "com.oracle.truffle.sl.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.sl",
        "JUNIT",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,SimpleLanguage,Test",
    },

    "com.oracle.graal.truffle" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.api",
        "com.oracle.graal.replacements",
        "com.oracle.graal.runtime",
        "com.oracle.graal.printer",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Truffle",
    },

    "com.oracle.graal.truffle.test" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.truffle",
        "com.oracle.graal.compiler.test",
        "com.oracle.truffle.sl.test",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "workingSets" : "Graal,Truffle,Test",
    },

    "com.oracle.graal.truffle.hotspot" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.truffle",
        "com.oracle.graal.hotspot",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "workingSets" : "Graal,Truffle",
    },

    "com.oracle.graal.truffle.hotspot.amd64" : {
      "subDir" : "graal",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.graal.truffle.hotspot",
        "com.oracle.graal.asm.amd64",
      ],
      "checkstyle" : "com.oracle.graal.graph",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.graal.service.processor"],
      "workingSets" : "Graal,Truffle",
    }
  },

  "distributions" : {
    "GRAAL" : {
      "path" : "build/graal.jar",
      "subDir" : "graal",
      "sourcesPath" : "build/graal.src.zip",
      "dependencies" : [
        "com.oracle.graal.hotspot.amd64",
        "com.oracle.graal.hotspot.ptx",
        "com.oracle.graal.hotspot.sparc",
        "com.oracle.graal.hotspot",
        "com.oracle.graal.hotspot.jfr",
        "com.oracle.graal.hotspot.hsail",
      ],
      "exclude" : ["FINDBUGS"],
    },

    "GRAAL_LOADER" : {
      "path" : "build/graal-loader.jar",
      "subDir" : "graal",
      "sourcesPath" : "build/graal-loader.src.zip",
      "dependencies" : ["com.oracle.graal.hotspot.loader"],
    },

    "TRUFFLE" : {
      "path" : "build/truffle.jar",
      "subDir" : "graal",
      "sourcesPath" : "build/truffle.src.zip",
      "javaCompliance" : "1.7",
      "dependencies" : [
        "com.oracle.truffle.api.dsl",
        "com.oracle.nfi",
      ],
    },

    "GRAAL_TRUFFLE" : {
      "path" : "build/graal-truffle.jar",
      "subDir" : "graal",
      "sourcesPath" : "build/graal-truffle.src.zip",
      "dependencies" : [
        "com.oracle.graal.truffle",
        "com.oracle.graal.truffle.hotspot.amd64",
      ],
      "exclude" : ["FINDBUGS"],
      "distDependencies" : [
        "GRAAL",
        "TRUFFLE",
      ],
    },

    "TRUFFLE-DSL-PROCESSOR" : {
      "path" : "build/truffle-dsl-processor.jar",
      "subDir" : "graal",
      "sourcesPath" : "build/truffle-dsl-processor.src.zip",
      "javaCompliance" : "1.7",
      "dependencies" : ["com.oracle.truffle.dsl.processor"],
      "distDependencies" : ["TRUFFLE"],
    }
  },

}
