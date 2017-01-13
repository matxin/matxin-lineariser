"""
An implementation of an linearisation algorithm as described here: https://aclweb.org/anthology/D/D12/D12-1085.pdf
"""

import Convert2dependencytree
import DependencyTree
import GreedyDomains
import GreedyLifting
import GreedyLinearisation

import Linearisation.Dependency_tree_linearisation
import Linearisation.NeuralNet

import nltk

import copy
import argparse
import sys

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

        lifting = GreedyLifting.GreedyLifting()

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
        else:
            id += 1
            bleu += nltk.translate.bleu_score.sentence_bleu(gold_order, linearised, weights=(1.0,))

    print(bleu/float(id))

def lift_linearise_greedy(prob_path):
    id = 0
    greedy = GreedyLinearisation.GreedyLinearisation()
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
        else:
            id += 1
            bleu += nltk.translate.bleu_score.sentence_bleu(gold_order, linearised, weights=(1.0,))

    print(bleu/float(id))

def lift_linearise_greedy_domains(prob_path):
    id = 0
    greedy = GreedyDomains.GreedyDomains()
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

        #if len(tree.tree) > 30:
         #   continue

        lifting = GreedyLifting.GreedyLifting()
        tree1 = lifting.execute(tree)

        tmp = []

        tree1.calculate_domains()

        #for node in tree1.tree:
         #   print (node, tree1.tree[node].give_domain())

        linearised = greedy.linearise(tree1)

        gold_order = tree1.give_gold_order()

        #print (str(gold_order)+'\n'+str(linearised))

        weights = ()
        for i in range(0, len(gold_order)):
            weights += (1.0/float(len(gold_order)),)

        id += 1
        bleu += nltk.translate.bleu_score.sentence_bleu(gold_order, linearised, weights=weights)
    try:
        print(bleu/float(id))
    except:
        print ("ONLY BIG SENTENCES.")

def lift_linearise_xml(prob_path):
    id = 0
    greedy = GreedyLinearisation.GreedyLinearisation()
    greedy.import_dict(prob_path)
    tmp = []
    bleu = 0.0

    order = ['0']
    tree = DependencyTree.DependencyTree()
    i = 0
    xml2conllu = {
        "lem": '_',
        "pos": '_',
        "smi": '_',
        "si": '_',
        "ref": '_',
        "head": '_'

    }
    unnumbered = []
    input = sys.stdin.readlines()

    for line in input:
        words = line.split()

        if (words[0] != '<NODE') and (words[0] != '</NODE>'):  # if not a node or an end of a node
            continue

        if words[0] == '</NODE>':
            order.pop()
            continue

        i += 1
        list = []

        xml2conllu = {
            "lem": '_',
            "pos": '_',
            "smi": '_',
            "si": '_',
            "ref": '_',
            "head": '_'

        }

        for word in words[1:-1]:
            # print (word)
            (feature, val) = word.split('="')
            val = val.strip('\"')
            if feature in xml2conllu:
                xml2conllu[feature] = val

        # print (xml2conllu["ref"], order)
        xml2conllu["head"] = order[-1]

        xml2conllu["ref"] = str(i)

        mi = xml2conllu["smi"].split('|')
        mi.pop(0)
        mi = '|'.join(mi)

        if mi == '':
            mi = '_'
        list = [xml2conllu["ref"], xml2conllu["lem"], xml2conllu["lem"], xml2conllu["pos"], '_', mi, xml2conllu["head"],
                xml2conllu["si"], '_', '_']
        tree.add_node(list)
        # sys.stdout.write(str(list)+'\n')
        # print (words[-1].split('/'))

        if [words[-1]] != words[-1].split('/'):  # a node without children
            # print ("L")
            continue

        order.append(xml2conllu["ref"])

    tree.add_children()

    tree.calculate_domains()

    tree.set_neigbouring_nodes()

    lifting = GreedyLifting.GreedyLifting()
    tree1 = lifting.execute(tree)

    linearised = greedy.linearise(tree1)

    id = 1
    for line in input:
        words = line.split()
        #print (words)
        if words[0] != '<NODE':  # if not a node or an end of a node
            sys.stdout.write(line)
            continue

        tmp = linearised.index(str(id))+1
        words = words[:1] + [('ord="%d"' % tmp)] + words[1:]
        line = ' '.join(words)
        sys.stdout.write(line + '\n')
        #print (line)
        id += 1




if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument('--train', action='store_true')
    parser.add_argument('-l', '--lang', type=str, help="Path to the probabilities file")
    parser.add_argument('--xml', action="store_true")

    args = parser.parse_args()

    if args.train is True:
        #print ("YES!")
        gen_prob()

    else:
        if args.xml is True:
            lift_linearise_xml(args.lang)
        else:
            lift_linearise_greedy_domains(args.lang)
    #linearisation()
    #lifting()
