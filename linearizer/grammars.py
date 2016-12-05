class Grammars:
    def __init__(self):
        self.grammars = {}

    def __init__(self, linearizer_data):
        """Deserialize self from the file linearizer_data."""
        raise NotImplementedError

    def get_grammar(self, local_configuration):
        """Return local_configuration's Grammar.
        
        This roughly corresponds to sorted-rules in the paper.
        """
        raise NotImplementedError

    def serialize(self, linearizer_data):
        """Serialize self to the file linearizer_data."""
        raise NotImplementedError
