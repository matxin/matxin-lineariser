from . import GreedyLinearisation

import copy
import operator
import random

class GreedyDomains(GreedyLinearisation.GreedyLinearisation):
    """
    Linearisation method, it needs to have a projectivised tree as input and by scoring linearisations in the beam it
    finds the best one
    """
    def linearise(self, tree):
        """
        main method of this class
        :param tree: the tree
        :return: (list) of linearised words
        """
        self.beam_size = 5 # maximum beam size, to improve!!!
        self.cutoff_limit = 100 # when it's exceeded, no scoring is performed and first linearisations are taken
        self.T = tree

        self.DFS(self.T.head)
        tmp = [self.T.tree[self.T.tree[self.T.head].beam[0][i]].fields["form"]
               for i in range(0, len(self.T.tree[self.T.head].beam[0]))] # gets the lemmas of the best linearisation

        return tmp

    def DFS(self, node):
        """
        depth-first search - transverses recursively the tree bottom-up and generates all posible word orders,
        keeps the best  for every node. The best linearisations for a given tree are in the head
        :param node: the current node
        :return: None
        """
        #print (node)

        for child in self.T.tree[node].fields["children"]:
            self.DFS(child)

        domain = self.T.tree[node].give_domain()

        if len(domain) > 1: # for a unigram domains there's only one permutation
            self.T.tree[node].beam = self.generate_permutations(domain, [], [])

        if len(self.T.tree[node].beam) > self.cutoff_limit: # due to performance reasons (taken too long to score)
            self.T.tree[node].beam = self.T.tree[node].beam[0:self.beam_size] #just randomly take first linearistions
            return

        if len(self.T.tree[node].beam) > self.beam_size:
            self.score_beams(node, self.T.tree[node].beam)
            tmp = self.sort_lists_descending_to_score(node)
            self.T.tree[node].beam = [list(i[0]) for i in tmp] # rearrange the beam according to scores
            self.T.tree[node].beam = self.T.tree[node].beam[0:self.beam_size] # and take the best ones

    def generate_permutations(self, domain, permutation, d_used):
        """
        generates all possible word orders for a given domain in a recursive fashion
        :param domain: the domain
        :param permutation: the current permutation
        :param d_used: list of domains already used
        :return: (list) of permutations
        """
        if len(d_used) == len(domain): # when we used every word in the domain
            return [permutation]

        res = []

        for id in domain:
            if id in d_used: # if it has already been used
                continue

            used = copy.copy(d_used)
            used.append(id)

            for beam in self.T.tree[id].beam: # append all the linearistions from previous nodes
                tmp = copy.copy(permutation)
                tmp += beam
                res += self.generate_permutations(domain, tmp, copy.copy(used))
                if len(res) > self.cutoff_limit: # same reason as above
                    return res

        return res

    def compute_score(self, l):
        """
        computes the probability by averaging the probalities of all possible trigrams
        :param l: the list
        :return: score (0, 1)
        """
        if len(l) == 1: # only one possible linearisation
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
        """
        gets score for all the beams
        :param node: the node
        :param beams: the list of beams
        :return: None
        """
        for beam in beams:
            self.T.tree[node].score.append((beam, self.compute_score(beam)))

    def sort_lists_descending_to_score(self, node):
        """
        sorts the score and beam lists
        :param beam: the current beam
        :return: the sorted list
        """
        res = sorted(self.T.tree[node].score, key=operator.itemgetter(1), reverse=True)

        return res

    def get_prob2(self, pos1, pos2):
        """
        gets the probability for a given pair of POS tags
        :param pos1:
        :param pos2:
        :return: the probability
        """
        sum = self.probabilities.get(pos1+','+pos2, 0)
        sum += self.probabilities.get(pos2+','+pos1, 0)

        if sum == 0:
            return 0.0

        return float(self.probabilities.get(pos1+','+pos2, 0)) / float(sum)

    def get_prob3(self, pos1, pos2, pos3):
        """
        gets the probability for a given triple of POS tags
        :param pos1:
        :param pos2:
        :param pos3:
        :return: the probability
        """
        sum = self.probabilities.get(pos1+','+pos2+','+pos3, 0)
        sum += self.probabilities.get(pos1+','+pos3+','+pos2, 0)
        sum += self.probabilities.get(pos2 + ',' + pos1 + ',' + pos3, 0)
        sum += self.probabilities.get(pos2+','+pos3+','+pos1, 0)
        sum += self.probabilities.get(pos3+','+pos2+','+pos1, 0)
        sum += self.probabilities.get(pos3 + ',' + pos1 + ',' + pos2, 0)

        if sum == 0:
            return 0.0

        return float(self.probabilities.get(pos1+','+pos2+','+pos3, 0)) / float(sum)
