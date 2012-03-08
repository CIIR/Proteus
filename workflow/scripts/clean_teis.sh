#!/bin/bash

f=`dirname $0`
mkdir $f/../output/mbteis/cleaned

# Call the program(s) to clean the fields of the TEI files in 'originals'
echo "Clearing out old files in output/mbteis/cleaned/ ..."
rm -rf $f/../output/mbteis/cleaned/*

echo "Cleaning TEI fields..."
# Don't have the programs yet so we are cheating:
cp $f/../output/mbteis/originals/* $f/../output/mbteis/cleaned/

