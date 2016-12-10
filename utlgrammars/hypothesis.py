from printing import Printing

from grammars import Grammars


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
        # print('Hypothesis.instantiate {{{')
        # print('==========================')
        # print('{{{')
        # print('self is ' + str(self))
        linearisation = []
        daughters = self.get_daughters()[:]
        linearisation_rule = self.get_node().get_sorted_rules()[
            self.get_indices()[0]][1]
        # print('self.get_node().get_local_configuration() is ' + str(
        #     self.get_node().get_local_configuration()))
        # print('linearisation_rule is ' + str(linearisation_rule))
        # print('}}}')
        # print()
        # print('init {{{')
        # print('--------')
        # print('linearisation is ' + Printing.print_list(linearisation))
        # print('daughters is ' + Printing.print_list(daughters))
        # print('}}}')
        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_insert(), linearisation, daughters)
        # print()
        # print('insert {{{')
        # print('----------')
        print('linearisation is ' + Printing.print_list(linearisation))
        # print('daughters is ' + Printing.print_list(daughters))
        linearisation.append(self.get_node())
        # print('}}}')
        # print()
        # print('self {{{')
        # print('--------')
        # print('linearisation is ' + Printing.print_list(linearisation))
        # print('daughters is ' + Printing.print_list(daughters))
        self.instantiate_linearisation_rule_element(
            linearisation_rule.get_append(), linearisation, daughters)
        # print('}}}')
        # print()
        # print('append {{{')
        # print('----------')
        # print('linearisation is ' + Printing.print_list(linearisation))
        # print('daughters is ' + Printing.print_list(daughters))
        # print('}}}')
        # print('}}}')
        return linearisation

    def instantiate_linearisation_rule_element(
            self, linearisation_rule_element, linearisation, daughters):
        print('linearisation_rule_element is ' + Printing.print_list(
            linearisation_rule_element))
        for dependent in linearisation_rule_element:
            linearisation.extend(
                daughters.pop(
                    next(index for index, daughter in enumerate(daughters)
                         if daughter.get_node().get_deprel() == dependent[0]
                         and daughter.get_node().get_word() == dependent[1]))
                .instantiate())

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  node = ' + Printing.shift_str(
                        str(self.get_node())) + '\n' + \
                '  daughters = ' + Printing.shift_str(
                        Printing.print_list(self.get_daughters())) + '\n' + \
                '  indices = ' + Printing.shift_str(
                        Printing.print_list(self.get_indices())) + '\n' + \
                '}'
