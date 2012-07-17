f=`dirname $0`

while read fl; do
    y=${fl##*/}
    echo "Annotating" $fl "saving output to" ${2}/${y}
    scala -classpath target/classes/entity_linker:output/tacco/target/classes/:output/tacco/tackbp/target/classes/:output/tacco/custom-galago/core/target/classes/:output/tacco/custom-galago/tupleflow/target/classes/:output/tacco/factorie/target/classes/:dep/jedis-2.0.0.jar:dep/tagsoup-1.2.jar:dep/trove4j-3.0.2.jar:dep/snappy-java-1.0.4.1.jar TEIAnnotator ${fl} ${2}/${y} ;
done < $1

rm ${1}
