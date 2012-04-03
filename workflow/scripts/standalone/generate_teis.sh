#!/bin/bash

f=`dirname $0`
mkdir $f/../../output
mkdir $f/../../output/mbteis
mkdir $f/../../output/mbteis/originals

# Call the program(s) to generate the TEI files from the source file
echo "Clearing out old files..."
rm -rf $f/../../output/mbteis/originals/*

echo "Generating TEI files from source file: " $1
# Don't have the programs yet so we are cheating:
cp /usr/mildura/scratch1/shortlist/* $f/../../output/mbteis/originals/

