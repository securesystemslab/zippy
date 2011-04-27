#!/bin/bash
${GRAAL}/java64/bin/java -client -graal -Xms1g -Xmx2g -esa -classpath ${DACAPO}/dacapo-9.12-bach.jar Harness $*
