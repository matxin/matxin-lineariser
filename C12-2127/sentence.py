from wordline import WordLine

class Sentence:
    @classmethod
    def deserialize(cls, conllu):
        sentence = {}

        for line in conllu:
            line = line[:-1] # strip the trailing newline
            if line == '':
                yield Sentence(sentence)
            else:
                wordline = WordLine(line)
                sentence[wordline.get_id()] = wordline

    def __init__(self, sentence):
        self.sentence = sentence

        for id_, wordline in sentence.items():
            wordline.add_edge(sentence)
