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
if [ -z "${DACAPO}" ]; then
  echo "DACAPO is not defined. It must point to a Dacapo benchmark directory."
  exit 1;
fi
COMMAND="${JDK7}/bin/java -graal -Xms1g -Xmx2g -esa -classpath ${DACAPO}/dacapo-9.12-bach.jar -XX:-GraalBailoutIsFatal -G:-QuietBailout $* Harness --preserve -n 20 xalan"
echo $COMMAND
$COMMAND
