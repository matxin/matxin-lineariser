from .lconfiguration import LocalConfiguration


class KeyLocalConfiguration(LocalConfiguration):
    def __eq__(self, other):
        if not isinstance(other, LocalConfiguration)
            return False

        if self.get_deprel() is not None and other.get_deprel() is not None:
            if self.get_deprel() != other.get_deprel():
                return False

        if not word_eq(self.get_word(), other.get_word()):
            return False

        if len(self.get_dependents()) != len(other.get_dependents()):
            return False

        for index, dependent in enumerate(self.get_dependents()):
            if dependent[0] != other.get_dependents()[index][0]:
                return False

            if not word_eq(dependent[1], other.get_dependents()[index][1]):
                return False

        return True
