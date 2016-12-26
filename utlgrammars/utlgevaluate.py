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
argument_parser.add_argument('samples', type=int)
arguments = argument_parser.parse_args()
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

treebank = [sentence for sentence in Sentence.deserialise(stdin)]


def default_reference_function(reference):
    pass


def get_bleu_score_sample(reference_function=default_reference_function):
    bleu_scores = []

    for sentence in treebank:
        sentence_list = list(sentence.get_sentence().items())
        sentence_list.sort()
        reference = [word.get_form().lower() for _, word in sentence_list]
        reference_function(reference)
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


reference_len_list = []


def get_len_(reference):
    reference_len_list.append(len(reference))


bleu_scores = get_bleu_score_sample(reference_function=get_len_)
print()
print('coverage = ' + str(hypothesis.coverage.get_coverage()))


def print_statistics(list_):
    min_ = min(list_)
    print('minimum = ' + format_statistic(min_))
    max_ = max(list_)
    print('maximum = ' + format_statistic(max_))
    print('range = ' + format_statistic(max_ - min_))
    print('mean = ' + format_statistic(numpy.mean(list_)))
    print('standard deviation = ' + format_statistic(numpy.std(list_)))
    print('median = ' + format_statistic(numpy.median(list_)))
    q_1 = numpy.percentile(list_, 25)
    print('first quartile = ' + format_statistic(q_1))
    q_3 = numpy.percentile(list_, 75)
    print('third quartile = ' + format_statistic(q_3))
    print('inter-quartile range = ' + format_statistic(q_3 - q_1))


def format_statistic(statistic):
    return '{0:.4f}'.format(statistic)


print()
print('sentence lengths')
print('================')
print_statistics(reference_len_list)
pyplot.figure()
pyplot.hist(reference_len_list, bins=10, range=(0, 50))
pyplot.title('Reference Length Frequency Histogram')
pyplot.xlabel('Reference Length (# of Words)')
pyplot.ylabel('# of References')
print()
print('bleu scores')
print('===========')
print_statistics(bleu_scores)
pyplot.figure()
pyplot.hist(bleu_scores, bins=10, range=(0.5, 1.0))
pyplot.title('BLEU Score Frequency Histogram')
pyplot.xlabel('BLEU Score')
pyplot.ylabel('# of References')
bleu_score_sample_means = [numpy.mean(bleu_scores)]
samples = '{:,}'.format(arguments.samples)
sample_format_str = '{:>' + str(len(samples)) + ',}'
precision = str(max(0, len(str(1.0 / arguments.samples)) - 4))
percent_format_str = '{:>' + str(len(('{:.' + precision + '%}').format(
    1))) + '.' + precision + '%}'
format_str = '\rsample ' + sample_format_str + ' of ' + samples + ' (' + percent_format_str + ')'
print(flush=True)

for sample in range(1, arguments.samples):
    print(
        format_str.format(sample, sample / float(arguments.samples)),
        end='',
        file=stderr,
        flush=True)
    bleu_score_sample_means.append(numpy.mean(get_bleu_score_sample()))

print(end='\n\n', file=stderr)
print('bleu score sample means')
print('=======================')
print_statistics(bleu_score_sample_means)
print()
pyplot.show()
