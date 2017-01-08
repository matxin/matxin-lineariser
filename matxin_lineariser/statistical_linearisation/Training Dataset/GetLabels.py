import sys

fhand = open("labels.csv", 'w')
labels = {}
id = 0
for line in sys.stdin.readlines():

    if line[0] == '#':  # if that's a comment
        continue

    if line =="\n":  # if that's a blank line
        continue

    words = line.rstrip("\n")

    words = words.split('\t')

    if words[7] not in labels:
        labels[words[7]] = id
        id += 1

for a in labels:
    fhand.write(str(a)+'\t'+str(labels[a])+'\n')