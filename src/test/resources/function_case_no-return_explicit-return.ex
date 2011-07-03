# Function does not always return!
# checkerexpected: SymbolTableException
int a = 1

def conditionalIntSetter(int r, bool b) -> int:
    if(b):
        return r

def main():
	int a = conditionalIntSetter(2, true)
    ensure(a==2)
    a = conditionalIntSetter(3, false)
    print("a: " + a)
