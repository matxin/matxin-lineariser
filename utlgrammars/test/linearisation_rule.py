import sys

sys.path.append('..')

from grammars import Grammars

from xml.etree import ElementTree

with open('linearisation_rule.xml') as xml:
    linearisation_rules_etree = ElementTree.parse(xml).getroot()

grammars = Grammars()
grammars.deserialise(linearisation_rules_etree)
print(grammars.get_grammars())
