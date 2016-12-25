from lineariser import Lineariser
from printing import Printing
from wordline import WordLine

import re
import sys

CONLLU_COMMENT = re.compile('\s*#')


class Sentence:
    @classmethod
    def deserialise(cls, conllu):
        sentence_bad = False
        sentence = {}

        for line in conllu:
            line = line[:-1]  # strip the trailing newline

            if CONLLU_COMMENT.match(line) is not None:
                continue

            if line == '':
                if sentence_bad:
                    sentence_bad = False
                    sentence = {}
                    continue

                yield Sentence(sentence)
                sentence = {}
                continue

            try:
                wordline = WordLine(line)
            except:
                sentence_bad = True
                continue

            sentence[wordline.get_id()] = wordline

        if len(sentence) != 0:
            yield Sentence(sentence)

    def __init__(self, sentence):
        self.sentence = sentence

        for wordline in sentence.values():
            wordline.add_edge(self)

    def get_sentence(self):
        return self.sentence

    def linearise(self, lineariser, n=0, shuffle=False):
        self.linearisations = lineariser.linearise_node(self.get_root(), n,
                                                        shuffle)
        self.strings = [
            ' '.join([word.get_form().lower() for word in linearisation])
            for linearisation in self.get_linearisations()
        ]

    def get_root(self):
        return self.root

    def get_linearisations(self):
        return self.linearisations

    def get_strings(self):
        return self.strings

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  sentence = ' + Printing.shift_str(Printing.print_dict(self.get_sentence())) + '\n' + \
                '  root = ' + Printing.shift_str(str(self.get_root())) + '\n' + \
                '}'
