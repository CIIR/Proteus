
tmp=${1##*/}
OUT_PATH=${2}/${tmp##.tei_chunk}
mkdir $OUT_PATH
while read fl; do
    if [ "${fl}" = "" ];
	then
	echo "Skipping blank line..."
    else
        y=${fl##*/}
	python scripts/fixTEIEntities.py ${fl} ${OUT_PATH}/${y} ; 
    fi
done < $1

rm ${1}
