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
TESTDIR=${MAXINE}/com.oracle.max.vm/test
${JDK7}/bin/java -client -d64 -graal -ea -esa -Xcomp -C1X:+PrintCFGToFile -XX:+TraceDeoptimization -XX:-TraceExceptions -XX:+PrintCompilation -XX:CompileOnly=jtt/except/BC_dastore -Xbootclasspath/p:"${MAXINE}/com.oracle.max.vm/bin" -C1X:+PrintAssembly -Xbootclasspath/p:"${MAXINE}/com.oracle.max.base/bin" $1 test.com.sun.max.vm.compiler.JavaTester -verbose=1 -gen-run-scheme=false -run-scheme-package=all ${TESTDIR}/jtt/bytecode ${TESTDIR}/jtt/except ${TESTDIR}/jtt/hotpath ${TESTDIR}/jtt/jdk ${TESTDIR}/jtt/lang ${TESTDIR}/jtt/loop ${TESTDIR}/jtt/micro ${TESTDIR}/jtt/optimize ${TESTDIR}/jtt/reflect ${TESTDIR}/jtt/threads
