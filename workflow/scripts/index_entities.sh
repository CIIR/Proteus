# Index Entities using galago and the specified tei file list
echo "Indexing Entities.... ../$1 to ../$2"
f=`dirname $0`
if [ $4 = 0 ]; then
echo "Building.. $1 $2 $3"
galago build \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --galagoJobDir=$3 \
--indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000
else
galago build \
--mode=drmaa --distrib=$4 \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --galagoJobDir=$3 --deleteJobDir=true \
--indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000
fi

