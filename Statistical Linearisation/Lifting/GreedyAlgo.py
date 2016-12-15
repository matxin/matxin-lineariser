class GreedyAlgorithm():
    def __init__(self):
        self.tree = None
        self.lifts = dict()

    def execute(self, T):
        self.tree = T
        n_pro_arcs = []


    def DFS1(self, node):
        for child in self.tree.tree[node].field["children"]:
            tmp = self.DFS2(node, child, 1)
            if tmp:
                return

            self.DFS1(child)

    def DFS2(self, ancestor, node, length):
        if not self.is_projective(ancestor, node):
            tmp = self.lifts.get(node, 0)

            if tmp < 3:  # max lifts
                self.lifts[node] = self.lifts.get(node, 0) + length
                self.lift(ancestor, node)

            return True
        if length < 3:
            for child in self.tree.tree[node].fields["children"]:
                tmp = self.DFS2(ancestor, child, length+1)
                if tmp:
                    return True

        return False

    def is_projective(self, ancestor, b):
        #tmp = sorted(self.tree.tree.items())
        begin = min(ancestor, b) - 1
        end = max(ancestor, b) - 1
        for node in range(begin+1, end):
            if not self.is_ancestor(node, ancestor):
                return False
        return True

    def is_ancestor(self, a, b):
        while self.tree.tree[a].fields["head"] != 0:
            if self.tree.tree[a].fields["head"] == b:
                return True

            a = self.tree.tree[a].fields["head"]

        return False

    def lift(self, a, b):
        self.tree.tree[b].fields["head"] = self.tree.tree[a].fields["head"]
