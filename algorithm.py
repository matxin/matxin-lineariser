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
        self.tree = {} # dictionary of nodes -> id

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

        self.tree[temp.fields["id"]] = temp

    def print_tree(self):
        """
        prints the val of fields for every node
        :return: None
        """
        for id in self.tree:
            print (self.tree[id].fields, "\n")

    def add_children(self):
        """
        fills out the children field for every node
        :return: None
        """
        for id in self.tree:
            if self.tree[id].fields["head"] != "0" and self.tree[id].fields["head"] != "_":
                #print (int(self.tree[id].fields["head"])-1, self.tree[self.tree[id].fields["head"]].fields["id"])
                self.tree[self.tree[id].fields["head"]].fields["children"].append(id)

       # for node in self.tree:
        #    print (node.fields["id"], node.fields["children"])

    def calculate_domains(self):
        """
        fills out the domain fields for every node
        :return: None
        """

        for id in self.tree:
            self.tree[id].domain = [self.tree[id].fields["form"]]

            #print (node.domain)

            for child in self.tree[id].fields["children"]:
                #print (int(child))
                self.tree[id].domain.append(self.tree[child].fields["form"])
            #print(node.domain)

    def ufeat(self, node, position, feature):
        """
        returns a feature or a vector of features
        :param node:
        :param position:
        :param feature:
        :return: value of a feature for nodes of given relation
        """
        pass

    def lemma(self, node, position):
        """
        returns the lemma of a nparent of the node (for position <0) or a nchildren
        :param node: the relative node
        :param position: the relative position to this node
        :return: lemma
        """
        return self.ufeat(node, position, 'lemma')

    def count(self, node, position):
        """
        count the number of nchildren
        :param node: the relative node
        :param position: the relative position to this node
        :return: the number of nchildren
        """
        return self.ufeat(node, position, 'count')

    def upos(self, node, position):
        """
        returns the part of speech
        :param node: the relative node
        :param position: the relative position
        :return: upostag
        """
        return self.ufeat(node, position, 'upostag')

    def deprel(self, node, position):
        """
        returns the relation to the HEAD
        :param node: the relative node
        :param position: the relative position to this node
        :return: the deprel tag
        """
        return self.ufeat(node, position, 'deprel')

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

        self.neighbouring_nodes = { # indices of nodes that are +n -> nchildren, -n -> nparents
            "-2": None,
            "-1": None,
            "0": None,
            "1": None,
            "2": None
        }

        self.domain = [] # words that are direct children and the node itself
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

    def give_domain(self):
        """
        returns a domain for a given node
        :return: self.domain
        """
        return self.domain

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

        self.tree.calculate_domains()


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
   # tree.print_tree()


    for id in tree.tree:
        print (id)
        print (tree.tree[id].give_domain())