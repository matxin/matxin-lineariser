import sys
from argparse import ArgumentParser

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
argument_parser.add_argument('--lemma-file', type=str)
arguments = argument_parser.parse_args()

try:
    with open(arguments.lemma_file) as lemma_file:
        lemmas = [line[:-1] for line in lemma_file]
except (TypeError):
    lemmas = []

def proc_node(h, n, i, r, cnf):  #{
    rule = {}
    head = nodes[i]
    lin_h = int(head[0])
    pos_h = head[3]
    deprel_h = head[7]
    rule[lin_h] = (pos_h, deprel_h)
    if i in h:  #{
        for child in h[i]:  #{
            dep = nodes[child]
            lin_c = int(dep[0])
            pos_c = dep[3]
            deprel_c = dep[7]
            rule[lin_c] = (pos_c, deprel_c)
        #}
        k = list(rule.keys())
        k.sort()
        #print('R', (lin_h, pos_h, deprel_h), rule);
        childs = ''
        rhead = ''
        config_head = ''
        config_childs = ''
        for ord in range(0, len(k)):  #{
            j = k[ord]
            #print(' ', j, rule[j], ':', ord);
            if j == lin_h:  #{
                rhead = '/'.join([str(ord)] + list(rule[j]))
                config_head = '/'.join(rule[j])
            else:  #{
                if childs == '':  #{
                    childs = '/'.join([str(ord)] + list(rule[j]))
                    config_childs = '/'.join(rule[j])
                else:  #{
                    childs = childs + '|' + '/'.join([str(ord)] + list(rule[j]))
                    config_childs = config_childs + '|' + '/'.join(rule[j])
                #}
                #}

                #}
        rule = rhead + '!' + childs
        config_rule = config_head + '!' + config_childs

        if rule not in r:  #{
            r[rule] = 0
        #}
        r[rule] += 1
        if config_rule not in configs:  #{
            configs[config_rule] = {}
        #}
        if rule not in configs[config_rule]:  #{
            configs[config_rule][rule] = 0
        #}
        configs[config_rule][rule] += 1
        for child in h[i]:  #{
            res = proc_node(h, n, child, r, cnf)
            r = res[0]
            cnf = res[1]
        #}
        #}
    return (r, cnf)


#}

### Process a CoNLL-U file

for line in sys.stdin.readlines():  #{

    if line[0] == '#':  #{
        continue
    #}

    # Add a node to the tree.
    if line.count('\t'):  #{
        row = line.split('\t')

        if row[0].count('-') > 0:  #{
            continue
        #}

        bas = int(row[6])
        cur = int(row[0])

        if bas not in heads:  #{
            heads[bas] = []
        #}
        heads[bas].append(cur)
        if cur not in nodes:  #{
            nodes[cur] = row
        #}
        #}

    if line == '\n':  #{
        for i in heads[0]:  #{
            (rules, configs) = proc_node(heads, nodes, i, rules, configs)
        #}
        heads = {}
        nodes = {}
    #}
    #}

    ### Now we print out the rules we learnt.

print('<?xml version="1.0"?>')
print('<linearisation-rules>')
for c in configs:  #{
    total = 0
    for r in configs[c]:  #{
        total = total + configs[c][r]
    #}
    for r in configs[c]:  #{
        prob = float(configs[c][r]) / float(total)
        print(
            '%.2f\t%d\t%d\t%s\t%s' % (prob, total, configs[c][r], c, r),
            file=sys.stderr)
        head = r.split('!')[0].split('/')
        deps = r.split('!')[1].split('|')

        print('<def-rule p="%.4f">' % (prob))
        print('  <NODE ord="%s" si="%s" pos="%s">' %
              (head[0], head[2], head[1]))
        for dep in deps:  #{
            deprow = dep.split('/')
            print('     <NODE ord="%s" si="%s" pos="%s"/>' %
                  (deprow[0], deprow[2], deprow[1]))
        #}
        print('  </NODE>')
        print('</def-rule>')
    #}
    #}
print('</linearisation-rules>')
