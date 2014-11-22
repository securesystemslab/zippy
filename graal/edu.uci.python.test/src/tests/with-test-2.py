# Gulfem & Qunaibit 03/02/2014
# With Statement (As name as Tuple)

class GrammarTests():

    def test_with_statement(self):
        class manager(object):
            def __enter__(self):
                return (1, 2)
            def __exit__(self, *args):
                pass

        with manager():
            pass
        with manager() as x:
            print(x)
            pass
        with manager() as (x, y):
            print(x , y)
            pass
        with manager(), manager():
            pass
        with manager() as x, manager() as y:
            pass
        with manager() as x, manager():
            pass

g = GrammarTests()
g.test_with_statement()