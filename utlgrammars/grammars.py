from lrule import LinearisationRule

from xml.etree import ElementTree

class Grammars:
    def __init__(self):
        self.grammars = {}

    def deserialise(self, linearisation_rules_etree):
        for def_rule_etree in linearisation_rules_etree.findall('def-rule'):
            LinearisationRule.deserialise(self, def_rule_etree)

    def get_grammars(self):
        return self.grammars

    def get_grammar(self, local_configuration):
        return self.getgrammars()[local_configuration]
