#
#
#

#infer return type
def hoger(int a, int b):
	if a > b:
		return(a)
	return(b)

var c = hoger(1, 2)

def main():
    ensure(c==2)

