from agenda import Agenda
from word import Word

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
            feats = None

        xpostag = fields.pop()

        if xpostag == '_':
            self.xpostag = None
        else:
            self.xpostag = xpostag

        upostag = fields.pop()
        self.word = Word(upostag, feats)
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
        try:
            sentence.get_sentence()[self.get_head()].dependents.append(self) 
        except(KeyError):
            sentence.root = self

    def get_local_configuration(self):
        """Return a set of the dependents' deprel-s."""
        return ((self.get_deprel(), self.get_word()), \
                frozenset((dependent.get_deprel(), dependent.get_word()) \
                for dependent in self.get_dependents()))

    def get_local_linearization(self):
        """Return a list of dependents' PoS-s ordered by their id-s."""
        raise NotImplementedError

    def apply_rule(self, rule):
        """Return a list of self and dependents ordered according to
        rule."""
        raise NotImplementedError

    def train(self, grammars):
        """Increment the rule corresponding to the local configuration
        in the likewise-corresponding Grammar in grammars."""
        raise NotImplementedError

    def get_dependents(self):
        """Return a deep copy of dependens."""
        return self.dependents[:]

    def get_deprel(self):
        return self.deprel

    def get_word(self):
        return self.word

    def get_agend(self):
        return self.agenda
