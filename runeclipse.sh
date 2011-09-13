#!/bin/bash
if [ -z "${JDK7}" ]; then
  echo "JDK7 is not defined."
  exit 1;
fi
if [ -z "${MAXINE}" ]; then
  echo "MAXINE is not defined. It must point to a Maxine repository directory."
  exit 1;
fi
if [ -z "${GRAAL}" ]; then
  echo "GRAAL is not defined. It must point to a Maxine repository directory."
  exit 1;
fi
if [ ! -f "${DACAPO}/dacapo-9.12-bach.jar" ]; then
  echo "DACAPO must point to a directory containing dacapo-9.12-bach.jar"
  exit 1;
fi
COMMAND="${JDK7}/bin/java -graal -Xms1g -Xmx2g -esa -classpath ${DACAPO}/dacapo-9.12-bach.jar -XX:-GraalBailoutIsFatal -G:-QuietBailout $* Harness --preserve -n 15 eclipse"
echo $COMMAND
$COMMAND
echo $COMMAND
