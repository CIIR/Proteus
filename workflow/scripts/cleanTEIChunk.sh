
f=`dirname $0`
while read fl; do
    y=${fl##*/}
    python ${f}/fixTEIEntities.py ${fl} ${f}/../${2}/${y} ; 
done < $1
