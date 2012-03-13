# Index Entities using galago and the specified tei file list
echo "Indexing Entities.... ../$1 to ../$2"
f=`dirname $0`
galago build \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --galagoJobDir=/usr/mildura/scratch1/tmp \
--indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000