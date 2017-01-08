class GetFeatures:
    def __init__(self):
        self.pos = {
            "ADJ": 1,
            "ADP": 2,
            "ADV": 3,
            "AUX": 4,
            "CCONJ": 5,
            "DET": 6,
            "INTJ": 7,
            "NOUN": 8,
            "NUM": 9,
            "PART": 10,
            "PRON": 11,
            "PROPN": 12,
            "PUNCT": 13,
            "SCONJ": 14,
            "SYM": 15,
            "VERB": 16,
            "X": 17
        }

        self.lemmas = {}
        self.labels = {}

        self.import_lemmas()
        self.import_labels()


    def import_lemmas(self):
        fhand = open("lemmas.csv")

        for line in fhand:
            words = line.split()
            self.lemmas[words[0]] = int(words[1])

    def import_labels(self):
        fhand = open("labels.csv")

        for line in fhand:
            words = line.split()
            self.labels[words[0]] = int(words[1])

if __name__ == "__main__":
    features = GetFeatures()
    print(features.labels)
    #print(features.lemmas)