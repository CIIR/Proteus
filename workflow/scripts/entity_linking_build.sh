#!/bin/bash

f=`dirname $0`
cd $f/../output/tacco

# Install Modified Galago into local maven
cd custom-galago/

# Next we should build the wiki-wex index
WEX_DATA=$1
INDEX_PATH=$2
TMP_DIR=$3

# However, we don't have an easy way to do that right now, so instead we will assume it has been built..
#if [ $4 = 0 ]; then
#./core/target/appassembler/bin/galago build --tokenizer/fields+title --tokenizer/fields+title-exact --tokenizer/fields+fbname --tokenizer/fields+fbtype --tokenizer/fields+category --tokenizer/fields+redirect --tokenizer/fields+redirect-exact --tokenizer/fields+kb_class --tokenizer/fields+anchor --tokenizer/fields+text --indexPath=$INDEX_PATH --inputPath+$WEX_DATA --galagoJobDir=$TMP_DIR --mode=threaded --distrib=8
#else
#./core/target/appassembler/bin/galago build --tokenizer/fields+title --tokenizer/fields+title-exact --tokenizer/fields+fbname --tokenizer/fields+fbtype --tokenizer/fields+category --tokenizer/fields+redirect --tokenizer/fields+redirect-exact --tokenizer/fields+kb_class --tokenizer/fields+anchor --tokenizer/fields+text --indexPath=$INDEX_PATH --inputPath+$WEX_DATA --galagoJobDir=$TMP_DIR --mode=drmaa --distrib=$4
#fi



