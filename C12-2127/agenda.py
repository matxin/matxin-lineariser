class Agenda:
    def __init__(self):
        self.agenda = {}

    def insert_hypothesis(self, probability, hypothesis):
        """Append hypothesis to the list of index probability.
        
        The agenda is a multimap, since multiple hypotheses may have
        the same probability.
        """
        raise NotImplementedError

    def pop_hypothesis(self):
        """Pop the last hypothesis from the list with the highest
        probability."""
        raise NotImplementedError
