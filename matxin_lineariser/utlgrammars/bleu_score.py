from nltk.translate.bleu_score import Fraction, modified_precision

from collections import Counter

import functools
import math
import operator
import sys


def get_bleu_score(weight, ps):
    return math.pow(functools.reduce(operator.mul, ps, 1), weight)


class SentenceBleuScore:
    def __init__(self, reference, hypothesis):
        hypothesis_len_ = len(hypothesis)
        self.p_numerators = {}
        self.p_denominators = {}

        if hypothesis_len_ < 4:
            weight = 1.0 / hypothesis_len_
            ps = self.get_modified_precision_scores(
                reference, hypothesis, n_max_=hypothesis_len_)
        else:
            weight = 0.25
            ps = self.get_modified_precision_scores(reference, hypothesis)

        self.s = get_bleu_score(weight, ps)

    def get_p_numerators(self):
        return self.p_numerators

    def get_p_denominators(self):
        return self.p_denominators

    def get(self):
        return self.s

    def __eq__(self, other):
        return isinstance(other,
                          SentenceBleuScore) and self.get() == other.get()

    def __hash__(self):
        return hash(self.s)

    def __lt__(self, other):
        if not isinstance(other, SentenceBleuScore):
            raise TypeError

        return self.get() < other.get()

    def get_modified_precision_scores(self, reference, hypothesis, n_max_=4):
        ps = []

        for i in range(2, n_max_ + 1):
            p_i = modified_precision([reference], hypothesis, i)
            self.get_p_numerators()[i] = p_i.numerator
            self.get_p_denominators()[i] = p_i.denominator
            ps.append(p_i)

        return ps


def get_corpus_bleu_score(sentence_bleu_scores):
    p_numerators = Counter()
    p_denominators = Counter()

    for s in sentence_bleu_scores:
        for i, p_numerator in enumerate(s.get_p_numerators()):
            p_numerators[i] += p_numerator

        for i, p_denominator in enumerate(s.get_p_denominators()):
            p_denominators[i] += p_denominator

    p_denominators_len_ = len(p_denominators)

    if p_denominators_len_ < 3:
        weight = 1.0 / (p_denominators_len_ + 1)
    else:
        weight = 0.25

    ps = [
        Fraction(
            p_numerators[i], p_denominators[i], _normalize=False)
        for i in range(2, p_denominators_len_ + 2)
    ]
    return get_bleu_score(weight, ps)
