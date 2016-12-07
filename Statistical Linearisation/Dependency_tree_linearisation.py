import copy, random, operator

class Dependecy_tree_linearisation:
    """
    The actual algorithm which perfoms the linearisation on the lifted dependency tree
    """

    def __init__(self, tree):
        self.T = tree  # The dependency tree with lifted nodes
        self.beam_size = None  # maximum beam size
        self.score = [] #list of tuples( the best scores for different combinations of words)

    def DFS(self, node):
        """
        depth-first search - transverses recursively the tree bottom-up and generates all posible word orders,
        keeps the best 1000 for every node. The best linearisations for a given tree are in the head
        :param node: the current node
        :return: None
        """
        for child in self.T.tree[node].fields["children"]:
            self.DFS(child)

        domain = self.T.tree[node].give_domain()

        if len(domain) > 1:
            self.T.tree[node].beam = self.generate_permutations(domain, [], [])

       # print (self.T.tree[node].beam)

        if len(self.T.tree[node].beam) > self.beam_size:
            self.score_beams(node, self.T.tree[node].beam)
            tmp = self.sort_lists_descending_to_score(node)
            self.T.tree[node].beam = [list(i[0]) for i in tmp]
            #print (self.T.tree[node].beam)
            self.T.tree[node].beam = self.T.tree[node].beam[0:self.beam_size]
            #print(self.T.tree[node].beam)

        print (self.T.tree[node].beam)

    def generate_permutations(self, domain, permutation, d_used):
        """
        generates all possible word orders for a given domain in a recursive fashion
        :param domain:
        :param permutation:
        :param d_used:
        :return:
        """
        if len(d_used) == len(domain):
            return [permutation]

        res = []

        for id in domain:
            if id in d_used:
                continue

            used = copy.copy(d_used)
            used.append(id)

            for beam in self.T.tree[id].beam:
                tmp = copy.copy(permutation)
                tmp += beam
                res += self.generate_permutations(domain, tmp, copy.copy(used))

        return res

    def execute_algorithm(self):
        """
        executes the linearisation algorithm (algorithm 3 in the paper)
        :return:
        """
        self.beam_size = 10

        self.DFS(self.T.head)

    def compute_score(self, l):
        """
        computes a score for a list using the training set
        :param l: the list
        :return: score
        """
        return random.random()

    def score_beams(self, node, beams):
        for beam in beams:
            self.T.tree[node].score.append((beam, self.compute_score(beam)))

    def sort_lists_descending_to_score(self, node):
        """
        sorts the score and beam lists
        :param beam: the current beam
        :return: None
        """
        res = sorted(self.T.tree[node].score, key=operator.itemgetter(1), reverse=True)
        return res

    def global_score(self, l):
        """
        computes the score for a list l
        :param l: the list
        :return: the score
        """