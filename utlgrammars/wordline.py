from agenda import Agenda
from lconfiguration import LocalConfiguration
from printing import Printing
from word import Word


class WordLine:
    def __init__(self):
        self.dependents = []
        self.hypotheses = []
        self.agenda = Agenda()

    def deserialise(self, line):
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
        self.word = Word(upostag)
        self.word.parse_feats(feats)
        self.word.add_lemma(fields.pop())
        self.form = fields.pop()

        # to-do
        # =====
        # - support multiword integer ranges (e.g. 1-2 or 3-5)
        # - also support empty nodes (e.g. 5.1)
        self.id_ = int(fields.pop())

    def deserialise_matxin(self, node_etree, maximum_ref = 0):
        self.node_etree = node_etree
        node_attributes = dict(node_etree.items())

        try:
            self.id_ = int(node_attributes.get('ref'))

            if self.get_id() > maximum_ref:
                maximum_ref = self.get_id()

            del node_attributes['ref']
        except (TypeError):
            self.id_ = None

        upostag = node_attributes['UPOSTAG']
        del node_attributes['UPOSTAG']
        self.deprel = node_attributes['si']
        del node_attributes['si']
        lemma = node_attributes['lem']
        del node_attributes['lem']

        for attribute in ['alloc', 'slem', 'smi', 'UpCase']:
            try:
                del node_attributes[attribute]
            except (KeyError):
                pass

        self.word = Word(upostag)
        self.word.feats = frozenset(node_attributes.items())
        self.word.add_lemma(lemma)

        for dependent_node_etree in node_etree.findall('NODE'):
            dependent = WordLine()
            ref = dependent.deserialise_matxin(dependent_node_etree)

            if ref > maximum_ref:
                maximum_ref = ref

            self.get_dependents().append(dependent)

        return maximum_ref

    def get_id(self):
        return self.id_

    def get_head(self):
        return self.head

    def add_edge(self, sentence, wordlines):
        try:
            wordlines[self.get_head()].get_dependents().append(self)
        except (KeyError):
            sentence.root = self

    def get_hypotheses(self):
        return self.hypotheses

    def get_local_configuration(self):
        """Return a set of the dependents' deprel-s."""
        dependents = [(dependent.get_deprel(), dependent.get_word())
                      for dependent in self.get_dependents()]
        dependents.sort()
        return LocalConfiguration(self.get_deprel(),
                                  self.get_word(), tuple(dependents))

    def get_deprel(self):
        return self.deprel

    def get_word(self):
        return self.word

    def get_dependents(self):
        return self.dependents

    def get_sorted_rules(self):
        return self.sorted_rules

    def get_agenda(self):
        return self.agenda

    def add_word(self, wordlines):
        wordlines[self.get_id()] = self

        for dependent in self.get_dependents():
            dependent.add_word(wordlines)

    def add_ref(self, maximum_ref):
        if self.get_id() is None:
            maximum_ref += 1
            self.id_ = maximum_ref

        for dependent in self.get_dependents():
            maximum_ref = dependent.add_ref(maximum_ref)

        return maximum_ref

    def get_form(self):
        return self.form

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  id = ' + str(self.get_id()) + '\n' + \
                '  word = ' + Printing.shift_str(str(self.get_word())) + '\n' + \
                '  head = ' + str(self.get_head()) + '\n' + \
                '  deprel = ' + repr(self.get_deprel()) + '\n' + \
                '}'
