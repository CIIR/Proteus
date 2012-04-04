while read fl; do
    y=${fl##*/}
    python scripts/fixTEIEntities.py ${fl} ${2}/${y} ; 
done < $1

rm ${1}
