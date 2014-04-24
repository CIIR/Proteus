#!/bin/bash
set -e -u

HOMER_JAR=`find target/ -name homer*.jar`

java -jar $HOMER_JAR proteus acmdl_example_server.json

