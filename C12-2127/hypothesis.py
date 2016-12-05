class Hypothesis:
    def __init__(self, node, daughters, indices):
        """Roughly corresponds to new-hypothesis in the paper.
        
        Indexes the new hypothesis in by its probability in the node's
        agenda.
        """
        raise NotImplementedError
