from xml.etree import ElementTree

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

        local_configuration = (
            (head_node_etree.get('si'), Word.deserialise(head_node_etree)),
            frozenset([value for value in linearisation_rule.values()]))
        linearisation_rule[int(head_node_etree.get('ord'))] = (
            head_node_etree.get('si'), Word.deserialise(head_node_etree))
        linearisation_rule = [value for value in linearisation_rule.values()]

        try:
            grammars.get_grammars()[local_configuration][
                probability] = linearisation_rule
        except (KeyError):
            grammars.get_grammars()[local_configuration] = {}
            grammars.get_grammars()[local_configuration][
                probability] = linearisation_rule
