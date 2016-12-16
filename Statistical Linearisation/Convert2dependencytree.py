import DependencyTree

class Convert2dependencytree:
    """
    reads a dependency tree in CoNLL-U format and converts it to a tree of clasess Dependency_tree
    """

    def __init__(self, path):
        """
        creates a dependency tree and reads it from a file
        """
        self.tree = DependencyTree.DependencyTree()

        self.read_open_file(path)

        self.tree.add_children()

        self.tree.calculate_domains()

        self.tree.set_neigbouring_nodes()


    def read_open_file(self, path):
        """
        reads a file in conllu, splits fields and invokes adding a node
        :return:
        """
        fhand = open(path)

        for line in fhand:

            if line[0] == '#': # if that's a comment
                continue

            if len(line) < 2: # if that's a blank line
                continue

            words = line.rstrip("\n")

            words = words.split('\t')

            self.tree.add_node(words)

        fhand.close()

    def ref_tree(self):
        """
        used to copy a dependency tree
        :return: the tree
        """

        return self.tree