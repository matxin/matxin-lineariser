from lineariser import Lineariser
from printing import Printing
from sentence import Sentence

from argparse import ArgumentParser
from pprint import PrettyPrinter
from sys import stdin, stdout, stderr

argument_parser = ArgumentParser()
argument_parser.add_argument(
    '-q', '--quiet', action='store_const', const=1, default=0, dest='quiet')
argument_parser.add_argument(
    '-v', '--verbose', action='store_true', dest='verbose')
argument_parser.add_argument('xml')
arguments = argument_parser.parse_args()
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

pretty_printer = PrettyPrinter()

for sentence in Sentence.deserialise(stdin):
    sentence.linearise(lineariser, arguments.quiet)

    if arguments.verbose:
        print('sentence = ' + str(sentence), file=stderr)
        print(
            'sentence.get_linearisations() = ' + Printing.print_list(
                sentence.get_linearisations(), print_item=Printing.print_list),
            file=stderr)
        stderr.flush()

    pretty_printer.pprint(sentence.get_strings())
    stdout.flush()
