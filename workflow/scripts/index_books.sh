# Index Books using galago and the specified tei file list
echo "Indexing Books.... ../$1 to ../$2"
f=`dirname $0`
if [ $4 = 0 ]; then
galago build \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --tokenizer/fields+page \
--parser/splitTag=book --galagoJobDir=$3 --corpusParameters/corpusText=false --corpusParameters/corpusTerms=false \
--indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000
else
galago build \
--mode=drmaa --distrib=$4 \
--tokenizer/fields+person --tokenizer/fields+location \
--tokenizer/fields+organization --tokenizer/fields+page \
--parser/splitTag=book --galagoJobDir=$3 --corpusParameters/corpusText=false --corpusParameters/corpusTerms=false \
--deleteJobDir=false --indexPath=${f}/../$2 --inputPath+${f}/../$1 #--port=10000
fi
echo "Done indexing books..."