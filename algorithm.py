"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""


class DependencyTree:
    """
    a class that holds the whole dependency tree
    """

    def __init__(self):
        """
        initialises the whole dependency tree and creates the conversion table for fields from the input
        """
        self.tree = [] # list of nodes

        self.no2field = {
            "0": "id", #id
            "1": "form", #form
            "2": "lemma", #lemma
            "3": "upostag", #universal part-of-speech tag
            "4": "xpostag", #language specific part-of-speech tag
            "5": "feats", #list of morphological features
            "6": "head", #head of the current word (val of ID or 0)
            "7": "deprel", #universal dependency relation to the HEAD (root iff HEAD = 0)
            "8": "deps", #enchanced dependency graph (list of head-deprel pairs)
            "9": "misc" #any other annotation
        }

    def add_node(self, list):
        """
        adds a node of type DependencyTreeNode to the class
        :param list: val of fields got from input
        :return: None
        """
        temp = DependencyTreeNode()

        for no in range(0, 9):
            temp.update_field(self.no2field[str(no)], list[no])

        self.tree.append(temp)

    def print_tree(self):
        """
        prints the val of fields for every node
        :return: None
        """
        for node in self.tree:
            print (node.fields, "\n")

    def add_children(self):
        for node in self.tree:
            if node.fields["head"] != "0" and node.fields["head"] != "_":
                self.tree[int(node.fields["head"])-1].fields["children"].append(node.fields["id"])

class DependencyTreeNode:
    """
    class which is a node of the dependency tree
    """

    def __init__(self):
        """
        initialises the variables and fields of the node
        """

        self.fields = {
            "id": None, #id
            "form": None, #form
            "lemma": None, #lemma
            "upostag": None, #universal part-of-speech tag
            "xpostag": None, #language specific part-of-speech tag
            "feats": None, #list of morphological features
            "head": None, #head of the current word (val of ID or 0)
            "deprel": None, #universal dependency relation to the HEAD (root iff HEAD = 0)
            "deps": None, #enchanced dependency graph (list of head-deprel pairs)
            "misc": None, #any other annotation
            "children": [] #points to the children
        }

        self.domain = None
        self.agenda = None  # word order beam
        self.beam = None  # beam for a node

    def update_field(self, id, val):
        """
        changes the value of a certain field
        :param id: id of a field
        :param val: value to which it's changed
        :return: None
        """
        self.fields[id] = val


class Convert2dependencytree:
    """
    reads a dependency tree in CoNLL-U format and converts it to a tree of clasess Dependency_tree
    """

    def __init__(self):
        """
        creates a dependency tree and reads it from a file
        """
        self.tree = DependencyTree()

        self.read_open_file()

        self.tree.add_children()


    def read_open_file(self):
        """
        reads a file in conllu, splits fields and invokes adding a node
        :return:
        """
        fhand = open("test1.conllu")

        for line in fhand:

            if line[0] == '#': # if that's a comment
                continue

            if len(line) < 2: # if that's a blank line
                continue

            words = line.split('\t')

            self.tree.add_node(words)


    def ref_tree(self):
        """
        used to copy a dependency tree
        :return: the tree
        """

        return self.tree

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


if __name__ == "__main__":
    test = Convert2dependencytree()
    tree = test.ref_tree()
    tree.print_tree()