from lineariser import Lineariser
from printing import Printing
from wordline import WordLine


class Sentence:
    @classmethod
    def deserialise(cls, conllu):
        sentence = {}

        for line in conllu:
            line = line[:-1]  # strip the trailing newline
            if line == '':
                yield Sentence(sentence)
            else:
                wordline = WordLine(line)
                sentence[wordline.get_id()] = wordline

        yield Sentence(sentence)

    def __init__(self, sentence):
        self.sentence = sentence

        for wordline in sentence.values():
            wordline.add_edge(self)

    def get_sentence(self):
        return self.sentence

    def linearise(self, lineariser, n=0):
        self.linearisations = lineariser.linearise_node(self.get_root(), n)

    def get_root(self):
        return self.root

    def get_linearisations(self):
        return self.linearisations

    def train(self, grammars):
        """Train grammars on all the nodes."""
        raise NotImplementedError

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  sentence = ' + Printing.shift_str(Printing.print_dict(self.get_sentence())) + '\n' + \
                '  root = ' + Printing.shift_str(str(self.get_root())) + '\n' + \
                '  linearisations = ' + Printing.shift_str(Printing.print_list(self.get_linearisations(), print_item=Printing.print_list)) + '\n' + \
                '}'
