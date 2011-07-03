#
# checkerexpected: IllegalFunctionDefinitionException

def getNumber(int n) -> int:
	if n == 0:
		return 0
    if n == 1:
        return 1

def main():
    int number = getNumber(5)

