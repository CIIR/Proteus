#!/bin/bash

f=`dirname $0`
mkdir $f/../$2

# Call the program(s) to clean the fields of the TEI files in 'originals'
echo "Clearing out old files in output/mbteis/cleaned/ ..."
rm -rf $f/../${2}/*

echo "Cleaning TEI fields..."
# Move entity name's into the RS tags so that tag-tokenizer can create fields from them:
sh ${f}/build_tei_list.sh $1 ${f}/../${2}/teilist.list
gunzip ${f}/../${2}/teilist.list.gz

echo "List at ${f}/../${2}/teilist.list"
if [ $3 = 0 ]; then
split -l 200 ${f}/../${2}/teilist.list ${f}/../${2}/.tei_chunk

for fl in ${f}/../${2}/.tei_chunk* ; do
    sh ${f}/cleanTEIChunk.sh ${fl} ${2}
done
else
counter=`wc -l ${f}/../${2}/teilist.list | awk '{print $1}'`
numLines=`echo ${counter}/${3} | bc`
split -l $numLines ${f}/../${2}/teilist.list ${f}/../${2}/.tei_chunk

for fl in ${f}/../${2}/.tei_chunk* ; do 
    qsub -cwd -o ${f}/../output/logs/teiClean.out -e ${f}/../output/logs/teiClean.err ${f}/cleanTEIChunk.sh ${fl} ${2}
done

python ${f}/waitforClean.py ${f}/../${2}/.tei_chunk
fi

rm ${f}/../${2}/teilist.list
#rm ${f}/../${2}/.tei_chunk*

#cp ${f}/../${1}/*_mbtei.xml.gz ${f}/../${2}/


