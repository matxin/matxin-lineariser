class Treebank:
    def train(cls, conllu, grammars):
        """Read one sentence at a time from the file conllu and train
        grammars on it."""
        raise NotImplementedError
