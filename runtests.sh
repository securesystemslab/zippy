#!/bin/bash
if [ -z "${JDK7}" ]; then
  echo "JDK7 is not defined."
  exit 1;
fi
if [ -z "${MAXINE}" ]; then
  echo "MAXINE is not defined. It must point to a maxine repository directory."
  exit 1;
fi
if [ -z "${GRAAL}" ]; then
  echo "GRAAL is not defined. It must point to a maxine repository directory."
  exit 1;
fi
${JDK7}/bin/java -client -graal -ea -esa -Xcomp -XX:+PrintCompilation -XX:CompileOnly=jtt -Xbootclasspath/p:"${MAXINE}/VM/bin" -Xbootclasspath/p:"${MAXINE}/Base/bin" $1 test.com.sun.max.vm.compiler.JavaTester -verbose=1 -gen-run-scheme=false -run-scheme-package=all ${MAXINE}/VM/test/jtt/bytecode ${MAXINE}/VM/test/jtt/except ${MAXINE}/VM/test/jtt/hotpath ${MAXINE}/VM/test/jtt/jdk ${MAXINE}/VM/test/jtt/lang ${MAXINE}/VM/test/jtt/loop ${MAXINE}/VM/test/jtt/micro ${MAXINE}/VM/test/jtt/optimize ${MAXINE}/VM/test/jtt/reflect ${MAXINE}/VM/test/jtt/threads
