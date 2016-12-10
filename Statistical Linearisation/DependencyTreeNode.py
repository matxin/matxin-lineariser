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

        self.score = []

        self.neighbouring_nodes = { # indices of nodes that are +n -> nchildren, -n -> nparents
            "-2": [],
            "-1": [],
            "0": [],
            "1": [],
            "2": []
        }

        self.domain = [] # words that are direct children and the node itself
        self.agenda = None  # word order beam
        self.beam = []  # beam for a node

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