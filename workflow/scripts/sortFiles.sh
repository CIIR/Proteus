# 1: path to files to sort, 2: path to output sorted files, 3: path to temp dir.

ls -al ${1}* | awk '{print $8}' | awk 'FS="/" {print "TARGET=" $0 "; echo Sorting... $TARGET; sort -s -T $2 " $0 " -o ${1}/${TARGET##*/}"}' > tmpSort.sh
sh tmpSort.sh $2 $3
rm tmpSort.sh
