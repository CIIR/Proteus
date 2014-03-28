#!/bin/bash
set -e -u

HOMER_JAR=`find target/ -name homer*.jar`

echo "After this starts up, try: http://localhost:1234/?q=romeo"
echo ""

java -jar $HOMER_JAR proteus tiny-example-server.json

