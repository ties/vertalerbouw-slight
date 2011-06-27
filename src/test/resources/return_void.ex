#
#
#
# expected: NoViableAltException

def useless():
	int a = 2

def number() -> int:
	return useless()
	
def main():
	int a = number(
