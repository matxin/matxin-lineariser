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
            self.get_agenda()[probability] = [hypothesis]

    def get_agenda(self):
        return self.agenda

    def pop_hypothesis(self):
        """Pop the last hypothesis from the list with the highest
        probability."""

        agenda = list(self.get_agenda.items())
        agenda.sort()
        hypothesis = agenda[-1][1].pop()

        if len(agenda.[-1][1] == 0):
            del self.get_agenda()[agenda[-1][0]]

        return hypothesis
