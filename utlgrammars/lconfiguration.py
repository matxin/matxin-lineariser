from printing import Printing


class LocalConfiguration:
    def __init__(self, deprel, word, dependents):
        self.deprel = deprel
        self.word = word
        self.dependents = dependents

    def __hash__(self):
        return hash((self.get_word(), self.get_dependents()))

    def get_word(self):
        return self.word

    def get_dependents(self):
        return self.dependents

    def __eq__(self, other):
        if type(other) is not type(self):
            return False

        if self.get_deprel() is not None and other.get_deprel() is not None:
            if self.get_deprel() != other.get_deprel():
                return False

        if self.get_word() != other.get_word():
            return False

        return self.get_dependents() == other.get_dependents()

    def get_deprel(self):
        return self.deprel

    def __lt__(self, other):
        if type(other) is not type(self):
            raise TypeError

        if self.get_deprel() is not None and other.get_deprel() is not None:
            if self.get_deprel() != other.get_deprel():
                return self.get_deprel() < other.get_deprel()

        if self.get_word() != other.get_word():
            return self.get_word() < other.get_word()

        return self.get_dependents() < other.get_dependents()

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  deprel = ' + repr(self.get_deprel()) + '\n' + \
                '  word = ' + Printing.shift_str(
                        str(self.get_word())) + '\n' + \
                '  dependents = ' + Printing.shift_str(Printing.print_frozenset(self.get_dependents(), print_item=self.print_edge)) + '\n' + \
                '}'

    @classmethod
    def print_edge(cls, edge):
        return Printing.print_tuple(edge, print_item=[repr, str])
