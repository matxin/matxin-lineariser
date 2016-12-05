from linearizationrule import LinearizationRule

from xml.etree import ElementTree

class Grammars:
    def __init__(self):
        self.grammars = {}

    def __init__(self, xml):
        self.grammars = {}
        root = ElementTree.parse(xml).getroot()

        for linearization_rule in root.findall('linearization-rule'):
            LinearizationRule.deserialize(self, linearization_rule)

    def get_grammar(self, local_configuration):
        """Return local_configuration's Grammar.
        
        This roughly corresponds to sorted-rules in the paper.
        """
        raise NotImplementedError

    def serialize(self, linearizer_data):
        """Serialize self to the file linearizer_data."""
        raise NotImplementedError
