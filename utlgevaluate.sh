#!/bin/bash
mteval="/home/matthew/github.com/moses-smt/mosesdecoder/scripts/generic/mteval-v13a.pl"

for d in *; do
        if [ ! -d "${d}" ]; then
                continue
        fi

        prefix="/home/matthew/github.com/m5w/matxin-lineariser"
        data_prefix="${prefix}/matxin_lineariser/utlgrammars/test/evaluate/${d}"

        mkdir -p "${data_prefix}/test"
        mkdir -p "${data_prefix}/test/projective"
        mkdir -p "${data_prefix}/train"

        echo "${0}: Entering directory \`${d}/utlgevaluate'"
        cd "${d}/utlgevaluate"

        cat train|python3 "${prefix}/utlgtrain.py" > train.xml 2> /dev/null
        cat train|python3 "${prefix}/utlgtrain.py" --projectivise > train_projective.xml 2> /dev/null
        cat test|python3 "${prefix}/utlgevaluate.py" train.xml ref_test.sgm src_test.sgm "$(cat trglang)" --figure-1="${data_prefix}/test/figure_1.pdf" 1_test.sgm "${mteval}" 100 --figure-2="${data_prefix}/test/figure_2.pdf"
        cat test|python3 "${prefix}/utlgevaluate.py" train_projective.xml ref_test_projective.sgm src_test_projective.sgm "$(cat trglang)" 1_test_projective.sgm "${mteval}" 100 --figure-2="${data_prefix}/test/projective/figure_2.pdf"
        cat train|python3 "${prefix}/utlgevaluate.py" train.xml ref_train.sgm src_train.sgm "$(cat trglang)" --figure-1="${data_prefix}/train/figure_1.pdf" 1_train.sgm "${mteval}" 1
        cat train|python3 "${prefix}/utlgevaluate.py" train.xml ref_train_projective.sgm src_train_projective.sgm "$(cat trglang)" 1_train_projective.sgm "${mteval}" 1

        echo "${0}: Leaving directory \`${d}/utlgevaluate'"
        cd ../..
done
