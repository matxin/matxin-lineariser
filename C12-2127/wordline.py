from agenda import Agenda

class WordLine:
    def __init__(self, line):
        fields = line.split('\t')

        # parse fields in reverse by popping

        misc = fields.pop()

        if misc == '_':
            self.misc = None
        else:
            self.misc = dict(field.split('=') for field in misc.split('|'))

        deps = fields.pop()

        if deps == '_':
            self.deps = None
        else:
            self.deps = dict(dep.split(':') for dep in deps.split('|'))

        self.deprel = fields.pop()
        self.head = int(fields.pop())
        feats = fields.pop()

        if feats == '_':
            self.feats = None
        else:
            self.feats = dict(feat.split('=') for feat in feats.split('|'))

        xpostag = fields.pop()

        if xpostag == '_':
            self.xpostag = None
        else:
            self.xpostag = xpostag

        self.upostag = fields.pop()
        self.lemma = fields.pop()
        self.form = fields.pop()
        self.id_ = int(fields.pop()) # to-do: support multiword integer ranges
                                     # e.g. 1-2 or 3-5
                                     # to-do: also support empty nodes
                                     # e.g. 5.1

        self.dependents = []

        self.hypotheses = []
        self.agenda = Agenda()

    def get_id(self):
        return self.id_

    def get_head(self):
        return self.head

    def add_edge(self, sentence):
        sentence[self.get_head()].dependents.append(self)

    def get_local_configuration(self):
        """Return a set of the dependents' deprel-s."""
        raise NotImplementedError

    def get_local_linearization(self):
        """Return a list of dependents' PoS-s ordered by their id-s."""
        raise NotImplementedError

    def apply_rule(self, rule):
        """Return a list of self and dependents ordered according to
        rule."""
        raise NotImplementedError
