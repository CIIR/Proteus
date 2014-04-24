#!/bin/bash
set -e -u

HOMER_JAR=`find target/ -name homer*.jar`

java -jar $HOMER_JAR build example_acmdl/build_index.json --server=false --indexPath=example_acmdl/index_acmdl --inputPath=example_acmdl/example_dataset
