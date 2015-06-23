#!/bin/bash
set -e -u

wget http://books.cs.umass.edu/downloads/dparser-2011-01-18.jar
mvn install:install-file -Dfile=dparser-2011-01-18.jar -Dpackaging=jar -DgroupId=dparser -Dversion=2011-01-18 -DartifactId=dparser

