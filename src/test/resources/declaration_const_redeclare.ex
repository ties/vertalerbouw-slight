#
#
# Correct usage of scope/variable shadowing
#
#
const int a = 0

def main():
	ensure(a==0)
		const char a = 'b'
		ensure(a=='b')

