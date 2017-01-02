from lineariser import Lineariser
from printing import Printing
from wordline import WordLine

import re
import sys

CONLLU_COMMENT = re.compile('\s*#')


class Sentence:
    def __init__(self):
        pass

    @classmethod
    def deserialise(cls, conllu):
        bad = False
        wordlines = {}

        for line in conllu:
            line = line[:-1]  # strip the trailing newline

            if CONLLU_COMMENT.match(line) is not None:
                continue

            if line == '':
                if bad:
                    bad = False
                    wordlines = {}
                    continue

                sentence = Sentence()

                for wordline in wordlines.values():
                    wordline.add_edge(sentence, wordlines)

                yield sentence
                wordlines = {}
                continue

            wordline = WordLine()

            try:
                wordline.deserialise(line)
            except:
                bad = True
                continue

            wordlines[wordline.get_id()] = wordline

    @classmethod
    def deserialise_matxin(cls, corpus_etree):
        for sentence_etree in corpus_etree.findall('SENTENCE'):
            sentence = Sentence()
            root_node_etree = corpus_etree.find('NODE')
            root = WordLine()
            maximum_ref = root.deserialise_matxin(root_node_etree)
            sentence.root = root
            sentence.get_root().add_ref(maximum_ref)
            yield sentence

    def get_wordlines(self):
        wordlines = {}
        self.get_root().add_word(wordlines)
        return wordlines

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
                '  wordlines = ' + Printing.shift_str(Printing.print_dict(self.get_wordlines())) + '\n' + \
                '  root = ' + Printing.shift_str(str(self.get_root())) + '\n' + \
                '}'
