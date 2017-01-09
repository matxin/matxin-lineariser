from .lineariser import Lineariser
from .printing import Printing
from .wordline import WordLine

import re

from ..statistical_linearisation.DependencyTree import DependencyTree
from ..statistical_linearisation.GreedyLifting import GreedyLifting

CONLLU_COMMENT = re.compile('\s*#')


class Sentence:
    def __init__(self):
        pass

    @classmethod
    def deserialise(cls, conllu):
        bad = False
        wordlines = {}

        for line in conllu:
            line = line[:-1]  # strip the trailing newline

            if CONLLU_COMMENT.match(line) is not None:
                continue

            if line == '':
                if bad:
                    bad = False
                    wordlines = {}
                    continue

                sentence = Sentence()

                for wordline in wordlines.values():
                    wordline.add_edge(sentence, wordlines)

                yield sentence
                wordlines = {}
                continue

            wordline = WordLine()

            try:
                wordline.deserialise(line)
            except:
                bad = True
                continue

            wordlines[wordline.get_id()] = wordline

    @classmethod
    def deserialise_matxin(cls, corpus_etree):
        for sentence_etree in corpus_etree.findall('SENTENCE'):
            sentence = Sentence()
            root_node_etree = sentence_etree.find('NODE')
            root = WordLine()
            maximum_ref = root.deserialise_matxin(root_node_etree)
            sentence.root = root
            sentence.get_root().add_ref(maximum_ref)
            yield sentence

    def get_wordlines(self):
        wordlines = {}
        self.get_root().add_word(wordlines)
        return wordlines

    def linearise(self, lineariser, n=0, shuffle=False):
        self.linearisations = lineariser.linearise_node(self.get_root(), n,
                                                        shuffle)

    def get_root(self):
        return self.root

    def get_linearisations(self):
        return self.linearisations

    def get_strings(self):
        return [
            ' '.join([word.get_form().lower() for word in linearisation])
            for linearisation in self.get_linearisations()
        ]

    def __str__(self):
        return Printing.get_module_qualname(self) + ' = {\n' + \
                '  wordlines = ' + Printing.shift_str(Printing.print_dict(self.get_wordlines())) + '\n' + \
                '  root = ' + Printing.shift_str(str(self.get_root())) + '\n' + \
                '}'

    def get_dependency_tree(self):
        dependency_tree = DependencyTree()

        for wordline in self.get_wordlines().values():
            for dependent in wordline.get_dependents():
                dependent.head = wordline.get_id()

        for wordline in self.get_wordlines().values():
            try:
                form = wordline.get_form()
            except (AttributeError):
                form = '_'

            feats = '|'.join([
                '='.join(item)
                for item in dict(wordline.get_word().get_feats()).items()
            ])

            if feats == '':
                feats = '_'

            dependency_tree.add_node([
                str(wordline.get_id()), form, wordline.get_word().get_lemma(),
                wordline.get_word().get_upostag(), '_', feats,
                str(wordline.get_head()), wordline.get_deprel(), '_', '_'
            ])

        dependency_tree.add_children()
        dependency_tree.calculate_domains()
        dependency_tree.set_neigbouring_nodes()
        return dependency_tree

    def deserialise_dependency_tree(self, dependency_tree):
        wordlines = {}

        for id_, dependency_tree_node in dependency_tree.tree.items():
            wordline = WordLine()
            wordline.deserialise_dependency_tree_node(dependency_tree_node)
            wordlines[int(id_)] = wordline

        for wordline in wordlines.values():
            wordline.add_edge(self, wordlines)

    def projectivise(self):
        dependency_tree = self.get_dependency_tree()
        greedy_lifting = GreedyLifting()
        dependency_tree = greedy_lifting.execute(dependency_tree)
        self.deserialise_dependency_tree(dependency_tree)
