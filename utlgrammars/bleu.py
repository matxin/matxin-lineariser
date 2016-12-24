from lineariser import Lineariser
from sentence import Sentence

import nltk

from argparse import ArgumentParser
from sys import stdin
import traceback

try:
    argument_parser = ArgumentParser()
    argument_parser.add_argument('xml')
    arguments = argument_parser.parse_args()
    lineariser = Lineariser()

    with open(arguments.xml) as xml:
        lineariser.deserialise(xml)

    references = []
    hypotheses = []

    iter_ = (sentence for sentence in Sentence.deserialise(stdin))

    while True:
        try:
            sentence = next(iter_)
        except (StopIteration):
            break
        except (AttributeError):
            traceback.print_exc()

        reference = list(sentence.get_sentence().items())
        reference.sort()
        references.append([value.get_form().lower() for key, value in reference])
        sentence.linearise(lineariser, 1)
        hypotheses.extend(sentence.get_strings())

    print(nltk.translate.bleu_score.corpus_bleu(references, hypotheses))
except (AttributeError):
    traceback.print_exc()
