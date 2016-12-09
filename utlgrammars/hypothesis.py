class Hypothesis:
    @classmethod
    def new_hypothesis(cls, node, daughters, indices):
        """Roughly corresponds to new-hypothesis in the paper.

        Indexes the new hypothesis by its probability in the node's
        agenda.
        """
        hypothesis = Hypothesis(node, daughters, indices)
        node.get_agenda().insert_hypothesis(hypothesis.score(), hypothesis)

    def __init__(self, node, daughters, indices):
        self.node = node
        self.daughters = daughters
        self.indices = indices

    def score(self):
        """Roughly corresponds to score-hypothesis in the paper."""
        score = self.get_node().get_sorted_rules()[self.get_indices()[0]]

        for daughter in self.get_daughters():
            score *= daughter.score()

        return score

    def get_node(self):
        return self.node

    def get_indices(self):
        return self.indices

    def get_daughters(self):
        return self.daughters

    def instantiate(self):
        """Roughly corresponds to instantiate-hypothesis in the
        paper.

        hypothesize_node should set WordLine.rules to an appropriate
        Grammar, so this method does not need to know about Grammars.
        """

    def __repr__(self):
        return self.__module__ + '.' + self.__class__.__name__ + '(' + repr(
            self.get_node()) + ', ' + repr(self.get_daughters()) + ', ' + repr(
                self.get_indices()) + ')'
