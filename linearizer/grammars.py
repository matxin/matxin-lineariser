from lrule import LinearisationRule

from xml.etree import ElementTree

class Grammars:
    def __init__(self):
        self.grammars = {}

    def __init__(self, linearisation_rules_etree):
        self.grammars = {}
        for def_rule_etree in linearisation_rules_etree.findall('def-rule'):
            LinearisationRule.deserialise(self, def_rule_etree)

    def get_grammars(self):
        return grammars

    def get_grammar(self, local_configuration):
        """Return local_configuration's Grammar.
        
        This roughly corresponds to sorted-rules in the paper.
        """
        raise NotImplementedError

    def serialize(self, linearizer_data):
        """Serialize self to the file linearizer_data."""
        raise NotImplementedError
