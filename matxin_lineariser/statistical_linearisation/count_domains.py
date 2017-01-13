import DependencyTree

fhand = open("Treebank/en-ud-train.conllu")

tree = DependencyTree.DependencyTree()

MAX = 0

words = []

for line in fhand:

    if line[0] == '#':  # if that's a comment
        continue

    if len(line) < 2:  # if that's a blank line
        tree.add_children()
        tree.calculate_domains()
        for node in tree.tree:
            MAX= max(len(tree.tree[node].domain), MAX)
        tree = DependencyTree.DependencyTree()
        continue

    words = line.rstrip("\n")

    words = words.split('\t')

    #print (words)

    tree.add_node(words)

print (MAX)
