from xml.etree import ElementTree

class LinearizationRule:
    @classmethod
    def deserialize(cls, grammars, element_tree):
        probability = float(element_tree.get('p'))
        linearization_rule = {int(element_tree.get('id')): \
                ('root', element_tree.get('form'))}

        for dependent in element_tree:
            linearization_rule[int(dependent.get('id'))] = \
                    (dependent.get('deprel'), dependent.get('form'))

        local_configuration = \
                frozenset(edge for id_, edge in linearization_rule.items())
        linearization_rule = [edge for id_, edge in linearization_rule.items()]

        try:
            grammars.grammars[local_configuration][probability] = \
                    linearization_rule
        except(KeyError):
            grammars.grammars[local_configuration] = {}
            grammars.grammars[local_configuration][probability] = \
                    linearization_rule
