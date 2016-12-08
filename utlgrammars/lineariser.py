from xml.etree import ElementTree

class Lineariser:
    def __init__(self):
        self.grammars = Grammars()

    def deserialise(self, xml):
        self.get_grammars().deserialise(ElementTree.parse(xml).getroot())

    def get_grammars(self):
        return self.grammars

    def linearise_node(self, root, n):
        """Roughly corresponds to linearise-node in the paper."""
        raise NotImplementedError

    def hypothesise_node(self, node, i):
        """Roughly corresponds to hypothesise-node in the paper."""
        raise NotImplementedError

    @classmethod
    def advance_indices(cls, indices):
        """A generator that roughly corresponds to advance-indeces in
        the paper."""

        for index in range(len(indices)):
            new_indices = indices[:]
            new_indices[index] += 1
            yield new_indices
