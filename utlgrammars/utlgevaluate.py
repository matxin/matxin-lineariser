import hypothesis
from lineariser import Lineariser
from sentence import Sentence

import nltk

from argparse import ArgumentParser
from sys import stdin, stdout, stderr

argument_parser = ArgumentParser()
argument_parser.add_argument(
    '-s', '--shuffle', action='store_true', dest='shuffle')
argument_parser.add_argument('xml')
arguments = argument_parser.parse_args()
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

bleu = 0.0
len_sentences_ = 0

for sentence in Sentence.deserialise(stdin):
    len_sentences_ += 1
    reference = list(sentence.get_sentence().items())
    reference.sort()
    print('reference = ' + str(
        [value.get_form().lower() for key, value in reference]))
    sentence.linearise(lineariser, 1, arguments.shuffle)
    print('hypothesis = ' + str([
        item.get_form().lower() for item in sentence.get_linearisations()[0]
    ]))
    sentence_len_ = len(sentence.get_linearisations()[0])
    weights = (1.0 / float(sentence_len_) for x in range(sentence_len_))
    bleu += nltk.translate.bleu_score.sentence_bleu(
        [value.get_form().lower() for key, value in reference],
        [item.get_form().lower() for item in sentence.get_linearisations()[0]])
    print('bleu = ' + str(bleu))
    print('reference = ' + str(
        [value.get_form().lower() for key, value in reference]))
    print('hypothesis = ' + str([
        item.get_form().lower() for item in sentence.get_linearisations()[0]
    ]))
    raise StopIteration

print('bleu = ' + str(bleu / float(len_sentences_)))
print('coverage = ' + str(hypothesis.coverage.get_coverage()))
