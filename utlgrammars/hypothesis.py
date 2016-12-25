from grammars import Grammars
from printing import Printing


numerator = 0
denominator = 0

class Hypothesis:
    @classmethod
    def new_hypothesis(cls, node, daughters, indices):
        """Roughly corresponds to new-hypothesis in the paper.

        Indexes the new hypothesis by its probability in the node's
        agenda.
        """
        hypothesis = Hypothesis(node, daughters, indices)

        try:
            node.get_agenda().insert_hypothesis(hypothesis.score(), hypothesis)
        except (IndexError):
            pass

    def __init__(self, node, daughters, indices):
        self.node = node
        self.daughters = daughters
        self.indices = indices

    def score(self):
        """Roughly corresponds to score-hypothesis in the paper."""
        score = self.get_node().get_sorted_rules()[self.get_indices()[0]][0]

        for daughter in self.get_daughters():
            score *= daughter.score()

        return score

    def get_node(self):
        return self.node

    def get_indices(self):
        return self.indices

    def get_daughters(self):
        return self.daughters

    def instantiate(self):
        """Roughly corresponds to instantiate-hypothesis in the
        paper.

        hypothesize_node should set WordLine.rules to an appropriate
        Grammar, so this method does not need to know about Grammars.
        """
        global numerator, denominator
        denominator += 1
        linearisation = []
        daughters = self.get_daughters()[:]
        linearisation_rule = self.get_node().get_sorted_rules()[
            self.get_indices()[0]][1]

        if linearisation_rule is None:
            daughters.sort(key=Hypothesis.get_id)
            list_iter_ = iter(daughters)

            while True:
                try:
                    daughter = next(list_iter_)
                except (StopIteration):
                    linearisation.append(self.get_node())
                    return linearisation

                if daughter.get_id() > self.get_id():
                    linearisation.append(self.get_node())
                    linearisation.extend(daughter.instantiate())
                    break

                linearisation.extend(daughter.instantiate())

            while True:
                try:
                    daughter = next(list_iter_)
                except (StopIteration):
                    return linearisation

                linearisation.extend(daughter.instantiate())

        numerator += 1
        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_insert(), linearisation, daughters)
        linearisation.append(self.get_node())
        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_append(), linearisation, daughters)
        return linearisation

    def get_id(self):
        return self.get_node().get_id()

    def instantiate_linearisation_rule_element(
            self, linearisation_rule_element, linearisation, daughters):
        for dependent in linearisation_rule_element:
            linearisation.extend(
                daughters.pop(
                    next(index for index, daughter in enumerate(daughters)
                         if daughter.get_node().get_deprel() == dependent[0]
                         and daughter.get_node().get_word() == dependent[1]))
                .instantiate())

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  node = ' + Printing.shift_str(str(self.get_node())) + '\n' + \
                '  daughters = ' + Printing.shift_str(Printing.print_list(self.get_daughters())) + '\n' + \
                '  indices = ' + Printing.shift_str(Printing.print_list(self.get_indices())) + '\n' + \
                '}'
