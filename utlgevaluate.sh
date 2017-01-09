#!/bin/bash
for d in *; do
        if [ ! -d "${d}" ]; then
                continue
        fi

        mkdir "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}"
        mkdir "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/test"
        mkdir "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/train"
        echo "${0}: Entering directory \`${d}/utlgevaluate'"
        cd "${d}/utlgevaluate"
        cat train|python3 ~/github.com/m5w/matxin-lineariser/utlgtrain.py > train.xml 2> /dev/null
        cat train|python3 ~/github.com/m5w/matxin-lineariser/utlgtrain.py --lemma-file=lemmas > train.lemmas.xml 2> /dev/null
        cat test|python3 ~/github.com/m5w/matxin-lineariser/utlgevaluate.py train.xml ref.sgm src.sgm "$(cat trglang)" "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/test/figure_1.pdf" 1.sgm ~/github.com/moses-smt/mosesdecoder/scripts/generic/mteval-v13a.pl 100 "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/test/figure_2.pdf"
        cat train|python3 ~/github.com/m5w/matxin-lineariser/utlgevaluate.py train.xml ref.sgm src.sgm "$(cat trglang)" "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/train/figure_1.pdf" 1.sgm ~/github.com/moses-smt/mosesdecoder/scripts/generic/mteval-v13a.pl 1 "/home/matthew/github.com/m5w/matxin-lineariser/matxin_lineariser/utlgrammars/test/evaluate/${d}/train/figure_2.pdf"
        echo "${0}: Leaving directory \`${d}/utlgevaluate'"
        cd ../..
done
