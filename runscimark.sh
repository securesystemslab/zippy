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
  echo "SCIMARK is not defined. It must point to a SciMark benchmark directory."
  exit 1;
fi
COUNT=$1
if [ -z "${COUNT}" ]; then
  COUNT=5000
fi
for (( i = 1; i <= ${COUNT}; i++ ))      ### Outer for loop ###
do
  echo "$i "
  ${JDK7}/jre/bin/java -client -graal -esa -ea -Xms32m -Xmx100m -Xbootclasspath/a:${SCIMARK} -C1X:+PrintTimers  jnt.scimark2.commandline -large
done
