
OUTPATH="output"
SOURCE="targets.list"
ORIG_TEIS="${OUTPATH}/mbteis/mbteis.list"
ANNOTATED_TEIS="${OUTPATH}/mbteis/annotated_mbteis.list.gz"
TMP="/work1/barto/wdabney/tmp/" # "/usr/mildura/scratch1/tmp/"

DATAPATH="sources"

-include Makefile.gentei
-include Makefile.dbpedia

build_tei_list: gentei
              ifneq ($(strip $(wildcard .build_tei_list.success)),.build_tei_list.success)
	      	   sh scripts/build_tei_list.sh ${OUTPATH}/mbteis/cleaned ${ORIG_TEIS}
		   touch .build_tei_list.success
              endif

ambig_ent_index: gentei build_tei_list
              ifneq ($(strip $(wildcard .ambig_entity_index.success)),.ambig_entity_index.success)
	      	   sh scripts/index_entities.sh ${ORIG_TEIS}.gz ${OUTPATH}/indexes/ambig_entities ${TMP}
		   touch .ambig_entity_index.success
              endif

entity_linking: ambig_ent_index index_dbpedia gentei
              ifneq ($(strip $(wildcard .entity_linking.success)),.entity_linking.success)
	      	   echo "Updating entity linking model..."
		   touch .entity_linking.success
              endif

annotate_teis: entity_linking ambig_ent_index index_dbpedia gentei
              ifneq ($(strip $(wildcard .annotate_teis.success)),.annotate_teis.success)
	      	   echo "Annotating TEI files with entity disambiguations to list file ${ANNOTATED_TEIS}"
		   cp ${ORIG_TEIS}.gz ${ANNOTATED_TEIS}
		   touch .annotate_teis.success
              endif

index_books: annotate_teis
              ifneq ($(strip $(wildcard .index_books.success)),.index_books.success)
	      	   sh scripts/index_books.sh ${ANNOTATED_TEIS} ${OUTPATH}/indexes/books ${TMP}
		   touch .index_books.success
              endif

index_pages: annotate_teis
              ifneq ($(strip $(wildcard .index_pages.success)),.index_pages.success)
	      	   sh scripts/index_pages.sh ${ANNOTATED_TEIS} indexes/pages
		   touch .index_pages.success
              endif

index_pictures: annotate_teis
              ifneq ($(strip $(wildcard .index_pictures.success)),.index_pictures.success)
	      	   sh scripts/index_pictures.sh ${ANNOTATED_TEIS} indexes/pictures
		   touch .index_pictures.success
              endif

index_entities: annotate_teis
              ifneq ($(strip $(wildcard .index_entities.success)),.index_entities.success)
	      	   sh scripts/index_entities.sh ${ANNOTATED_TEIS} indexes/entities
		   touch .index_entities.success
              endif

create_location_database: index_entities
              ifneq ($(strip $(wildcard .create_location_database.success)),.create_location_database.success)
	      	   sh scripts/create_location_database.sh "../indexes/entities"
		   touch .create_location_database.success
              endif



all: | index_entities create_location_database index_books index_pages index_pictures
