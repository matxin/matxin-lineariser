from matxin_lineariser.utlgrammars.lineariser import Lineariser
from matxin_lineariser.utlgrammars.sentence import Sentence
import matxin_lineariser.utlgrammars.hypothesis as hypothesis

from matplotlib import pyplot
from nltk.translate import bleu_score
import numpy

from argparse import ArgumentParser
from sys import stdin, stdout, stderr
import re
import subprocess


def print_seg(id_, seg, file):
    print('<seg id="' + str(id_) + '">' + ' '.join(seg) + '</seg>', file=file)


def format_statistic(statistic):
    return '{0:.4f}'.format(statistic)


def print_statistics(list_):
    print('size = ' + str(len(list_)))
    min_ = min(list_)
    print('minimum = ' + format_statistic(min_))
    max_ = max(list_)
    print('maximum = ' + format_statistic(max_))
    print('range = ' + format_statistic(max_ - min_))
    print('median = ' + format_statistic(numpy.median(list_)))
    q_1 = numpy.percentile(list_, 25, interpolation='midpoint')
    print('first quartile = ' + format_statistic(q_1))
    q_3 = numpy.percentile(list_, 75, interpolation='midpoint')
    print('third quartile = ' + format_statistic(q_3))
    print('inter-quartile range = ' + format_statistic(q_3 - q_1))
    print('mean = ' + format_statistic(numpy.mean(list_)))
    print('standard deviation = ' + format_statistic(numpy.std(list_, ddof=1)))


def get_sample_bleu_score():
    with open(arguments.tst_file, mode='w') as tst_file:
        print(
            '<tstset trglang="' + arguments.trglang +
            '" setid="1" srclang="any">',
            file=tst_file)
        print('<doc sysid="1" docid="1">', file=tst_file)

        for id_, sentence in enumerate(treebank, start=1):
            sentence.linearise(lineariser, 1, True)
            linearisation = sentence.get_linearisations()[0]
            hypothesis = [word.get_form().lower() for word in linearisation]
            print_seg(id_, hypothesis, tst_file)

        print('</doc>', file=tst_file)
        print('</tstset>', file=tst_file)

    completed_process = subprocess.run([
        'perl', arguments.mteval, '-r', arguments.ref_file, '-s',
        arguments.src_file, '-t', arguments.tst_file
    ],
                                       stdout=subprocess.PIPE)

    try:
        completed_process.check_returncode()
    except (subprocess.CalledProcessError):
        stdout.flush()
        stderr.flush()
        print(completed_process.stdout, flush=True)
        print(completed_process.stderr, file=stderr, flush=True)
        raise

    return float(BLEU_SCORE.search(completed_process.stdout).group(1))


def main():
    argument_parser = ArgumentParser()
    argument_parser.add_argument('xml')
    argument_parser.add_argument('ref_file')
    argument_parser.add_argument('src_file')
    argument_parser.add_argument('trglang')
    argument_parser.add_argument(
        'corpus_sentence_length_frequency_histogram_fname')
    argument_parser.add_argument('tst_file')
    argument_parser.add_argument('mteval')
    argument_parser.add_argument('n', type=int)
    argument_parser.add_argument(
        'corpus_linearisation_bleu_score_frequency_histogram_fname')
    arguments = argument_parser.parse_args()
    lineariser = Lineariser()

    with open(arguments.xml) as xml:
        lineariser.deserialise(xml)

    corpus_sentence_lengths = []
    treebank = [sentence for sentence in Sentence.deserialise(stdin)]

    with open(arguments.ref_file, mode='w') as ref_file, \
         open(arguments.src_file, mode='w') as src_file:
        print(
            '<refset trglang="' + arguments.trglang +
            '" setid="1" srclang="any">',
            file=ref_file)
        print('<doc sysid="ref" docid="1">', file=ref_file)
        print('<srcset setid="1" srclang="any">', file=src_file)
        print('<doc docid="1">', file=src_file)

        for id_, sentence in enumerate(treebank, start=1):
            sentence_list = list(sentence.get_wordlines().items())
            sentence_list.sort()
            reference = [word.get_form().lower() for _, word in sentence_list]
            corpus_sentence_lengths.append(len(reference))
            print_seg(id_, reference, ref_file)
            print_seg(id_, reference, src_file)

        print('</doc>', file=src_file)
        print('</srcset>', file=src_file)
        print('</doc>', file=ref_file)
        print('</refset>', file=ref_file)

    print()
    print('Corpus Sentence Length Statistics')
    print('=================================')
    print_statistics(corpus_sentence_lengths)
    pyplot.figure()
    pyplot.hist(corpus_sentence_lengths)
    pyplot.title('Corpus Sentence Length Frequency Histogram')
    pyplot.xlabel('Sentence Length (# of Words)')
    pyplot.ylabel('# of Sentences')
    pyplot.savefig(arguments.corpus_sentence_length_frequency_histogram_fname)
    BLEU_SCORE = re.compile(b'BLEU score = ([01]\.\d{4,4})')
    corpus_linearisation_bleu_scores = [get_sample_bleu_score()]
    print()
    print('coverage = ' + format_statistic(hypothesis.coverage.get_coverage()))
    print(flush=True)

    if arguments.n == 1:
        print('BLEU score = ' + format_statistic(
            corpus_linearisation_bleu_scores[0]))
        print()
        return

    n = '{:,}'.format(arguments.n)
    sample_format_str = '{:>' + str(len(n)) + ',}'
    precision = str(max(0, len(str(1.0 / arguments.n)) - 4))
    percent_format_str = '{:>' + str(
        len(('{:.' + precision + '%}').format(1))) + '.' + precision + '%}'
    format_str = '\rsample ' + sample_format_str + ' of ' + n + ' (' + percent_format_str + ')'

    for sample in range(2, arguments.n + 1):
        print(
            format_str.format(sample, sample / float(arguments.n)),
            end='',
            file=stderr,
            flush=True)
        corpus_linearisation_bleu_scores.append(get_sample_bleu_score())

    print(end='\n\n', file=stderr)
    print('Corpus Linearisation BLEU Score Statistics')
    print('==========================================')
    print_statistics(corpus_linearisation_bleu_scores)
    pyplot.figure()
    pyplot.hist(corpus_linearisation_bleu_scores)
    pyplot.title('Corpus Linearisation BLEU Score Frequency Histogram')
    pyplot.xlabel('BLEU Score')
    pyplot.ylabel('# of Samples')
    pyplot.savefig(
        arguments.corpus_linearisation_bleu_score_frequency_histogram_fname)
    print()


if __name__ == '__main__':
    main()
