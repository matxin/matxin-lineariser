from printing import Printing

from sys import stderr


class Word:
    def __init__(self, upostag):
        self.upostag = upostag
        self.feats = frozenset({})
        self.lemma = None

    def add_lemma(self, lemma):
        self.lemma = lemma

    @classmethod
    def deserialise(cls, node_etree):
        upostag = node_etree.get('pos')
        feats = node_etree.get('feats')
        word = Word(upostag)
        word.parse_feats(feats)
        word.add_lemma(node_etree.get('lem'))
        return word

    def parse_feats(self, feats):
        if feats is None:
            self.feats = frozenset({})
        else:
            self.feats = frozenset(
                tuple(feat.split('=')) for feat in feats.split('|'))

    def __hash__(self):
        return hash((self.get_upostag(), self.get_feats(), self.get_lemma()))

    def get_upostag(self):
        return self.upostag

    def get_lemma(self):
        return self.lemma

    def __eq__(self, other):
        return type(self) is type(other) and self.get_upostag(
        ) == other.get_upostag() and self.get_feats() == other.get_feats(
        ) and self.get_lemma() == other.get_lemma()

    def get_feats(self):
        return self.feats

    def __lt__(self, other):
        if type(other) is not type(self):
            raise TypeError

        if self.get_upostag() != other.get_upostag():
            return self.get_upostag() < other.get_upostag()

        if len(self.get_feats()) != 0 and len(other.get_feats()) != 0:
            if self.get_feats() != other.get_feats():
                return self.get_feats() < other.get_feats()

        if self.get_lemma() is not None and other.get_lemma() is not None:
            if self.get_lemma() != other.get_lemma():
                return self.get_lemma() < other.get_lemma()

        return False

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  upostag = ' + repr(self.get_upostag()) + '\n' + \
                '  feats = ' + Printing.shift_str(Printing.print_list(self.get_feats(), print_item=Printing.print_tuple)) + '\n' + \
                '  lemma = ' + repr(self.get_lemma()) + '\n' + \
                '}'


def word_eq(a, b):
    if type(a) is not type(b):
        return False

    if a.get_upostag() != b.get_upostag():
        return False

    if len(a.get_feats()) != 0 and len(b.get_feats()) != 0:
        if a.get_feats() != b.get_feats():
            return False

    if a.get_lemma() is not None and b.get_lemma() is not None:
        if a.get_lemma() != b.get_lemma():
            return False

        print('a.get_lemma() = ' + a.get_lemma(), file=stderr)
        print('b.get_lemma() = ' + b.get_lemma(), file=stderr)

    return True
