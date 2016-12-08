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

    def get_upostag(self):
        return self.upostag

    def get_feats(self):
        return self.feats

    def __repr__(self):
        return '<' + self.__module__ + '.' + self.__class__.__name__ + \
                ' upostag=' + repr(self.upostag) + \
                ' feats=' + repr(self.feats) + '>'

    def __eq__(self, other):
        if type(other) is not type(self):
            return False

        if self.get_upostag() != other.get_upostag():
            return False

        if len(self.get_feats()) == 0 or len(other.get_feats()) == 0:
            return True

        return self.get_feats() == other.get_feats()
