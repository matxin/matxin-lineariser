class Printing:
    @classmethod
    def shift_str(cls, str_, shift=1, shiftwidth=2):
        return str_.replace('\n', '\n' + ' ' * shift * shiftwidth)

    @classmethod
    def print_dict(cls, dict_, print_key=repr, print_value=repr):
        def print_item(item):
            return print_key(item[0]) + ': ' + print_value(item[1])

        dict_items_list_ = list(dict_.items())
        dict_items_list_.sort()
        return cls.print_iterable(
            cls.get_name(dict_), dict_items_list_, print_item)

    @classmethod
    def get_name(cls, object_):
        class_ = object_.__class__
        return class_.__module__ + '.' + class_.__name__

    @classmethod
    def print_iterable(cls, name_, iterable_, print_item):
        iterable_len_ = len(iterable_)
        str_ = name_ + ' of len ' + str(iterable_len_) + ' = {'

        if iterable_len_ != 0:
            str_ += '\n  ' + print_item(iterable_[0])

            for index in range(1, iterable_len_):
                str_ += ',\n' + \
                        '  ' + print_item(iterable_[index])

            str_ += '\n'

        str_ += '}'
        return str_

    @classmethod
    def print_list(cls, list_, print_item=repr):
        return cls.print_iterable(cls.get_name(list_), list_, print_item)
