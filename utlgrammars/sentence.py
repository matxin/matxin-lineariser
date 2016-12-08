from wordline import WordLine


class Sentence:
    @classmethod
    def deserialise(cls, conllu):
        sentence = {}

        for line in conllu:
            line = line[:-1]  # strip the trailing newline
            if line == '':
                yield Sentence(sentence)
            else:
                wordline = WordLine(line)
                sentence[wordline.get_id()] = wordline

    def __init__(self, sentence):
        self.sentence = sentence

        for wordline in sentence.values():
            wordline.add_edge(self)

    def get_sentence(self):
        return self.sentence

    def get_root(self):
        return self.root

    def train(self, grammars):
        """Train grammars on all the nodes."""
        raise NotImplementedError
