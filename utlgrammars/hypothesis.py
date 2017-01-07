from grammars import Grammars
from printing import Printing
from word import word_eq

import random

random.seed(0)


class Coverage:
    def __init__(self):
        self.numerator = 0
        self.denominator = 0

    def covered(self):
        self.numerator += 1
        self.denominator += 1

    def not_covered(self):
        self.denominator += 1

    def get_coverage(self):
        return float(self.numerator) / float(self.denominator)


coverage = Coverage()


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

    def instantiate(self, shuffle=False):
        """Roughly corresponds to instantiate-hypothesis in the
        paper.

        hypothesize_node should set WordLine.rules to an appropriate
        Grammar, so this method does not need to know about Grammars.
        """
        global coverage
        linearisation = []
        daughters = self.get_daughters()[:]
        linearisation_rule = self.get_node().get_sorted_rules()[
            self.get_indices()[0]][1]

        print('self = ' + str(self))
        print('linearisation_rule = ' + str(linearisation_rule))

        if linearisation_rule is None:
            coverage.not_covered()

            if shuffle:
                daughters.append(self.get_node())
                random.shuffle(daughters)
                daughters_iter_ = iter(daughters)

                while True:
                    x = next(daughters_iter_)

                    if x is self.get_node():
                        linearisation.append(x)
                        break

                    linearisation.extend(x.instantiate(shuffle))

                while True:
                    try:
                        daughter = next(daughters_iter_)
                    except (StopIteration):
                        return linearisation

                    linearisation.extend(daughter.instantiate(shuffle))

            daughters.sort(key=Hypothesis.get_id)
            daughters_iter_ = iter(daughters)

            while True:
                try:
                    daughter = next(daughters_iter_)
                except (StopIteration):
                    linearisation.append(self.get_node())
                    return linearisation

                if daughter.get_id() > self.get_id():
                    linearisation.append(self.get_node())
                    linearisation.extend(daughter.instantiate(shuffle))
                    break

                linearisation.extend(daughter.instantiate(shuffle))

            while True:
                try:
                    daughter = next(daughters_iter_)
                except (StopIteration):
                    return linearisation

                linearisation.extend(daughter.instantiate(shuffle))

        if len(daughters) != 0:
            coverage.covered()

        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_insert(), linearisation, daughters, shuffle)
        linearisation.append(self.get_node())
        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_append(), linearisation, daughters, shuffle)
        return linearisation

    def get_id(self):
        return self.get_node().get_id()

    def instantiate_linearisation_rule_element(self,
                                               linearisation_rule_element,
                                               linearisation,
                                               daughters,
                                               shuffle=False):
        print('linearisation_rule_element = ' + Printing.print_list(
            linearisation_rule_element))
        print('daughters = ' + Printing.print_list(daughters))
        for dependent in linearisation_rule_element:
            linearisation.extend(
                daughters.pop(
                    next(index for index, daughter in enumerate(daughters)
                         if daughter.get_node().get_deprel() == dependent[0]
                         and word_eq(daughter.get_node().get_word(), dependent[
                             1]))).instantiate(shuffle))

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  node = ' + Printing.shift_str(str(self.get_node())) + '\n' + \
                '  daughters = ' + Printing.shift_str(Printing.print_list(self.get_daughters())) + '\n' + \
                '  indices = ' + Printing.shift_str(Printing.print_list(self.get_indices())) + '\n' + \
                '}'
