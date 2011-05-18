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
${JDK7}/bin/java -client -d64 -graal -ea -esa -Xcomp -XX:+PrintCompilation -XX:CompileOnly=jtt -Xbootclasspath/p:"${MAXINE}/com.oracle.max.vm/bin" -Xbootclasspath/p:"${MAXINE}/com.oracle.max.base/bin" $1 test.com.sun.max.vm.compiler.JavaTester -verbose=1 -gen-run-scheme=false -run-scheme-package=all ${MAXINE}/com.oracle.max.vm/test/jtt/bytecode ${MAXINE}/com.oracle.max.vm/test/jtt/except ${MAXINE}/com.oracle.max.vm/test/jtt/hotpath ${MAXINE}/com.oracle.max.vm/test/jtt/jdk ${MAXINE}/com.oracle.max.vm/test/jtt/lang ${MAXINE}/com.oracle.max.vm/test/jtt/loop ${MAXINE}/com.oracle.max.vm/test/jtt/micro ${MAXINE}/com.oracle.max.vm/test/jtt/optimize ${MAXINE}/com.oracle.max.vm/test/jtt/reflect ${MAXINE}/com.oracle.max.vm/test/jtt/threads
