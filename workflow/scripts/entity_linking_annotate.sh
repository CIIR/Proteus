#!/bin/bash

f=`dirname $0`

INDEX_PATH=$1
CLEANED_TEIS=$2
OUTPUT_PATH=$3
TMP_PATH=$4
DRMAA=$5

echo $f/../$OUTPUT_PATH
mkdir $f/../$OUTPUT_PATH
rm -rf $f/../${OUTPUT_PATH}/*

# Temporary Hack -> This is what gets done when we aren't actually annotating, remove it once annotation works
#cd $f/../
#cp ${CLEANED_TEIS}/* ${OUTPUT_PATH}/

GALAGO_PATH=$f/../output/tacco/custom-galago/core/target/appassembler/bin/galago

# Start a galago search server on port 10005 (or as desired)
$GALAGO_PATH search --index=$INDEX_PATH --corpus=$INDEX_PATH/corpus --port=10005 --fields+title --fields+title-exact --fields+fbname --fields+fbtype --fields+category --fields+redirect --fields+redirect-exact --fields+kb_class --fields+anchor --fields+text --stemming=false &

echo "Galago Server Started... Pausing for 5 sec. to allow server to startup"
sleep 5

# Start annotating a single file
#scala -classpath target/classes/entity_linker:output/tacco/target/classes/:output/tacco/tackbp/target/classes/:output/tacco/custom-galago/core/target/classes/:output/tacco/custom-galago/tupleflow/target/classes/:output/tacco/factorie/target/classes/:dep/jedis-2.0.0.jar:dep/tagsoup-1.2.jar:dep/trove4j-3.0.2.jar:dep/snappy-java-1.0.4.1.jar TEIAnnotator output/mbteis/cleaned/00frengoog_mbtei.xml.gz ./00fren.xml.gz

gunzip ${f}/../${CLEANED_TEIS}
echo "List at ${f}/../${CLEANED_TEIS%.*}"
counter=`wc -l ${f}/../${CLEANED_TEIS%.*} | awk '{print $1}'`

if [ ${DRMAA} = 0 ]; then
    if [ $counter -lt 100 ]; then
	echo "split -l $counter ${f}/../${2}/teilist.list ${f}/../${2}/.tei_chunk"
	split -l 1 ${f}/../${CLEANED_TEIS%.*} ${f}/../${OUTPUT_PATH}/.tei_chunk
    else
	split -l 5 ${f}/../${CLEANED_TEIS%.*} ${f}/../${OUTPUT_PATH}/.tei_chunk
    fi
    for fl in ${f}/../${OUTPUT_PATH}/.tei_chunk* ; do
	sh ${f}/annotate_tei.sh ${fl} ${OUTPUT_PATH}
    done
else
#counter=`wc -l ${f}/../${2}/teilist.list | awk '{print $1}'`
    if [ $counter -lt ${DRMAA} ]; then
	split -l 1 ${f}/../${CLEANED_TEIS%.*} ${f}/../${OUTPUT_PATH}/.tei_chunk
    else
	numLines=`echo ${counter}/${3} | bc`
	split -l $numLines ${f}/../${CLEANED_TEIS%.*} ${f}/../${OUTPUT_PATH}/.tei_chunk
    fi
cd ${f}/../
for fl in ${f}/../${OUTPUT_PATH}/.tei_chunk* ; do 
    qsub -cwd -o ${f}/../output/logs/teiAnnotate.out -e ${f}/../output/logs/teiAnnotate.err ${f}/annotate_tei.sh ${fl} ${OUTPUT_PATH}
done

python ${f}/waitforClean.py ${f}/../${OUTPUT_PATH}/.tei_chunk
fi


echo "Killing all java processes to kill galago server... Need a better way"
killall -9 java
