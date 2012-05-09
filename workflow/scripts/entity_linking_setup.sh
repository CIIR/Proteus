#!/bin/bash

f=`dirname $0`
cd $f/../output/tacco

# Install Modified Galago into local maven
cd custom-galago/
# The script is not well formed it seems, do a little fix until its updated
sed -i 's: -a \": -e \":g' scripts/installlib.sh 
sh ./scripts/installlib.sh
mvn install -DskipTests=true
chmod u+x ./core/target/appassembler/bin/galago
