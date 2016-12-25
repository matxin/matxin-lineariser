import hypothesis
from lineariser import Lineariser
from sentence import Sentence

from matplotlib import pyplot
from nltk.translate import bleu_score
import numpy

from argparse import ArgumentParser
from sys import stdin, stdout, stderr

argument_parser = ArgumentParser()
argument_parser.add_argument('xml')
argument_parser.add_argument('sample_size', type=int)
arguments = argument_parser.parse_args()
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

treebank = [sentence for sentence in Sentence.deserialise(stdin)]

def sample():
    bleu_scores = []

    for sentence in treebank:
        sentence_list = list(sentence.get_sentence().items())
        sentence_list.sort()
        reference = [word.get_form().lower() for _, word in sentence_list]
        references = [reference]
        sentence.linearise(lineariser, 1, True)
        linearisation = sentence.get_linearisations()[0]
        hypothesis = [word.get_form().lower() for word in linearisation]
        hypothesis_len = len(hypothesis)
        weights = (1.0 / hypothesis_len, ) * hypothesis_len
        bleu_scores.append(
            bleu_score.sentence_bleu(
                references, hypothesis, weights=weights))

    return bleu_scores

pyplot.figure(1)
pyplot.hist(sample(), bins=20, range=(0.5, 1.0))
print('coverage = ' + '{0:.4f}'.format(hypothesis.coverage.get_coverage()))
pyplot.title('Frequency Histogram of Sentence BLEU Scores')
pyplot.xlabel('Sentence BLEU Score')
pyplot.ylabel('# of Sentences')
bleu_scores = [numpy.mean(sample()) for _ in range(arguments.sample_size)]
print('bleu_score = ' + '{0:.4f}'.format(numpy.mean(bleu_scores)))
pyplot.figure(2)
pyplot.hist(bleu_scores, bins=5)
pyplot.title('Frequency Histogram of Average Sentence BLEU Scores')
pyplot.xlabel('Average Sentence BLEU Score')
pyplot.ylabel('# of Sentences')
pyplot.show()
