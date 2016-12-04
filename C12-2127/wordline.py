class WordLine:
    def __init__(self, line):
        fields = line.split('\t')
        # parse the fields in reverse order by popping them off the end
        misc = fields.pop()

        if misc == '_':
            self.misc = None
        else:
            self.misc = dict(field.split('=') for field in misc.split('|'))

        deps = fields.pop()

        if deps == '_':
            self.deps = None
        else:
            self.deps = dict(dep.split(':') for dep in deps.split('|'))

        self.deprel = fields.pop()
        self.head = int(fields.pop())
        feats = fields.pop()

        if feats == '_':
            self.feats = None
        else:
            self.feats = dict(feat.split('=') for feat in feats.split('|'))

        xpostag = fields.pop()

        if xpostag == '_':
            self.xpostag = None
        else:
            self.xpostag = xpostag

        self.upostag = fields.pop()
        self.lemma = fields.pop()
        self.form = fields.pop()
        self.id_ = int(fields.pop()) # to-do: support multiword integer ranges
                                     # e.g. 1-2 or 3.5
