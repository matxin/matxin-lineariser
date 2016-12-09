from agenda import Agenda
from lconfiguration import LocalConfiguration
from printing import Printing
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

        # to-do
        # =====
        # - support multiword integer ranges (e.g. 1-2 or 3-5)
        # - also support empty nodes (e.g. 5.1)
        self.id_ = int(fields.pop())

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
        except (KeyError):
            sentence.root = self

    def get_hypotheses(self):
        return self.hypotheses

    def get_local_configuration(self):
        """Return a set of the dependents' deprel-s."""
        return LocalConfiguration(
            self.get_deprel(),
            self.get_word(),
            frozenset({(dependent.get_deprel(), dependent.get_word())
                       for dependent in self.get_dependents()}))

    def get_deprel(self):
        return self.deprel

    def get_word(self):
        return self.word

    def get_dependents(self):
        return self.dependents

    def get_rules(self):
        return self.rules

    def get_agenda(self):
        return self.agenda

    def get_sorted_rules(self):
        return self.sorted_rules

    def apply_rule(self, rule):
        """Return a list of self and dependents ordered according to
        rule."""
        raise NotImplementedError

    def train(self, grammars):
        """Increment the rule corresponding to the local configuration
        in the likewise-corresponding Grammar in grammars."""
        raise NotImplementedError

    def get_local_linearisation(self):
        """Return a list of dependents' PoS-s ordered by their id-s."""
        raise NotImplementedError

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  id = ' + str(self.get_id()) + '\n' + \
                '  word = ' + Printing.shift_str(
                        str(self.get_word())) + '\n' + \
                '  head = ' + str(self.get_head()) + '\n' + \
                '  deprel = ' + repr(self.get_deprel()) + '\n' + \
                '}'
