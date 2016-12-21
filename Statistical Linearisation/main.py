"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""

import copy, Convert2dependencytree, Linearisation.Dependency_tree_linearisation, Lifting.GreedyAlgorithm, sys, Linearisation.NeuralNet

if __name__ == "__main__":

    res = True
    id = 0
    inp = sys.stdin.readlines()
    tmp = []
    #NN = Linearisation.NeuralNet()

    for tree_raw in inp:
        if tree_raw != '\n':
            tmp.append(tree_raw)
            continue

        test = Convert2dependencytree.Convert2dependencytree(tmp)
        tree = test.ref_tree()

        tmp = []


        #for line in sys.stdin.readlines():
         #   sys.stdout.write(line)

        #linear = Linearisation.Dependency_tree_linearisation.Dependecy_tree_linearisation(tree)

        #linear.execute_algorithm()

        lifting = Lifting.GreedyAlgorithm.GreedyAlgorithm()

        #tree.generate_conllu()

        tree1 = lifting.execute(copy.deepcopy(tree))

        tree1.generate_conllu()

        #for id in tree.tree:
         #   print (id)
          #  print (tree.tree[id].neighbouring_nodes["-2"], tree.tree[id].neighbouring_nodes["-1"], tree.tree[id].neighbouring_nodes["0"], tree.tree[id].neighbouring_nodes["1"], tree.tree[id].neighbouring_nodes["2"])
            #print (tree.ufeat(id, "-2", "Gender"), tree.ufeat(id, "-1", "Gender"), tree.ufeat(id, "0", "Gender"), tree.ufeat(id, "1", "Gender"), tree.ufeat(id, "2", "Gender"))
           # print (tree.deprel(id, "-2"), tree.deprel(id, "-1"), tree.deprel(id, "0"), tree.deprel(id, "1"), tree.deprel(id, "2")))