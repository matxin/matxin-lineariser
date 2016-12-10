from lineariser import Lineariser
from printing import Printing
from sentence import Sentence

from argparse import ArgumentParser
import sys

argument_parser = ArgumentParser()
argument_parser.add_argument(
    '-q', '--quiet', action='store_const', const=1, default=0, dest='quiet')
argument_parser.add_argument('xml')
arguments = argument_parser.parse_args()
corpus = [sentence for sentence in Sentence.deserialise(sys.stdin)]
lineariser = Lineariser()

with open(arguments.xml) as xml:
    lineariser.deserialise(xml)

for sentence in corpus:
    sentence.linearise(lineariser, arguments.quiet)

print('corpus is ' + Printing.print_list(corpus))
