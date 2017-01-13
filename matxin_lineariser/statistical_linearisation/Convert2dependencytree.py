import DependencyTree

import sys

class Convert2dependencytree:
    """
    reads a dependency tree in CoNLL-U format and converts it to a tree of clasess Dependency_tree
    """

    def __init__(self, tree_raw):
        """
        creates a dependency tree and reads it from a file
        """
        self.tree = DependencyTree.DependencyTree()

        self.read_open_file(tree_raw)

        self.tree.add_children()

        self.tree.calculate_domains()

        self.tree.set_neigbouring_nodes()


    def read_open_file(self, tree_raw):
        """
        reads a file in conllu, splits fields and invokes adding a node
        :return:
        """

        for line in tree_raw:

            if line[0] == '#': # if that's a comment
                continue

            words = line.rstrip("\n")

            words = words.split('\t')

            if len(words[0].split('-')) > 1: # id of type: a-b
                continue

            self.tree.add_node(words)

    def ref_tree(self):
        """
        used to copy a dependency tree
        :return: the tree
        """

        return self.tree
