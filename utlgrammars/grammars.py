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
        str_ = self.__module__ + '.' + self.__class__.__name__ + ' = {\n' + \
        '  grammars = ' + Printing.shift_str(
            Printing.print_dict(
                self.get_grammars(),
                print_key=self.print_local_configuration,
                print_value=self.print_grammar)) + '\n' + \
                '}'
        return str_

    @classmethod
    def print_local_configuration(cls, local_configuration):
        return Printing.print_tuple(
            local_configuration,
            print_item=[cls.print_edge, cls.print_dependents])

    @classmethod
    def print_edge(cls, edge):
        return Printing.print_tuple(edge, print_item=[repr, str])

    @classmethod
    def print_dependents(cls, dependents):
        return Printing.print_frozenset(dependents, print_item=cls.print_edge)

    @classmethod
    def print_grammar(cls, grammar):
        return Printing.print_dict(grammar, print_value=cls.print_rule)

    @classmethod
    def print_rule(cls, rule):
        return Printing.print_list(rule, print_item=cls.print_edge)
