
f=`dirname $0`
p=`pwd`

find ${p}/${f}/../${1}/ | grep .xml.gz > ${f}/../${2}
gzip -f ${f}/../${2}
