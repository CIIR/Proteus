f=`dirname $0`
if [ $2 = 0 ]; then
galago build \
--tokenizer/fields+object-title --tokenizer/fields+objecttype \
--tokenizer/fields+object-name --tokenizer/fields+object-synonym \
--tokenizer/fields+internal-link --tokenizer/fields+external-link \
--tokenizer/fields+text-value --galagoJobDir=$1 \
--indexPath=${f}/../../output/indexes/dbpedia --inputPath+${f}/../../output/trecweb/ # --port=55555
else
galago build \
--mode=drmaa --distrib=$2 \
--tokenizer/fields+object-title --tokenizer/fields+objecttype \
--tokenizer/fields+object-name --tokenizer/fields+object-synonym \
--tokenizer/fields+internal-link --tokenizer/fields+external-link \
--tokenizer/fields+text-value --galagoJobDir=$1 \
--indexPath=${f}/../../output/indexes/dbpedia --inputPath+${f}/../../output/trecweb/ # --port=55555
fi