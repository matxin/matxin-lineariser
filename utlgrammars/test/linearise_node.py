with open('hypothesise_node.py') as hypothesise_node:
    exec(hypothesise_node.read())

results = lineariser.linearise_node(treebank[0].get_root())
