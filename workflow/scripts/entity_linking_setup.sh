#!/bin/bash

f=`dirname $0`

if [ -e $f/../output/tacco ]; then
  echo "Updating tacco dependencies..."
  cd $f/../output/tacco
  git pull
else
  echo "Cloning tacco repository..."
  git clone --recursive scm-ciir.cs.umass.edu:/var/git/tacco.git $f/../output/tacco
  cd $f/../output/tacco
fi

# Install Modified Galago into local maven
cd custom-galago/
# The script is not well formed it seems, do a little fix until its updated
sed -i 's: -a \": -e \":g' scripts/installlib.sh 
sh ./scripts/installlib.sh
mvn install -DskipTests=true
chmod u+x ./core/target/appassembler/bin/galago

# Install tackbp
cd ../tackbp
mvn clean install

# Install factorie
cd ../factorie
mvn clean install -DskipTests source:jar

cd ..
mvn compile

# Copy the properties files, only do this if we need to, because they have been edited
#cp tackbp/src/main/resources/tackbp.properties ../../src/entity_linker/
#cp src/main/resources/tacco-example.properties ../../src/entity_linker/tacco.properties
cd ../../

#sed -e 's:<docid>\([^<]*\)</docid>:<docid>\1</docid>\n    <nodeid>\1</nodeid>\n    <entity>\1</entity>:g' /work1/allan/jdalton/entity-linking/tac_2011_kbp_english_evaluation_entity_linking_queries.xml > sources/tac_2011_kbp_queries.xml
