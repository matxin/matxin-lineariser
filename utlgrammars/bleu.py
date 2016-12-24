import hypothesis
from lineariser import Lineariser
from sentence import Sentence

import nltk

from argparse import ArgumentParser
from sys import stdin, stdout, stderr
import traceback

argument_parser = ArgumentParser()
argument_parser.add_argument('xml')
arguments = argument_parser.parse_args()
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

references = []
hypotheses = []

for sentence in Sentence.deserialise(stdin):
    reference = list(sentence.get_sentence().items())
    reference.sort()
    references.append([value.get_form().lower() for key, value in reference])
    sentence.linearise(lineariser, 1)
    hypotheses.append(
        [item.get_form().lower() for item in sentence.get_linearisations()[0]])

print('bleu = ' + str(nltk.translate.bleu_score.corpus_bleu(
    references, hypotheses, weights=(1.0,))))
print('coverage = ' + str(float(hypothesis.numerator) / float(hypothesis.denominator)))
