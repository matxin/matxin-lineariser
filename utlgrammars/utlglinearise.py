from lineariser import Lineariser
from printing import Printing
from sentence import Sentence

import sys

corpus = [sentence for sentence in Sentence.deserialise(sys.stdin)]
lineariser = Lineariser()

with open(sys.argv[1]) as xml:
    lineariser.deserialise(xml)

for sentence in corpus:
    sentence.linearise(lineariser)

print('corpus is ' + Printing.print_list(corpus))
