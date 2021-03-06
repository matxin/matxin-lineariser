from .printing import Printing


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
        return isinstance(other, LocalConfiguration) and self.get_deprel(
        ) == other.get_deprel() and self.get_word() == other.get_word(
        ) and self.get_dependents() == other.get_dependents()

    def get_deprel(self):
        return self.deprel

    def __lt__(self, other):
        if not isinstance(other, LocalConfiguration):
            raise TypeError

        if self.get_deprel() is not None and other.get_deprel() is not None:
            if self.get_deprel() != other.get_deprel():
                return self.get_deprel() < other.get_deprel()

        if self.get_word() != other.get_word():
            return self.get_word() < other.get_word()

        if self.get_dependents() != other.get_dependents():
            return self.get_dependents() < other.get_dependents()

        return False

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  deprel = ' + repr(self.get_deprel()) + '\n' + \
                '  word = ' + Printing.shift_str(str(self.get_word())) + '\n' + \
                '  dependents = ' + Printing.shift_str(Printing.print_list(self.get_dependents(), print_item=self.print_dependent)) + '\n' + \
                '}'

    @classmethod
    def print_dependent(cls, edge):
        return Printing.print_tuple(edge, print_item=[repr, str])
