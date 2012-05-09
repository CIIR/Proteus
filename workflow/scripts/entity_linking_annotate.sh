#!/bin/bash

f=`dirname $0`

INDEX_PATH=$1
CLEANED_TEIS=$2
OUTPUT_PATH=$3
TMP_PATH=$4
DRMAA=$5

mkdir $f/../$OUTPUT_PATH
rm -rf $f/../${OUTPUT_PATH}/*

# Temporary Hack
cp ${CLEANED_TEIS}/* ${OUTPUT_PATH}/

GALAGO_PATH=$f/../output/tacco/custom-galago/core/target/appassembler/bin/galago

# Start a galago search server on port 10005 (or as desired)
$GALAGO_PATH search --index=$INDEX_PATH --corpus=$INDEX_PATH/corpus --port=10005 --fields+title --fields+title-exact --fields+fbname --fields+fbtype --fields+category --fields+redirect --fields+redirect-exact --fields+kb_class --fields+anchor --fields+text


