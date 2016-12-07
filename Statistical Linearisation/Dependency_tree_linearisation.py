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
        keeps the best 1000
        :param node: the current node
        :return: None
        """
        for child in self.T.tree[node].fields["children"]:
            self.DFS(child)

        domain = self.T.tree[node].give_domain()

        if len(domain) > 1:
            self.T.tree[node].beam = self.generate_permutations(domain, [], [])

        print (node, self.T.tree[node].beam)

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
        self.beam_size = 1000

        self.DFS(self.T.head)

        """ for node in self.T.tree:
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

            #### TO FINISH!!! ####"""

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