
f=`dirname $0`

cat ${f}/../output/tests/clean_sorted.list | awk 'BEGIN{FS="_"}; {print $1}' > ${f}/../output/tests/clean_names.list

galago dump-index ${f}/../output/indexes/ambig_entities/names.reverse | awk 'BEGIN{FS="_"}; {print $1}' | uniq > ${f}/../output/tests/pages.list
sort ${f}/../output/tests/pages.list -o ${f}/../output/tests/pages_sorted.list

# Check their differences..
CHECK=`diff ${f}/../output/tests/clean_names.list ${f}/../output/tests/pages_sorted.list`

COL_GREEN="\x1b[32;01m"
COL_RED="\x1b[31;01m"
COL_RESET="\x1b[39;49;00m"

if ${CHECK}; then 
    echo -e "[SANITY CHECK]: "$COL_GREEN"PASSED"$COL_RESET""
else
    echo -e "[SANITY CHECK]: "$COL_RED"FAILED"$COL_RESET" --> Differences between cleaned TEI list and successfully indexed pages exist:"
    NUMCLEAN=`wc -l ${f}/../output/tests/clean_names.list | awk '{print $1}'`
    NUMBOOKS=`wc -l ${f}/../output/tests/pages_sorted.list | awk '{print $1}'`
    echo "Cleaned TEI List ("$NUMCLEAN") vs. Indexed Pages List [of books] ("$NUMBOOKS")"
    printf "%$(tput cols)s\n"|tr ' ' '='
    diff ${f}/../output/tests/clean_names.list ${f}/../output/tests/pages_sorted.list
fi

