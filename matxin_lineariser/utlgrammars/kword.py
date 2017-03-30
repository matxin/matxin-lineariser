from .word import Word


class KeyWord(Word):
    def __eq__(self, other):
        if not isinstance(other, Word):
            return False

        if self.get_upostag() != other.get_upostag():
            return False

        if len(self.get_feats()) != 0 and len(other.get_feats()) != 0:
            if self.get_feats() != other.get_feats():
                return False

        if self.get_lemma() is not None and other.get_lemma() is not None:
            if self.get_lemma() != other.get_lemma():
                return False

        return True
