#!/bin/bash
if [ -z "${JRE7}" ]; then
  echo "JRE7 is not defined."
  exit 1;
fi
if [ -z "${MAXINE}" ]; then
  echo "MAXINE is not defined. It must point to a maxine repository directory."
  exit 1;
fi
if [ -z "${C1X}" ]; then
  echo "C1X is not defined. It must point to a c1x4hotspot directory."
  exit 1;
fi
for (( i = 1; i <= 4000; i++ ))      ### Outer for loop ###
do
  echo "$i "
  ${JRE7}/bin/java -client -esa -ea -XX:+UseC1X -Xms32m -Xmx100m -Xbootclasspath/p:${MAXINE}/C1X/bin:${MAXINE}/CRI/bin:${MAXINE}/Base/bin:${MAXINE}/Assembler/bin:${C1X}/c1x4hotspotsrc/HotSpotVM/bin -Xbootclasspath/a:/home/tw/Benchmarks/scimark/ -classpath "${SCIMARK}" -C1X:+PrintTimers  jnt.scimark2.commandline -large
done
