from matxin_lineariser.statistical_linearisation.DependencyTree import DependencyTree
from matxin_lineariser.statistical_linearisation.GreedyLifting import GreedyLifting

from argparse import ArgumentParser
from sys import stdin, stderr

# This file extracts local linearisation rules from a treebank in CoNLL-U format.
# The format of the rules is
#
#   HEAD {order} -> [DEP_1 {order}, DEP_2 {order}, DEP_3 {order}, ...]
#
#

rules = {}
configs = {}
domains = {}
heads = {}
nodes = {}

argument_parser = ArgumentParser()
argument_parser.add_argument(
    '--lemma-file',
    type=str,
    help='the name of the file to get rule-differentiating lemmas from')
argument_parser.add_argument(
    '--projectivise',
    action='store_true',
    help='projectivise each sentence before processing it')
arguments = argument_parser.parse_args()

try:
    with open(arguments.lemma_file) as lemma_file:
        lemmas = [line[:-1] for line in lemma_file]
except (TypeError):
    lemmas = []


def proc_node(h, n, i, r, cnf):
    rule = {}
    head = nodes[i]
    lin_h = int(head[0])
    pos_h = head[3]
    deprel_h = head[7]
    rule[lin_h] = (pos_h, deprel_h)
    lem_h = head[2]

    if lem_h in lemmas:
        rule[lin_h] += (lem_h, )

    if i in h:
        for child in h[i]:
            dep = nodes[child]
            lin_c = int(dep[0])
            pos_c = dep[3]
            deprel_c = dep[7]
            rule[lin_c] = (pos_c, deprel_c)
            lem_c = dep[2]

            if lem_c in lemmas:
                rule[lin_c] += (lem_c, )

        k = list(rule.keys())
        k.sort()
        childs = ''
        rhead = ''
        config_head = ''
        config_childs = ''

        for ord_ in range(0, len(k)):
            j = k[ord_]

            if j == lin_h:
                rhead = '/'.join([str(ord_)] + list(rule[j]))
                config_head = '/'.join(rule[j])
            else:
                if childs == '':
                    childs = '/'.join([str(ord_)] + list(rule[j]))
                    config_childs = '/'.join(rule[j])
                else:
                    childs = childs + '|' + '/'.join([str(ord_)] + list(rule[
                        j]))
                    config_childs = config_childs + '|' + '/'.join(rule[j])

        rule = rhead + '!' + childs
        config_rule = config_head + '!' + config_childs

        if rule not in r:
            r[rule] = 0

        r[rule] += 1

        if config_rule not in configs:
            configs[config_rule] = {}

        if rule not in configs[config_rule]:
            configs[config_rule][rule] = 0

        configs[config_rule][rule] += 1

        for child in h[i]:
            res = proc_node(h, n, child, r, cnf)
            r = res[0]
            cnf = res[1]

    return (r, cnf)


### Process a CoNLL-U file

if arguments.projectivise:
    dependency_tree = DependencyTree()

for line in stdin.readlines():
    if line[0] == '#':
        continue

    # Add a node to the tree.
    if line.count('\t'):
        row = line.split('\t')

        if row[0].count('-') > 0:
            continue

        if arguments.projectivise:
            dependency_tree.add_node(row)
        else:
            bas = int(row[6])
            cur = int(row[0])

            if bas not in heads:
                heads[bas] = []

            heads[bas].append(cur)

            if cur not in nodes:
                nodes[cur] = row

    if line == '\n':
        if arguments.projectivise:
            dependency_tree.add_children()
            dependency_tree.calculate_domains()
            dependency_tree.set_neigbouring_nodes()
            greedy_lifting = GreedyLifting()
            dependency_tree = greedy_lifting.execute(dependency_tree)

            for dependency_tree_node in dependency_tree.tree.values():
                id_ = int(dependency_tree_node.fields['id'])
                head = int(dependency_tree_node.fields['head'])

                if head not in heads:
                    heads[head] = []

                heads[head].append(id_)
                nodes[id_] = [
                    dependency_tree_node.fields['id'],
                    dependency_tree_node.fields['form'],
                    dependency_tree_node.fields['lemma'],
                    dependency_tree_node.fields['upostag'],
                    dependency_tree_node.fields['xpostag'],
                    dependency_tree_node.fields['feats'],
                    dependency_tree_node.fields['head'],
                    dependency_tree_node.fields['deprel'],
                    dependency_tree_node.fields['deps'],
                    dependency_tree_node.fields['misc']
                ]

            dependency_tree = DependencyTree()

        for i in heads[0]:
            (rules, configs) = proc_node(heads, nodes, i, rules, configs)

        heads = {}
        nodes = {}

### Now we print out the rules we learnt.

print('<?xml version="1.0"?>')
print('<linearisation-rules>')
configs = list(configs.items())
configs.sort()

for c, i in configs:
    total = 0
    i = list(i.items())
    i.sort()

    for r, j in i:
        total = total + j

    for r, j in i:
        prob = float(j) / float(total)
        print('%.2f\t%d\t%d\t%s\t%s' % (prob, total, j, c, r), file=stderr)
        head = r.split('!')[0].split('/')
        deps = r.split('!')[1].split('|')

        print('<def-rule p="%.4f">' % (prob))

        if len(head) == 4:
            print('  <NODE ord_="%s" si="%s" pos="%s" lem="%s">' %
                  (head[0], head[2], head[1], head[3]))
        else:
            print('  <NODE ord_="%s" si="%s" pos="%s">' %
                  (head[0], head[2], head[1]))

        for dep in deps:
            deprow = dep.split('/')

            if len(deprow) == 4:
                print('     <NODE ord_="%s" si="%s" pos="%s" lem="%s"/>' %
                      (deprow[0], deprow[2], deprow[1], deprow[3]))
            else:
                print('     <NODE ord_="%s" si="%s" pos="%s"/>' %
                      (deprow[0], deprow[2], deprow[1]))

        print('  </NODE>')
        print('</def-rule>')

print('</linearisation-rules>')
