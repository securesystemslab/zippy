suite = {
  "mxversion" : "2.8.0",
  "name" : "zippy",
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

  },

  "projects" : {
    "edu.uci.python.nodes" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.runtime","com.oracle.truffle.api","com.oracle.truffle.api.dsl","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.parser" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.profiler","edu.uci.python.builtins","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.shell" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["JLINE09","edu.uci.python.nodes","edu.uci.python.profiler","edu.uci.python.runtime","edu.uci.python.parser","edu.uci.python.builtins",
      "JYTHON","JAVA_ALLOCATION_INSTRUMENTER"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.profiler" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.builtins" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","edu.uci.python.profiler","com.oracle.truffle.api","com.oracle.truffle.api.dsl","JYTHON"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "annotationProcessors" : ["com.oracle.truffle.dsl.processor"],
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.runtime" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["com.oracle.truffle.api","JYTHON","ASM","JAMM"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.benchmark" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","JUNIT"],
      "checkstyle" : "edu.uci.python.runtime",
      "javaCompliance" : "1.8",
      "workingSets" : "Truffle,Python",
    },

    "edu.uci.python.test" : {
      "sourceDirs" : ["src"],
      "dependencies" : ["edu.uci.python.nodes","edu.uci.python.runtime","edu.uci.python.shell","JUNIT"],
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
