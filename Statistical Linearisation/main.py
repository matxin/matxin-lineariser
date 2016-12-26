"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""

import copy, Convert2dependencytree, Linearisation.Dependency_tree_linearisation, Lifting.GreedyAlgorithm, sys, \
    Linearisation.NeuralNet, GreedyLinearisation.GreedyLinearisation, nltk, argparse

def lifting():

    inp = sys.stdin.readlines()
    tmp = []

    for tree_raw in inp:
        #print (tree_raw)
        if tree_raw != '\n':
            tmp.append(tree_raw)
            continue

        test = Convert2dependencytree.Convert2dependencytree(tmp)
        tree = test.ref_tree()

        lifting = Lifting.GreedyAlgorithm.GreedyAlgorithm()

        #print (len(tree.tree))

        tree1 = lifting.execute(tree)

        tree1.generate_conllu()

        tmp = []

def gen_prob():
    greedy = GreedyLinearisation.GreedyLinearisation.GreedyLinearisation()
    inp = sys.stdin.readlines()
    tmp = []

    linearised = gold_order = []

    for tree_raw in inp:
        if tree_raw != '\n':
            tmp.append(tree_raw)
            continue

        test = Convert2dependencytree.Convert2dependencytree(tmp)
        tree = test.ref_tree()


        n = len(tree.tree)
        #print (n)
        for i in range(1, n+1):
            for j in range(i+1, n+1):
                for k in range(j+1, n+1):
                    greedy.add_case(tree.tree[str(i)].fields["upostag"], tree.tree[str(j)].fields["upostag"],
                                    tree.tree[str(k)].fields["upostag"])
        tmp = []

    greedy.save_dict2file()


def linearisation():
    res = True
    id = 0
    greedy = GreedyLinearisation.GreedyLinearisation.GreedyLinearisation()
    greedy.import_dict("order_probabilities_en.cvs")
    inp = sys.stdin.readlines()
    tmp = []
    bleu = 0.0

    for tree_raw in inp:
        if tree_raw != '\n':
            tmp.append(tree_raw)
            continue

        test = Convert2dependencytree.Convert2dependencytree(tmp)
        tree = test.ref_tree()

        tmp = []
        linearised = greedy.linearise(tree)

        gold_order = tree.give_gold_order()

        if len(gold_order) > 1:
            id += 1
            bleu += nltk.translate.bleu_score.sentence_bleu(gold_order, linearised, weights=(0.5, 0.5))

    print(bleu/float(id))

def lift_linearise(prob_path):
    id = 0
    greedy = GreedyLinearisation.GreedyLinearisation.GreedyLinearisation()
    greedy.import_dict(prob_path)
    inp = sys.stdin.readlines()
    tmp = []
    bleu = 0.0

    for tree_raw in inp:
        if tree_raw != '\n':
            tmp.append(tree_raw)
            continue

        test = Convert2dependencytree.Convert2dependencytree(tmp)
        tree = test.ref_tree()

        lifting = Lifting.GreedyAlgorithm.GreedyAlgorithm()
        tree1 = lifting.execute(tree)

        tmp = []
        linearised = greedy.linearise(tree1)

        gold_order = tree1.give_gold_order()

        # print (gold_order, linearised)

        if len(gold_order) > 1:
            id += 1
            bleu += nltk.translate.bleu_score.sentence_bleu(gold_order, linearised, weights=(0.5, 0.5))

    print(bleu/float(id))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument('--train', action='store_true')
    parser.add_argument('-l', '--lang', type=str, help="Path to the probabilities file")

    args = parser.parse_args()

    if args.train is True:
        #print ("YES!")
        gen_prob()

    else:
        lift_linearise(args.lang)
    #linearisation()
    #lifting()