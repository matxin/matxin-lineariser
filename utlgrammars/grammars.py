from lrule import LinearisationRule
from printing import Printing

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
        return self.get_grammars()[local_configuration]

    def __str__(self):
        return self.__module__ + '.' + self.__class__.__name__ + ' = {\n' + \
        '  grammars = ' + Printing.shift_str(
                Printing.print_dict(
                    self.get_grammars(),
                    print_value=Printing.print_dict)) + '\n' + \
        '}'
