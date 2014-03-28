#!/bin/bash
set -e -u

HOMER_JAR=`find target/ -name homer*.jar`

INPUT_PATH='src/test/resources/toktei'
mkdir -p examples

java -jar $HOMER_JAR build scripts/pages.conf --server=false --indexPath=examples/tiny.pages --inputPath=$INPUT_PATH
java -jar $HOMER_JAR build scripts/books.conf --server=false --indexPath=examples/tiny.books --inputPath=$INPUT_PATH

