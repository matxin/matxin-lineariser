import nltk

from argparse import ArgumentParser

argument_parser = ArgumentParser()
argument_parser.add_argument('references')
argument_parser.add_argument('hypotheses')
arguments = argument_parser.parse_args()

with open(arguments.references) as references, \
     open(arguments.hypotheses) as hypotheses:
    print(nltk.translate.bleu_score.corpus_bleu(
        [line[:-1].split(' ') for line in references],
        [line[:-1].split(' ') for line in hypotheses],
        weights=(1.0, )))
