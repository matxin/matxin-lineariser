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
    reference = ' '.join([value.get_form().lower() for key, value in reference])
    print(reference)
    stdout.flush()
    sentence.linearise(lineariser, 1)
    hypothesis = sentence.get_strings()[0]
    print(hypothesis, file=stderr)
    stderr.flush()

    if len(reference) != len(hypothesis):
        print('reference = ' + repr(reference))
        print('hypothesis = ' + repr(hypothesis))
        sys.exit()
