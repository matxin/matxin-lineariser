from printing import Printing


class Word:
    @classmethod
    def deserialise(cls, node_etree):
        upostag = node_etree.get('pos')
        feats = node_etree.get('feats')
        return Word(upostag, feats)

    def __init__(self, upostag, feats):
        self.upostag = upostag

        if feats is None:
            self.feats = {}
        else:
            self.feats = dict(feat.split('=') for feat in feats.split('|'))

    def __hash__(self):
        return hash(self.get_upostag())

    def get_upostag(self):
        return self.upostag

    def __eq__(self, other):
        if type(other) is not type(self):
            return False

        if self.get_upostag() != other.get_upostag():
            return False

        if len(self.get_feats()) == 0 or len(other.get_feats()) == 0:
            return True

        return self.get_feats() == other.get_feats()

    def get_feats(self):
        return self.feats

    def __lt__(self, other):
        if type(other) is not type(self):
            raise TypeError

        if self.get_upostag() != other.get_upostag():
            return self.get_upostag() < other.get_upostag()

        if len(self.get_feats()) != 0 and len(other.get_feats()) != 0:
            return self.get_feats() < other.get_feats()

        return False

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  upostag = ' + repr(self.get_upostag()) + '\n' + \
                '  feats = ' + Printing.shift_str(Printing.print_dict(self.get_feats(), print_key=repr, print_value=repr)) + '\n' + \
                '}'
