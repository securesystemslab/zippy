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
if [ -z "${SCIMARK}" ]; then
  echo "SCIMARK is not defined. It must point to a directory with the SciMark benchmark jar."
  exit 1;
fi
${JDK7}/jre/bin/java -client -d64 -graal -Xms256m -Xmx512m -Xbootclasspath/a:${SCIMARK}/scimark2lib.jar $@ jnt.scimark2.commandline
