class Word:
    def __init__(self, node_etree):
        self.upos = node_etree.get('pos')

        try:
            self.feats = node_etree.get('feats').split('|')
        except(AttributeError):
            self.feats = []

    def get_upos(self):
        return self.upos

    def get_feats(self):
        return self.feats

    def __repr__(self):
        return '<__main__.Word upos=' + repr(self.upos) + ' feats=' + \
                repr(self.feats) + '>'
