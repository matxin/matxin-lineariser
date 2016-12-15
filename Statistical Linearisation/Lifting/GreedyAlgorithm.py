class GreedyAlgorithm():
    def __init__(self):
        self.tree = None
        self.lifts = dict()

    def execute(self, T):
        self.tree = T
        tmp = True
       # print (self.tree.head)
        while tmp:
            #print (tmp)
            #self.tree.print_tree()
            #print ("lol")
            tmp = False
            tmp1 = self.DFS1(self.tree.head)
            if tmp1:
                tmp = True
        self.tree.print_tree()


    def DFS1(self, node):
        for child in self.tree.tree[node].fields["children"]:
            tmp = False
            if node != self.tree.head:
                tmp = self.DFS2(node, child, 1)

            if tmp:
                #print ("lol")
                return True

            tmp = self.DFS1(child)
            if tmp:
                #print ("lol")
                return True
        #print ("lol")
        return False

    def DFS2(self, ancestor, node, length):
        if not self.is_projective(ancestor, node):
            tmp = self.lifts.get(node, 0)
            #print ("abc")
            if tmp < 3:  # max lifts
                #print (ancestor, node, length)
                self.lifts[node] = self.lifts.get(node, 0) + length
                self.lift(ancestor, node)
                return True
        if length < 3:
            for child in self.tree.tree[node].fields["children"]:
                tmp = False
                tmp = self.DFS2(ancestor, child, length+1)
                if tmp:
                    return True

        return False

    def is_projective(self, ancestor, b):
        #tmp = sorted(self.tree.tree.items())
        begin = min(int(ancestor), int(b))
        end = max(int(ancestor), int(b)) - 1
        #print (begin, end)
        for node in range(begin, end):
            if not self.is_ancestor(str(node), ancestor):
                return False
        return True

    def is_ancestor(self, a, b):
        #print (a, b)
        while self.tree.tree[a].fields["head"] != self.tree.head:
            if self.tree.tree[a].fields["head"] == b:
                return True

            a = self.tree.tree[a].fields["head"]

        return False

    def lift(self, a, b):
        self.tree.tree[self.tree.tree[b].fields["head"]].fields["children"].remove(b)
        self.tree.tree[self.tree.tree[a].fields["head"]].fields["children"].append(b)
        self.tree.tree[b].fields["head"] = self.tree.tree[a].fields["head"]
        print (a,b)
