#
#
# tests assingment of read with multiple parameters
# checkerexpected: SymbolTableException

int a = 2
int b = 4

def main():
    b = read(a,b)
    ensure(a==b)
