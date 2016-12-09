class Printing:
    @classmethod
    def shift_str(cls, str_, shift=1, shiftwidth=2):
        return str_.replace('\n', '\n' + ' ' * shift * shiftwidth)

    @classmethod
    def print_dict(cls,
                   dict_,
                   print_key=str,
                   print_value=str,
                   shift=1,
                   shiftwidth=2):
        def print_item(item):
            return print_key(item[0]) + ': ' + print_value(item[1])

        dict_items_list_ = list(dict_.items())
        dict_items_list_.sort()
        return cls.print_iterable(
            cls.get_module_qualname(dict_), dict_items_list_, print_item,
            shift, shiftwidth)

    @classmethod
    def get_module_qualname(cls, object_):
        class_ = object_.__class__
        return '{0}.{1}'.format(class_.__module__, class_.__qualname__)

    @classmethod
    def print_iterable(cls, qualname_, iterable_, print_item, shift,
                       shiftwidth):
        def print_item_(item):
            return cls.shift_str(print_item(item), shift, shiftwidth)

        iterable_len_ = len(iterable_)
        str_ = qualname_ + ' of len ' + str(iterable_len_) + ' = {'

        if iterable_len_ != 0:
            str_ += '\n  ' + print_item_(iterable_[0])

            for index in range(1, iterable_len_):
                str_ += ',\n' + \
                        '  ' + print_item_(iterable_[index])

            str_ += '\n'

        str_ += '}'
        return str_

    @classmethod
    def print_list(cls, list_, print_item=str, shift=1, shiftwidth=2):
        return cls.print_iterable(
            cls.get_module_qualname(list_), list_, print_item, shift,
            shiftwidth)

    @classmethod
    def print_tuple(cls, tuple_, print_item=[], shift=1, shiftwidth=2):
        tuple_len_ = len(tuple_)
        print_item.extend([str for x in range(tuple_len_ - len(print_item))])
        str_ = cls.get_module_qualname(tuple_) + ' of len ' + str(
            tuple_len_) + ' = {'

        if tuple_len_ != 0:
            str_ += '\n  ' + cls.shift_str(print_item[0](tuple_[0]), shift,
                                           shiftwidth)

            for index in range(1, tuple_len_):
                str_ += ',\n' + \
                        '  ' + cls.shift_str(print_item[index](tuple_[index]),
                                             shift, shiftwidth)

            str_ += '\n'

        str_ += '}'
        return str_

    @classmethod
    def print_frozenset(cls, frozenset_, print_item=str, shift=1,
                        shiftwidth=2):
        return cls.print_list(frozenset_, print_item, shift, shiftwidth)
