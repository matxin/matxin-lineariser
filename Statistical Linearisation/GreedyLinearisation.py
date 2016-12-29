import DependencyTree, sys

class GreedyLinearisation:
    def __init__(self):
        self.probabilities = {} # dict with POSs and indices

    def add_case(self, pos, pos1, pos2):
        self.probabilities[pos+","+pos1+","+pos2] = self.probabilities.get(pos+","+pos1+","+pos2, 0) + 1
        self.probabilities[pos+','+pos1] = self.probabilities.get(pos+','+pos1, 0) + 1
        self.probabilities[pos1 + ',' + pos2] = self.probabilities.get(pos1 + ',' + pos2, 0) + 1

    def linearise(self, tree):

        use_now = []
        tmp = 0
        order = []
        tmp1 = []

        for node in tree.tree:
            tmp += 1
            if tmp < 3:
                use_now.append(tree.tree[node])
                continue

            if tmp == 3:
                use_now.append(tree.tree[node])
                tmp1 = self.get_prob(use_now[0].fields["upostag"], use_now[1].fields["upostag"], use_now[2].fields["upostag"])

                for id in tmp1:
                    order.append(use_now[id-1].fields["id"])

            if tmp > 3:
                flag = False
                for id in range(len(order)-1):
                    tmp1 = self.get_prob(tree.tree[order[id]].fields["upostag"], tree.tree[order[id+1]].fields["upostag"],
                                         tree.tree[node].fields["upostag"])

                    if tmp1 == [1, 2, 3]:
                        #print (order, id)
                        tmp2 = [node]
                        order = order[:id+2] + tmp2 + order[id+2:]
                        flag = True
                        break

                if not flag:
                    order.append(node)

        linearised = []
        if order == []:
            if len(tree.tree) == 1:
                return [tree.tree['1'].fields["form"]]

            else:
                if self.get_prob2(tree.tree['1'].fields['upostag'], tree.tree['2'].fields['upostag']) == [1, 2]:
                    return [tree.tree['1'].fields["form"], tree.tree['2'].fields["form"]]
                else:
                    return [tree.tree['2'].fields["form"], tree.tree['1'].fields["form"]]

        for word in order:
            linearised.append(tree.tree[word].fields["form"])

        return linearised

    def get_prob2(self, pos, pos1):
        prob_max = 0
        id = None

        if self.probabilities.get(pos+','+pos1, 0) > prob_max:
            prob_max = self.probabilities.get(pos+','+pos1, 0)
            id = [1, 2]

        if self.probabilities.get(pos1+','+pos, 0) > prob_max:
            prob_max = self.probabilities.get(pos1+','+pos, 0)
            id = [2, 1]

        return id


    def get_prob(self, pos, pos1, pos2):
        prob_max = 0
        id = [1, 2, 3]
        #print (self.probabilities)

        if self.probabilities.get(pos + "," + pos1 + "," + pos2, 0) > prob_max:
            prob_max = self.probabilities.get(pos + "," + pos1 + "," + pos2, 0)
            id = [1, 2, 3]

        if self.probabilities.get(pos + "," + pos2 + "," + pos1, 0) > prob_max:
            prob_max = self.probabilities.get(pos + "," + pos2 + "," + pos1, 0)
            id = [1, 3, 2]

        if self.probabilities.get(pos1 + "," + pos + "," + pos2, 0) > prob_max:
            prob_max = self.probabilities.get(pos1 + "," + pos + "," + pos2, 0)
            id = [2, 1, 3]

        if self.probabilities.get(pos1 + "," + pos2 + "," + pos, 0) > prob_max:
            prob_max = self.probabilities.get(pos1 + "," + pos2 + "," + pos, 0)
            id = [2, 3, 1]

        if self.probabilities.get(pos2 + "," + pos1 + "," + pos, 0) > prob_max:
            prob_max = self.probabilities.get(pos2 + "," + pos1 + "," + pos, 0)
            id = [3, 2, 1]

        if self.probabilities.get(pos2 + "," + pos + "," + pos1, 0) > prob_max:
            prob_max = self.probabilities.get(pos2 + "," + pos + "," + pos1, 0)
            id = [3, 1, 2]

        return id

    def save_dict2file(self):
        #fhand = open("order_probabilities_en.cvs", 'w')

        for pos in self.probabilities:
            keys = pos.split(',')
            try:
                string = keys[0] + "\t" + keys[1] + "\t" + keys[2] + "\t" + str(self.probabilities[pos])
            except:
                string = keys[0] + "\t" + keys[1] + "\t" + str(self.probabilities[pos])
            sys.stdout.write(string + "\n")

    def import_dict(self, file):
        fhand = open(file)

        for line in fhand:
            words = line.split('\t')
            try:
                key = words[0]+","+words[1]+','+words[2]
                self.probabilities[key] = int(words[3])
            except:
                key = words[0] + "," + words[1]
                self.probabilities[key] = int(words[2])

