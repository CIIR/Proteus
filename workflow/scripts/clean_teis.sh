#!/bin/bash

f=`dirname $0`
mkdir $f/../$2

# Call the program(s) to clean the fields of the TEI files in 'originals'
echo "Clearing out old files in output/mbteis/cleaned/ ..."
rm -rf $f/../${2}/*

echo "Cleaning TEI fields..."
# Move entity name's into the RS tags so that tag-tokenizer can create fields from them:
#for fl in ${f}/../${1}/*_mbtei.xml.gz ; do sh ${f}/move_entity_tag.sh ${fl} ${f}/../${2} ; done
cp ${f}/../${1}/*_mbtei.xml.gz ${f}/../${2}/


