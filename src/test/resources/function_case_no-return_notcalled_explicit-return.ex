# Function does not always return!
# checkerexpected: SymbolTableException
int a = 1

def conditionalIntSetter(int r, bool b) -> int:
    if(b):
        return r

def main():
	int w = 4
