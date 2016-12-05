class Grammar:
    def __init__(self):
        self.rules = {}

    def increment_rule(self, rule):
        """Increment rule's frequency or initialize it to one if it
        does not exit."""
        raise NotImplementedError

    def get_probability(self, rule):
        """Return rule's probability."""
        raise NotImplementedError
