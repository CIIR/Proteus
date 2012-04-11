
f=`dirname $0`
mkdir ${f}/../output/tests
rm ${f}/../output/tests/*

sh ${f}/build_tei_list.sh output/mbteis/originals output/tests/orig.list
zcat ${f}/../output/tests/orig.list.gz | awk 'BEGIN{ FS="/" }; {print $NF}' > ${f}/../output/tests/orig.list
sort ${f}/../output/tests/orig.list -o ${f}/../output/tests/orig_sorted.list

sh ${f}/build_tei_list.sh output/mbteis/cleaned output/tests/clean.list
zcat ${f}/../output/tests/clean.list.gz | awk 'BEGIN{ FS="/" }; {print $NF}' > ${f}/../output/tests/clean.list
sort ${f}/../output/tests/clean.list -o ${f}/../output/tests/clean_sorted.list

# Check their differences..
CHECK=`diff ${f}/../output/tests/orig_sorted.list ${f}/../output/tests/clean_sorted.list`

COL_GREEN="\x1b[32;01m"
COL_RED="\x1b[31;01m"
COL_RESET="\x1b[39;49;00m"

if ${CHECK}; then 
    echo -e "[SANITY CHECK]: "$COL_GREEN"PASSED"$COL_RESET""
else
    echo -e "[SANITY CHECK]: "$COL_RED"FAILED"$COL_RESET" --> Differences between original TEI list and cleaned TEI list exist:"
    NUMCLEAN=`wc -l ${f}/../output/tests/clean_sorted.list | awk '{print $1}'`
    NUMORIG=`wc -l ${f}/../output/tests/orig_sorted.list | awk '{print $1}'`
    echo "Original TEI List ("$NUMORIG") vs. Cleaned TEI List ("$NUMCLEAN")"
    printf "%$(tput cols)s\n"|tr ' ' '='
    diff ${f}/../output/tests/orig_sorted.list ${f}/../output/tests/clean_sorted.list
fi

