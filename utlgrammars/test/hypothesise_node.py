import sys

sys.path.append('..')

from lineariser import Lineariser
from sentence import Sentence

with open('hypothesise_node.conllu') as conllu:
    treebank = [sentence for sentence in Sentence.deserialise(conllu)]

lineariser = Lineariser()

with open('hypothesise_node.xml') as xml:
    lineariser.deserialise(xml)

hypothesis = [lineariser.hypothesise_node(treebank[0].get_root(), 0)]
