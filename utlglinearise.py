from matxin_lineariser.utlgrammars.lineariser import Lineariser
from matxin_lineariser.utlgrammars.printing import Printing
from matxin_lineariser.utlgrammars.sentence import Sentence

from argparse import ArgumentParser
from pprint import PrettyPrinter
from sys import stdin, stdout, stderr
from xml.etree import ElementTree

argument_parser = ArgumentParser()
argument_parser.add_argument(
    '-m', '--matxin', action='store_true', dest='matxin')
argument_parser.add_argument(
    '-1', '--1-best', action='store_const', const=1, default=0, dest='n')
argument_parser.add_argument(
    '-v', '--verbose', action='store_true', dest='verbose')
argument_parser.add_argument(
    '-s', '--shuffle', action='store_true', dest='shuffle')
argument_parser.add_argument('--projectivise', action='store_true')
argument_parser.add_argument('xml')
arguments = argument_parser.parse_args()
lineariser = Lineariser()


def print_verbose(sentence):
    print('sentence = ' + str(sentence), file=stderr)
    print(
        'sentence.get_linearisations() = ' + Printing.print_list(
            sentence.get_linearisations(), print_item=Printing.print_list),
        file=stderr)
    stderr.flush()


def main():
    with open(arguments.xml) as xml:
        lineariser.deserialise(xml)

    if arguments.matxin:
        etree = ElementTree.parse(stdin)
        corpus_etree = etree.getroot()

        for sentence in Sentence.deserialise_matxin(corpus_etree):
            if arguments.projectivise:
                sentence.projectivise()

            sentence.linearise(lineariser, arguments.n, arguments.shuffle)
            for ref, wordline in enumerate(
                    sentence.get_linearisations()[0], start=1):
                wordline.node_etree.set('ord', str(ref))

            if arguments.verbose:
                print_verbose(sentence)

        etree.write(stdout, encoding='unicode', xml_declaration=True)
        return

    if arguments.n != 1:
        pretty_printer = PrettyPrinter()

    for sentence in Sentence.deserialise(stdin):
        if arguments.projectivise:
            sentence.projectivise()

        sentence.linearise(lineariser, arguments.n, arguments.shuffle)

        if arguments.verbose:
            print_verbose(sentence)

        if arguments.n == 1:
            print(sentence.get_strings()[0], flush=True)
            continue

        pretty_printer.pprint(sentence.get_strings())
        stdout.flush()

if __name__ == '__main__':
    main()
