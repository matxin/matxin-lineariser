class Grammars:
    def __init__(self):
        self.grammars = {}

    def get_grammar(self, local_configuration):
        """Return local_configuration's Grammar.
        
        This roughly corresponds to sorted-rules in the paper.
        """
        raise NotImplementedError
