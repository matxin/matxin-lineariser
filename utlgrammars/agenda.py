class Agenda:
    def __init__(self):
        self.agenda = {}

    def insert_hypothesis(self, probability, hypothesis):
        """Append hypothesis to the list of index probability.
        
        The agenda is a multimap, since multiple hypotheses may have
        the same probability.
        """

        try:
            self.get_agenda()[probability].append(hypothesis)
        except(KeyError):
            self.get_agenda()[probability] = []
            self.get_agenda()[probability].append(hypothesis)

    def get_agenda(self):
        return self.agenda

    def pop_hypothesis(self):
        """Pop the last hypothesis from the list with the highest
        probability."""

        agenda = list(self.get_agenda.items())
        agenda.sort(reverse=True)
        return agenda[0][1].pop()
