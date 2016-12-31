import copy

class GreedyLifting:
    """
    a class performing the greedy lifting algorithm as described here:
    """

    def __init__(self):
        self.tree = None
        self.lifts = dict()
        self.max_lifts = 1000
        self.max_length = 3

    def execute(self, T):
        """
        main method, executes the whole algorithm
        :param T: the tree
        :return: the lifted tree
        """
        self.tree = copy.deepcopy(T)
        tmp = True

        while tmp: # while last time the algorithm lifted something
            tmp = self.DFS1(self.tree.head)

        return self.tree


    def DFS1(self, node):
        """
        find the first node of a pair that will be to be lifted
        :param node: the current node
        :return: (Boolean) whether the algorithm lifted something or not
        """

        for child in self.tree.tree[node].fields["children"]:
            if node != self.tree.head:
                tmp = self.DFS2(node, child, 1)

                if tmp:
                    return True

            tmp = self.DFS1(child)

            if tmp:
                return True

        return False

    def DFS2(self, ancestor, node, length):
        """
        looks for the second node of a pair to be lifted (the lower one), first taking the smallest paths
        :param ancestor: the first node of a pair
        :param node: the current node
        :param length: the length of a path from one node to another
        :return: (Boolean) whether the algorithm lifted something or not
        """

        if not self.is_projective(ancestor, node):
            tmp = self.lifts.get(node, 0) # how many times a node has already been lifted

            if tmp < self.max_lifts:  # max lifts per node
                self.lifts[node] = self.lifts.get(node, 0) + length # add the number of lifts done this time
                self.lift(ancestor, node)
                return True

        if length < self.max_length: # the max length of a path

            for child in self.tree.tree[node].fields["children"]: # continue the search for a non-projective edge
                tmp = self.DFS2(ancestor, child, length+1)
                if tmp:
                    return True

        return False

    def is_projective(self, ancestor, b):
        """
        checks whether the edge is projective
        :param ancestor: a node
        :param b: a node
        :return: (Boolean)
        """
        begin = min(int(ancestor), int(b))
        end = max(int(ancestor), int(b)) - 1

        for node in range(begin, end):
            if not self.is_ancestor(str(node), ancestor): # if confused, look at the def of projectivity
                return False

        return True

    def is_ancestor(self, a, b):
        """
        checkhs whether a is an ancestor of b
        :param a:
        :param b:
        :return: (Boolean
        """
        if self.tree.tree[a].fields["head"] == '0':
            return False

        while self.tree.tree[a].fields["head"] != self.tree.head :
            if self.tree.tree[a].fields["head"] == b:
                return True

            a = self.tree.tree[a].fields["head"]

        return False

    def lift(self, a, b):
        """
        lifts the a->b edge
        :param a: the higher node
        :param b: the lower node
        :return: None
        """
        self.tree.tree[self.tree.tree[b].fields["head"]].fields["children"].remove(b)
        self.tree.tree[self.tree.tree[a].fields["head"]].fields["children"].append(b)
        self.tree.tree[b].fields["head"] = self.tree.tree[a].fields["head"]