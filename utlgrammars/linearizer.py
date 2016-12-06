class Linearizer:
    def __init__(self, linearizer_data):
        self.grammars = Grammars(linearizer_data)

    def linearize_node(self, root, n):
        """Roughly corresponds to linearize-node in the paper."""
        raise NotImplementedError

    def hypothesize_node(self, node, i):
        """Roughly corresponds to hypothesize-node in the paper."""
        raise NotImplementedError

    @classmethod
    def advance_indices(cls, indices):
        """A generator that roughly corresponds to advance-indeces in
        the paper."""
        raise NotImplementedError
