import DependencyTree

import sys

order = ['0']
tree = DependencyTree.DependencyTree()
i = 0
xml2conllu = {
    "lem": '_',
    "pos": '_',
    "smi": '_',
    "si":  '_',
    "ref": '_',
    "head": '_'

}
unnumbered = []
input = sys.stdin.readlines()

for line in input:
    words = line.split()

    if (words[0] != '<NODE') and (words[0] != '</NODE>'): # if not a node or an end of a node
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
        #print (word)
        (feature, val) = word.split('="')
        val = val.strip('\"')
        if feature in xml2conllu:
            xml2conllu[feature] = val

    #print (xml2conllu["ref"], order)
    xml2conllu["head"] = order[-1]

    xml2conllu["ref"] = str(i)

    mi = xml2conllu["smi"].split('|')
    mi.pop(0)
    mi = '|'.join(mi)

    if mi == '':
        mi = '_'
    list = [xml2conllu["ref"], '_', xml2conllu["si"], xml2conllu["pos"], '_', mi, xml2conllu["head"], xml2conllu["si"], '_', '_']
    tree.add_node(list)
    #sys.stdout.write(str(list)+'\n')
    #print (words[-1].split('/'))

    if [words[-1]] != words[-1].split('/'): # a node without children
        #print ("L")
        continue

    order.append(xml2conllu["ref"])
