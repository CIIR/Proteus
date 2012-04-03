# Index Books using galago and the specified tei file list
echo "Indexing Books.... ../$1 to ../$2"
f=`dirname $0`
galago build \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --tokenizer/fields+page \
--galagoJobDir=$3 \
--indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000
