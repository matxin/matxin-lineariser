import DependencyTreeNode, sys

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
        temp = DependencyTreeNode.DependencyTreeNode()

        for no in range(0, 10): # indices of all the fields
            temp.update_field(self.no2field[str(no)], list[no])

        temp.beam = [[temp.fields["id"]]]

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
                self.tree[self.tree[id].fields["head"]].fields["children"].append(id)

    def calculate_domains(self):
        """
        fills out the domain fields for every node
        :return: None
        """

        for id in self.tree:
            self.tree[id].domain = [id]

            for child in self.tree[id].fields["children"]:
                self.tree[id].domain.append(child)

    def set_neigbouring_nodes(self):
        """
        Calculates the gparents, parents, children and gchildren of every node in a tree
        :return: None
        """
        for node in self.tree:
            self.tree[node].neighbouring_nodes["0"] = [node]


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

    def generate_conllu(self):
        """
        generates the tree in the CONLLU format and puts it to the stdout
        :return:
        """

        size = len(self.tree)

        for node in range(1, size+1):
            line = ""
            for field in range(0, 10):
                line += self.tree[str(node)].fields[self.no2field[str(field)]] + "\t"

            sys.stdout.write(line+"\n")

        sys.stdout.write('\n')

    def give_gold_order(self):
        """
        returns the gold linear order for a tree
        :return: the list with all the words
        """
        order = []

        for node in range(len(self.tree)):
            order.append(self.tree[str(node+1)].fields["form"])

        return order
