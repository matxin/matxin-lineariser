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
    """Write a sentence to an SGML file *file* to be used by Moses SMT's MT
    evaluation scorer.

    *id_* is the sentence's position in the treebank.  The first sentence's
    position would be 1.
    *seg* is a list of the sentence's FORMs.
    """
    print('<seg id="' + str(id_) + '">' + ' '.join(seg) + '</seg>', file=file)


def format_statistic(statistic):
    """Return a string of *statistic* to four decimal places."""
    return '{:.4f}'.format(statistic)


def print_statistics(list_):
    """Print the (1) size, (2) minimum value, (3) maximum value, (4) range, (5)
    median, (6) first quartile, (7) third quartile, (8) inter-quartile range,
    (9) mean, and (10) standard deviation of a sample.

    *list_* is the sample.
    """
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


BLEU_SCORE = re.compile(b'BLEU score = ([01]\.\d{4,4})')


def get_sample_corpus_linearisation_bleu_score(arguments, treebank,
                                               lineariser):
    """Linearise the corpus with the lineariser *lineariser* and score the
    linearisation with Moses SMT's MT evaluation scorer.

    *arguments* contain the command-line arguments passed to the program.
    *treebank* is a list of all the corpus' sentences, each as a Sentence
    object.

    Returns the linearisation's BLEU score.

    subprocess.CalledProcessError is raised if the return of Moses-SMT's MT
    evaluation scorer is non-zero.

    """
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

    try:
        return float(
            BLEU_SCORE.search(
                subprocess.check_output(
                    [
                        'perl', arguments.mteval, '-r', arguments.ref_file,
                        '-s', arguments.src_file, '-t', arguments.tst_file
                    ],
                    stderr=subprocess.STDOUT)).group(1))
    except subprocess.CalledProcessError as called_process_error:
        print(file=stderr)

        for line in called_process_error.output.split(b'\n'):
            print(
                '    [' + called_process_error.cmd[0] + '] ' +
                line.decode('utf-8'),
                file=stderr)

        raise


END_FORMAT_STR = 'corpus linearisation BLEU score = {:.4f}'


def print_sample(format_str, sample, arguments, treebank, lineariser,
                 corpus_linearisation_bleu_scores):
    print(format_str.format(sample), end='', file=stderr, flush=True)
    corpus_linearisation_bleu_score = get_sample_corpus_linearisation_bleu_score(
        arguments, treebank, lineariser)
    corpus_linearisation_bleu_scores.append(corpus_linearisation_bleu_score)
    print(
        END_FORMAT_STR.format(corpus_linearisation_bleu_score),
        file=stderr,
        flush=True)


def main():
    print()
    print('Corpus Sentence Length Statistics')
    print('=================================')
    argument_parser = ArgumentParser()
    argument_parser.add_argument(
        'data', help='the name of the data file to use for linearization')
    argument_parser.add_argument(
        'ref_file',
        help='the name of the file to use as the <ref_file> for Moses SMT\'s MT evaluation scorer'
    )
    argument_parser.add_argument(
        'src_file',
        help='the name of the file to use as the <src_file> for Moses SMT\'s MT evaluation scorer'
    )
    argument_parser.add_argument(
        'trglang', help='the target language\'s ISO 639-1 two-letter code')
    argument_parser.add_argument(
        '--figure-1',
        help='the name of the file to write the corpus sentence length frequency histogram to'
    )
    argument_parser.add_argument(
        'tst_file',
        help='the name of the file to use as the <tst_file> for Moses SMT\'s MT evaluation scorer'
    )
    argument_parser.add_argument(
        'mteval', help='the filename of Moses SMT\'s MT evaluation scorer')
    argument_parser.add_argument(
        'n',
        type=int,
        help='the number of corpus linearisation BLEU scores to get')
    argument_parser.add_argument(
        '--figure-2',
        help='the name of the file to write the corpus linearisation BLEU score frequency histogram to'
    )
    argument_parser.add_argument(
        '--projectivise',
        action='store_true',
        help='projectivise each sentence before linearising it')
    arguments = argument_parser.parse_args()
    lineariser = Lineariser()

    with open(arguments.data) as data:
        lineariser.deserialise(data)

    corpus_sentence_lengths = []
    treebank = []

    for sentence in Sentence.deserialise(stdin):
        if arguments.projectivise:
            sentence.projectivise()

        treebank.append(sentence)

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

    print_statistics(corpus_sentence_lengths)
    print()
    pyplot.figure()
    pyplot.hist(corpus_sentence_lengths)
    pyplot.title('Corpus Sentence Length Frequency Histogram')
    pyplot.xlabel('Sentence Length (# of Words)')
    pyplot.ylabel('# of Sentences')

    if arguments.figure_1 is not None:
        pyplot.savefig(arguments.figure_1)

    n = '{:,}'.format(arguments.n)
    sample_format_str = '{:>' + str(len(n)) + ',}'
    format_str = 'Sample ' + sample_format_str + ' of ' + n + ': '
    corpus_linearisation_bleu_scores = []
    stdout.flush()
    print_sample(format_str, 1, arguments, treebank, lineariser,
                 corpus_linearisation_bleu_scores)
    print(file=stderr)
    stderr.flush()
    print('coverage = ' + format_statistic(hypothesis.coverage.get_coverage()))
    print()

    if arguments.n == 1:
        print('BLEU score = ' + format_statistic(
            corpus_linearisation_bleu_scores[0]))
        print()
        return

    stdout.flush()

    for sample in range(2, arguments.n + 1):
        print_sample(format_str, sample, arguments, treebank, lineariser,
                     corpus_linearisation_bleu_scores)

    print(file=stderr)
    stderr.flush()
    print('Corpus Linearisation BLEU Score Statistics')
    print('==========================================')
    print_statistics(corpus_linearisation_bleu_scores)
    print()
    pyplot.figure()
    pyplot.hist(corpus_linearisation_bleu_scores)
    pyplot.title('Corpus Linearisation BLEU Score Frequency Histogram')
    pyplot.xlabel('BLEU Score')
    pyplot.ylabel('# of Samples')

    if arguments.figure_2 is not None:
        pyplot.savefig(arguments.figure_2)


if __name__ == '__main__':
    main()
