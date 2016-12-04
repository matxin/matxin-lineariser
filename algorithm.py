"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""

import operator, random, copy

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
            "5": "feats", #list o-f morphological features
            "6": "head", #head of the current word (val of ID or 0)
            "7": "deprel", #universal dependency relation to the HEAD (root iff HEAD = 0)
            "8": "deps", #enchanced dependency graph (list of head-deprel pairs)
            "9": "misc" #any other annotation
        }

        self.head = None

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
        self.tree[temp.fields["id"]].extract_features()

        if temp.fields["head"] == "0":
            self.head = temp.fields["id"]




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

    def set_neigbouring_nodes(self):
        """
        Calculates the gparents, parents, children and gchildren of every node in a tree
        :return: None
        """
        for node in self.tree:
            self.tree[node].neighbouring_nodes["0"] = [node]

            #print (node, self.tree[node].fields["head"])

            if self.tree[node].fields["head"] != "0" and self.tree[node].fields["head"] != "_":
                self.tree[node].neighbouring_nodes["-1"] = [self.tree[node].fields["head"]] # parent

                if self.tree[self.tree[node].fields["head"]].fields["head"] != "0" and self.tree[self.tree[node].fields["head"]].fields["head"] != "_":
                    self.tree[node].neighbouring_nodes["-2"] = [self.tree[self.tree[node].fields["head"]].fields["head"]] # grandparent

            self.tree[node].neighbouring_nodes["1"] = self.tree[node].fields["children"] #children

            self.tree[node].neighbouring_nodes["2"] = []

            for child in self.tree[node].fields["children"]:
                self.tree[node].neighbouring_nodes["2"] += self.tree[child].fields["children"] #gchildren



    def ufeat(self, node, position, feature):
        """
        returns a feature or a vector of features
        :param node:
        :param position:
        :param feature:
        :return: value of a feature for nodes of given relation
        """
        res = []
        for node_1 in self.tree[node].neighbouring_nodes[position]:
            tmp = self.tree[node_1].features.get(feature, None)
            if tmp != None:
                res.append(tmp)

        return res

    def lemma(self, node, position):
        """
        returns the lemma of a nparent of the node (for position <0) or a nchildren
        :param node: the relative node
        :param position: the relative position to this node
        :return: lemma
        """
        res = []

        for node_1 in self.tree[node].neighbouring_nodes[position]:
            res.append(self.tree[node_1].fields["lemma"])

        return res

    def count(self, node, position):
        """
        count the number of nchildren
        :param node: the relative node
        :param position: the relative position to this node
        :return: the number of nchildren
        """
        return len(self.tree[node].neighbouring_nodes[position])

    def upos(self, node, position):
        """
        returns the part of speech
        :param node: the relative node
        :param position: the relative position
        :return: upostag
        """
        res = []
        for node_1 in self.tree[node].neighbouring_nodes[position]:
            res.append(self.tree[node_1].fields["upostag"])

        return res

    def deprel(self, node, position):
        """
        returns the relation to the HEAD
        :param node: the relative node
        :param position: the relative position to this node
        :return: the deprel tag
        """
        res = []

        for node_1 in self.tree[node].neighbouring_nodes[position]:
            res.append(self.tree[node_1].fields["deprel"])

        return res

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

        self.features = {}

        self.neighbouring_nodes = { # indices of nodes that are +n -> nchildren, -n -> nparents
            "-2": [],
            "-1": [],
            "0": [],
            "1": [],
            "2": []
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

    def extract_features(self):
        """
        extracts all the features from the feats field
        :return: None
        """
        if self.fields["feats"] == "_":
            return

        temp = self.fields["feats"].split('|') #split features

        for feat in temp:
            temp1 = feat.split('=') # splits into a feature and value
            self.features[temp1[0]] = temp1[1]

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

        self.tree.set_neigbouring_nodes()


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


    def __init__(self, tree):
        self.T = tree  # The dependency tree with lifted nodes
        self.beam_size = None  # maximum beam size
        self.score = [] #list of tuples( the best scores for different combinations of words)
        self.beam = []

    def execute_algorithm(self):
        """
        executes the linearisation algorithm (algorithm 3 in the paper)
        :return:
        """
        self.beam_size = 1000

        for node in self.T.tree:
            domain = self.T.tree[node].give_domain()
            agenda = [[]]
            #print ("d", node, domain)

            for w in domain:
                self.beam = []
               # print (agenda)

                for l in agenda:
                    tmp = copy.copy(l)
                   # print ("l", tmp)

                    if w not in l:
                        tmp.append(w)
                        self.beam.append(tmp)
                        self.score.append((tmp, self.compute_score(tmp)))


                if len(self.beam) > self.beam_size:
                    tmp1 = self.sort_lists_descending_to_score() #returns lists of tuples where 2nd is the score
                    iter = 0
                    agenda = []

                    for (a,b) in tmp1:
                        if iter > self.beam_size:
                            break

                        iter += 1
                        agenda.append(a)

                else:
                    agenda = self.beam

                print (node, agenda)

            #### TO FINISH!!! ####

    def compute_score(self, l):
        """
        computes a score for a list using the training set
        :param l: the list
        :return: score
        """
        return random.random()

    def sort_lists_descending_to_score(self):
        """
        sorts the score and beam lists
        :param beam: the current beam
        :return: None
        """
        res = sorted(self.score, key=operator.itemgetter(1), reverse=True)
        return res

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

   # print (tree.head)

    linear = Dependecy_tree_linearisation(tree)

    linear.execute_algorithm()

    #for id in tree.tree:
     #   print (id)
      #  print (tree.tree[id].neighbouring_nodes["-2"], tree.tree[id].neighbouring_nodes["-1"], tree.tree[id].neighbouring_nodes["0"], tree.tree[id].neighbouring_nodes["1"], tree.tree[id].neighbouring_nodes["2"])
        #print (tree.ufeat(id, "-2", "Gender"), tree.ufeat(id, "-1", "Gender"), tree.ufeat(id, "0", "Gender"), tree.ufeat(id, "1", "Gender"), tree.ufeat(id, "2", "Gender"))
       # print (tree.deprel(id, "-2"), tree.deprel(id, "-1"), tree.deprel(id, "0"), tree.deprel(id, "1"), tree.deprel(id, "2"))