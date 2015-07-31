suite = {
  "mxversion" : "5.0",
  "name" : "zippy",

  "imports" : {
    "suites": [
            {
               "name" : "graal",
               "version" : "9dafd1dc5ff9788a838653f3cd9d79a4b9acf18c",
               "urls" : [
                    {"url" : "http://hg.openjdk.java.net/graal/graal-compiler", "kind" : "hg"},
                ]
            },
    ]
   },

    # ------------- Libraries -------------

  "libraries" : {
    "JLINE09" : {
      "path" : "lib/jline-0.9.95-SNAPSHOT.jar",
      "urls" : [
        "http://mirrors.ibiblio.org/maven2/jline/jline/0.9.94/jline-0.9.94.jar",
      ],
      "sha1" : "99a18e9a44834afdebc467294e1138364c207402",
    },

    "JYTHON" : {
      "path" : "lib/jython-standalone-2.7-b3.jar",
      "urls" : [
        "http://repo1.maven.org/maven2/org/python/jython-standalone/2.7-b3/jython-standalone-2.7-b3.jar",
      ],
      "sha1" : "56411f652bcf4acce8e9fb3bc7d06b4a0e926aaf",
    },

    "JAMM" : {
      "path" : "lib/jamm-0.2.5.jar",
      "urls" : [
        "http://central.maven.org/maven2/com/github/stephenc/jamm/0.2.5/jamm-0.2.5.jar",
      ],
      "sha1" : "0422d3543c01df2f1d8bd1f3064adb54fb9e93f3",
    },

    "ASM" : {
      "path" : "lib/asm-5.0.3.jar",
      "urls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/asm-5.0.3.jar",
        "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/5.0.3/asm-5.0.3.jar",
      ],
      "sha1" : "dcc2193db20e19e1feca8b1240dbbc4e190824fa",
      "sourcePath" : "lib/asm-5.0.3-sources.jar",
      "sourceSha1" : "f0f24f6666c1a15c7e202e91610476bd4ce59368",
      "sourceUrls" : [
        "http://lafo.ssw.uni-linz.ac.at/graal-external-deps/asm-5.0.3-sources.jar",
        "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/5.0.3/asm-5.0.3-sources.jar",
      ],
    },

  },

  "projects" : {

    # ------------- ZipPy -------------

    "edu.uci.python.nodes" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
    #   "dependencies" : ["edu.uci.python.runtime","com.oracle.truffle.api","com.oracle.truffle.api.dsl","com.oracle.graal.truffle","JYTHON"],
      "dependencies" : ["edu.uci.python.runtime","truffle:TRUFFLE","truffle:TRUFFLE_DSL_PROCESSOR","graal:GRAAL_TRUFFLE","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
    #   "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.parser" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
#      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.profiler","edu.uci.python.builtins","JYTHON"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.builtins","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.shell" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
#      "dependencies" : ["JLINE09","edu.uci.python.nodes","edu.uci.python.profiler","edu.uci.python.runtime","edu.uci.python.parser","edu.uci.python.builtins",
      "dependencies" : ["JLINE09","edu.uci.python.nodes","edu.uci.python.runtime","edu.uci.python.parser","edu.uci.python.builtins",
      "JYTHON","graal:JAVA_ALLOCATION_INSTRUMENTER"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

#    "edu.uci.python.profiler" : {
#      "sourceDirs" : ["src"],
#      "dependencies" : ["edu.uci.python.nodes","JYTHON"],
#      "checkstyle" : "edu.uci.python.runtime",
#      "javaCompliance" : "1.8",
#      "workingSets" : "Truffle,Python",
#    },

    "edu.uci.python.builtins" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
#      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","edu.uci.python.profiler","TRUFFLE","com.oracle.truffle.api.dsl","JYTHON"],
    #   "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","com.oracle.truffle.api","com.oracle.truffle.api.dsl","JYTHON"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","truffle:TRUFFLE","truffle:TRUFFLE_DSL_PROCESSOR","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
    #   "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.runtime" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
    #   "dependencies" : ["com.oracle.truffle.api","com.oracle.graal.truffle","JYTHON","ASM","JAMM"],
      "dependencies" : ["truffle:TRUFFLE","graal:GRAAL_TRUFFLE","JYTHON","ASM","JAMM"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.test" : {
      "subDir" : "zippy",
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","edu.uci.python.shell","mx:JUNIT"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

  },

  "distributions" : {
    "ZIPPY" : {
      "path" : "zippy.jar",
      "dependencies" : [
        "edu.uci.python.shell",
      ],
      "sourcesPath" : "zippy-sources.jar",
    },

  },
}
