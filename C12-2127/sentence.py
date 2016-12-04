from wordline import WordLine

class Sentence:
    @classmethod
    def deserialize(cls, conllu):
        sentence = {}

        for line in conllu:
            if line == '':
                yield Sentence(sentence)
            else:
                wordline = WordLine(line)
                sentence[wordline.get_id()] = wordline

    def __init__(self, sentence):
        pass
