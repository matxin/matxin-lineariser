import GreedyLinearisation, copy, operator, random

class GreedyDomains(GreedyLinearisation.GreedyLinearisation):
    def linearise(self, tree):
        self.beam_size = 5
        self.T = tree
        self.DFS(self.T.head)
        tmp = [self.T.tree[self.T.tree[self.T.head].beam[0][i]].fields["form"] for i in range(0, len(self.T.tree[self.T.head].beam[0]))]
        return tmp

    def DFS(self, node):
        """
        depth-first search - transverses recursively the tree bottom-up and generates all posible word orders,
        keeps the best 1000 for every node. The best linearisations for a given tree are in the head
        :param node: the current node
        :return: None
        """
        #print (node)

        for child in self.T.tree[node].fields["children"]:
            self.DFS(child)

        domain = self.T.tree[node].give_domain()

        #print (self.T.tree[node].beam)
        """print ("node: "+ node)
        for node in domain:
            print(self.T.tree[node].beam)"""

        if len(domain) > 1:
            self.T.tree[node].beam = self.generate_permutations(domain, [], [])

        #print (domain)
       # pom = [len(i) for i in self.T.tree[node].beam]
        #print (pom)
        #print (node)

        #print (self.T.tree[node].beam)
        #print (node, len(self.T.tree[node].beam))

        if len(self.T.tree[node].beam) > 100:
            #print (node)
            self.T.tree[node].beam = self.T.tree[node].beam[0:self.beam_size]
            return

        if len(self.T.tree[node].beam) > self.beam_size:
            #print ("LOL")
            self.score_beams(node, self.T.tree[node].beam)
            #print (node)
            tmp = self.sort_lists_descending_to_score(node)
            #pom = self.T.tree[node].beam
            self.T.tree[node].beam = [list(i[0]) for i in tmp]
            #print (pom == self.T.tree[node].beam)
            #print (self.T.tree[node].score)
            self.T.tree[node].beam = self.T.tree[node].beam[0:self.beam_size]
            #print(self.T.tree[node].beam)

        #print (len(self.T.tree[node].beam))


    def generate_permutations(self, domain, permutation, d_used):
        """
        generates all possible word orders for a given domain in a recursive fashion
        :param domain:
        :param permutation:
        :param d_used:
        :return:
        """
        #print (permutation)
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
                if len(res) > 100:
                    return res

        return res

    def compute_score(self, l):
        """
        computes a score for a list using the training set
        :param l: the list
        :return: score
        """
        if len(l) == 1:
            return 1
        elif len(l) == 2:
            return self.get_prob2(self.T.tree[l[0]].fields['upostag'], self.T.tree[l[1]].fields['upostag'])
        else:
            score = 0.0
            id = 0
            for i in range(0, len(l)):
                for j in range(i+1, len(l)):
                    for k in range(j+1, len(l)):
                        score += self.get_prob3(self.T.tree[l[i]].fields['upostag'], self.T.tree[l[j]].fields['upostag'],
                                                self.T.tree[l[k]].fields['upostag'])
                        id += 1

            return (score/float(id))

    def score_beams(self, node, beams):
        #print (len(beams))
        for beam in beams:
            self.T.tree[node].score.append((beam, self.compute_score(beam)))
            #print (self.compute_score(beam))

    def sort_lists_descending_to_score(self, node):
        """
        sorts the score and beam lists
        :param beam: the current beam
        :return: None
        """
        res = sorted(self.T.tree[node].score, key=operator.itemgetter(1), reverse=True)
        return res

    def get_prob2(self, pos1, pos2):
        sum = self.probabilities.get(pos1+','+pos2, 0)
        sum += self.probabilities.get(pos2+','+pos1, 0)
        return float(self.probabilities.get(pos1+','+pos2, 0)) / float(sum)

    def get_prob3(self, pos1, pos2, pos3):
        sum = self.probabilities.get(pos1+','+pos2+','+pos3, 0)
        sum += self.probabilities.get(pos1+','+pos3+','+pos2, 0)
        sum += self.probabilities.get(pos2 + ',' + pos1 + ',' + pos3, 0)
        sum += self.probabilities.get(pos2+','+pos3+','+pos1, 0)
        sum += self.probabilities.get(pos3+','+pos2+','+pos1, 0)
        sum += self.probabilities.get(pos3 + ',' + pos1 + ',' + pos2, 0)
        return float(self.probabilities.get(pos1+','+pos2+','+pos3, 0)) / float(sum)
