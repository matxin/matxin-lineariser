"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""

import Convert2dependencytree, Linearisation.Dependency_tree_linearisation

if __name__ == "__main__":
    test = Convert2dependencytree.Convert2dependencytree()
    tree = test.ref_tree()
   # tree.print_tree()

   # print (tree.head)

    linear = Linearisation.Dependency_tree_linearisation.Dependecy_tree_linearisation(tree)

    linear.execute_algorithm()

    #for id in tree.tree:
     #   print (id)
      #  print (tree.tree[id].neighbouring_nodes["-2"], tree.tree[id].neighbouring_nodes["-1"], tree.tree[id].neighbouring_nodes["0"], tree.tree[id].neighbouring_nodes["1"], tree.tree[id].neighbouring_nodes["2"])
        #print (tree.ufeat(id, "-2", "Gender"), tree.ufeat(id, "-1", "Gender"), tree.ufeat(id, "0", "Gender"), tree.ufeat(id, "1", "Gender"), tree.ufeat(id, "2", "Gender"))
       # print (tree.deprel(id, "-2"), tree.deprel(id, "-1"), tree.deprel(id, "0"), tree.deprel(id, "1"), tree.deprel(id, "2"))