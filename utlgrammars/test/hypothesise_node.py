import sys

sys.path.append('..')

from lineariser import Lineariser
from printing import Printing
from sentence import Sentence

with open(input('conllu: ')) as conllu:
    treebank = [sentence for sentence in Sentence.deserialise(conllu)]

lineariser = Lineariser()

with open(input('xml: ')) as xml:
    lineariser.deserialise(xml)

Printing.print_list(
    lineariser.hypothesise_node(treebank[0].get_root(), 0).instantiate())
