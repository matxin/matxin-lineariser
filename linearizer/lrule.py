from xml.etree import ElementTree

class LinearisationRule:
    @classmethod
    def deserialise(cls, grammars, def_rule_etree):
        probability = float(def_rule_etree.get('p'))
        node_etree = def_rule_etree.find('NODE')
        linearisation_rule = {int(node_etree.get('ord')): \
                (node_etree.get('si'), node_etree.get('pos'))}

        for node_etree in node_etree.findall('NODE'):
            linearisation_rule[int(node_etree.get('ord'))] = \
                    (node_etree.get('si'), node_etree.get('pos'))

        local_configuration = \
                frozenset(value for key, value in linearisation_rule.items())
        linearisation_rule = \
                [value for key, value in linearisation_rule.items()]

        try:
            grammars.get_grammars()[local_configuration][probability] = \
                    linearisation_rule
        except(KeyError):
            grammars.get_grammars()[local_configuration] = {}
            grammars.get_grammars()[local_configuration][probability] = \
                    linearisation_rule
