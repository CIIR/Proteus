#!/bin/sh

HOMER="java -cp target/homer-0.2.jar ciir.proteus.build.Main"

# build books & pages - no deps on anything else
#${HOMER} build new-config/buildBooks.json
#${HOMER} build new-config/buildPages.json


# build people/corpus
${HOMER} build-entity-corpus new-config/buildPersonCorpus.json
# use people/corpus to build the rest of the index
#${HOMER} build new-config/buildPerson.json

