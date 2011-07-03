# Function does not always return!
# checkerexpected: SymbolTableException
int a = 1

def conditionalIntSetter(int r, bool b):
    if(b):
        return r

def main():
	a = conditionalIntSetter(5, false)
