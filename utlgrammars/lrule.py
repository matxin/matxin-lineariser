from xml.etree import ElementTree

from lconfiguration import LocalConfiguration
from printing import Printing
from word import Word


class LinearisationRule:
    @classmethod
    def deserialise(cls, grammars, def_rule_etree):
        probability = float(def_rule_etree.get('p'))
        head_node_etree = def_rule_etree.find('NODE')
        linearisation_rule = {}

        for node_etree in head_node_etree.findall('NODE'):
            linearisation_rule[int(node_etree.get('ord'))] = (
                node_etree.get('si'), Word.deserialise(node_etree))

        head_node = (head_node_etree.get('si'),
                     Word.deserialise(head_node_etree))
        dependents = list(linearisation_rule.values())
        dependents.sort()
        local_configuration = LocalConfiguration(head_node[0], head_node[1],
                                                 tuple(dependents))
        head_node_ord = int(head_node_etree.get('ord'))
        linearisation_rule[head_node_ord] = head_node
        linearisation_rule = list(linearisation_rule.items())
        linearisation_rule.sort()
        head_node_index = linearisation_rule.index((head_node_ord, head_node))
        linearisation_rule = LinearisationRule(
            [value for key, value in linearisation_rule[:head_node_index]],
            [value for key, value in linearisation_rule[head_node_index + 1:]])
        print('deserialise')
        print('local_configuration = ' + str(local_configuration))
        print('linearisation_rule = ' + str(linearisation_rule))

        try:
            grammars.get_grammars()[local_configuration][
                probability] = linearisation_rule
        except (KeyError):
            grammars.get_grammars()[local_configuration] = {}
            grammars.get_grammars()[local_configuration][
                probability] = linearisation_rule

    def __init__(self, insert, append):
        self.insert = insert
        self.append = append

    def get_insert(self):
        return self.insert

    def get_append(self):
        return self.append

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  insert = ' + Printing.shift_str(Printing.print_list(self.get_insert(), print_item=self.print_edge)) + '\n' + \
                '  append = ' + Printing.shift_str(Printing.print_list(self.get_append(), print_item=self.print_edge)) + '\n' + \
                '}'

    @classmethod
    def print_edge(cls, edge):
        return Printing.print_tuple(edge, print_item=[repr, str])
