"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""


class DependencyTreeNode:
    """
    class which is a node of the dependency tree
    """

    def __init__(self):
        self.domain = None
        self.agenda = None  # word order beam
        self.beam = None  # beam for a node


class Convert2dependencytree:
    """
    reads a dependency tree in CoNLL-U format and converts it to a tree of clasess Dependency_tree
    """
    pass

class Lifting_decoding:
    """
    Does the whole decoding stage of the lifting algorithm using the greedy method
    """
    def sort_by_depth_break_ties_arbitrarily(self, n, t):
        """
        sorts the tree
        :param n: nodes
        :param t: tree
        :return: None
        """

    def extract_features(self, node, T):
        """
        extracts all the features from a node
        :param node: a node
        :param T: the tree
        :return: list of features
        """

    def classify(self, feats):
        """
        classifies features
        :param feats: features
        :return: number of steps to be listed
        """

    def lift(self, node, T, steps):
        """
        lifts a node up
        :param node: the node
        :param T: the tree
        :param steps: no of steps
        :return: None
        """

class Dependecy_tree_linearisation:
    """
    The actual algorithm which perfoms the linearisation on the lifted dependency tree
    """


    def __init__(self):
        self.T = None  # The dependency tree with lifted nodes
        self.beam_size = None  # maximum beam size

    def get_domain(self, h):
        """
        Gets the domain of h and returns it
        :param h: a node
        :return: domain of h
        """

    def append(self, l, w):
        """
        appends a word to a list
        :param l: list to which we append
        :param w: a word that we append
        :return: a new list
        """

    def compute_score(self, l):
        """
        computes a score for a list using the training set
        :param l: the list
        :return: score
        """

    def sort_lists_descending_to_score(self):
        """
        sorts the list, puts the best score up
        :return: the best beam
        """
    def sublist(self, beam_size, beam):
        """
        sublists the longest possible beam to the word order beam
        :param beam_size: size
        :param beam: the current beam
        :return: the longest possible beam
        """

    def global_score(self, l):
        """
        computes the score for a list l
        :param l: the list
        :return: the score
        """
    pass

if __name__ == "main":
    pass