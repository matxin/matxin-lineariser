from grammars import Grammars
from hypothesis import Hypothesis

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
        try:
            return node.get_hypotheses()[i]
        except (IndexError):
            pass

        if i == 0:
            if len(node.get_dependents()) == 0:
                node.rules = {1.0: [(node.get_deprel(), node.get_word())]}
                node.sorted_rules = [1.0]
            else:
                node.rules = self.get_grammars().get_grammar(
                        node.get_local_configuration())
                node.sorted_rules = list(node.get_rules())
                node.sorted_rules.sort(reverse=True)

            indices = [0]
            daughters = []

            for dependent in node.get_dependents():
                daughters.append(self.hypothesise_node(dependent, 0))
                indices.append(0)

            Hypothesis.new_hypothesis(node, daughters, indices)

        try:
            hypothesis = node.get_agenda().pop_hypothesis()
        except (IndexError):
            return

        for indices in self.advance_indices(hypothesis.get_indices()):
            daughters = []

            for index, dependent in enumerate(node.get_dependents()):
                daughter = self.hypothesise_node(dependent, indices[index])

                if daughter is None:
                    daughters = []
                    break

                daughters.append(daughter)

            if len(daughters) != 0:
                Hypothesis.new_hypothesis(node, daughters, indices)

        node.get_hypotheses().append(hypothesis)
        return hypothesis

    @classmethod
    def advance_indices(cls, indices):
        """A generator that roughly corresponds to advance-indeces in
        the paper."""
        for index in range(len(indices)):
            new_indices = indices[:]
            new_indices[index] += 1
            yield new_indices
