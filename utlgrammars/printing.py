class Printing:
    @classmethod
    def shift_str(cls, str_, shift=1, shiftwidth=2):
        return str_.replace('\n', '\n' + ' ' * shift * shiftwidth)

    @classmethod
    def print_str(cls, str_):
        return 'str of len ' + str(len(str_)) + ' = ' + repr(str_)

    @classmethod
    def print_dict(cls, dict_, print_key=repr, print_value=repr):
        def print_item(item):
            return print_key(item[0]) + ': ' + print_value(item[1])

        dict_items_list_ = list(dict_.items())
        dict_items_list_.sort()
        dict_len_ = len(dict_)
        str_ = 'dict of len ' + str(dict_len_) + ' = {'

        if dict_len_ != 0:
            str_ += '\n  ' + print_item(dict_items_list_[0])

            for index in range(1, dict_len_):
                str_ += ',\n' + \
                        '  ' + print_item(dict_items_list_[index])

            str_ += '\n'

        str_ += '}'
        return str_
