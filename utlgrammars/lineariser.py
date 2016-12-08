class Lineariser:
    def __init__(self, lineariser_data):
        self.grammars = Grammars(lineariser_data)

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
