class GreedyLinearisation:
    def __init__(self):
        self.probabilities = {} # dict with POSs and indices

    def add_case(self, pos, pos1, pos2):
        self.probabilities[pos+","+pos1+","+pos2] = self.probabilities.get(pos+","+pos1+","+pos2, 0) + 1

    def get_prob(self, pos, pos1, pos2):
        prob_max = 0
        id = None

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
        fhand = open("order_probabilities.cvs", 'w')

        for pos in self.probabilities:
            keys = pos.split(',')
            string = keys[0] + "\t" + keys[1] + "\t" + keys[2] + "\t" + str(self.probabilities[pos])
            fhand.write(string + "\n")

    def import_dict(self, file):
        fhand = open(file)

        for line in fhand:
            words = line.split('\t')
            key = words[0]+","+words[1]+','+words[2]
            self.probabilities[key] = int(words[3])

