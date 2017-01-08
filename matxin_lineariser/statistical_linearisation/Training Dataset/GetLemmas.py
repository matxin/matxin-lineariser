import sys

fhand = open("lemmas.csv", 'w')
lemmas = {}
id = 0
for line in sys.stdin.readlines():

    if line[0] == '#':  # if that's a comment
        continue

    if line =="\n":  # if that's a blank line
        continue

    words = line.rstrip("\n")

    words = words.split('\t')

    if words[2] not in lemmas:
        lemmas[words[2]] = id
        id += 1

for a in lemmas:
    fhand.write(str(a)+'\t'+str(lemmas[a])+'\n')