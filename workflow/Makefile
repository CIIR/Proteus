
OUTPATH="output"
SOURCE="../../demo-54K.present.list" # targets.list"
ORIG_TEIS="${OUTPATH}/mbteis/mbteis.list"
ANNOTATED_TEIS="${OUTPATH}/mbteis/annotated_mbteis.list.gz"
TMP="/work1/barto/wdabney/tmp/" # "/usr/mildura/scratch1/tmp/"
DATAPATH="sources"
WEX_DATA="armadale:/hdfs/disk2/jdalton/freebase-wex-2012-01-27-joined"
ENTLINK_INDEX="/work1/allan/jdalton/entity-linking/freebase-wex-all" # ${OUTPATH}/indexes/wiki_index

DRMAA=100
#DRMAA=0

-include Makefile.gentei
-include Makefile.dbpedia
-include Makefile.entlink

build_tei_list: gentei
              ifneq ($(strip $(wildcard .build_tei_list.success)),.build_tei_list.success)
	      	   sh scripts/build_tei_list.sh ${OUTPATH}/mbteis/cleaned ${ORIG_TEIS}
	      	   sh scripts/sanity_check_cleaned.sh
		   touch .build_tei_list.success
              endif

ambig_ent_index: gentei build_tei_list
              ifneq ($(strip $(wildcard .ambig_entity_index.success)),.ambig_entity_index.success)
	      	   sh scripts/index_entities.sh ${ORIG_TEIS}.gz ${OUTPATH}/indexes/ambig_entities ${TMP} ${DRMAA}
	      	   #-sh scripts/sanity_check_pages.sh
		   touch .ambig_entity_index.success
              endif

index_books: annotate_teis
              ifneq ($(strip $(wildcard .index_books.success)),.index_books.success)
	      	   sh scripts/index_books.sh ${ANNOTATED_TEIS} ${OUTPATH}/indexes/books ${TMP} ${DRMAA}
	      	   -sh scripts/sanity_check_books.sh
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
