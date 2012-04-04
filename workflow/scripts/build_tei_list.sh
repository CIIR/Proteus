
f=`dirname $0`
p=`pwd`
find ${p}/${f}/../${1}/ -name *.xml.gz > ${f}/../${2}
gzip -f ${f}/../${2}
