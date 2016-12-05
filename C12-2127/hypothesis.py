class Hypothesis:
    def __init__(self, node, daughters, indices):
        """Roughly corresponds to new-hypothesis in the paper.
        
        Indexes the new hypothesis in by its probability in the node's
        agenda.
        """
        raise NotImplementedError

    def score(self):
        """Roughly corresponds to score-hypothesis in the paper."""
        raise NotImplementedError

    def instantiate(self):
        """Roughly corresponds to instantiate-hypothesis in the
        paper.
        
        hypothesize_node should set WordLine.rules to an appropriate
        Grammar, so this method does not need to know about Grammars.
        """
