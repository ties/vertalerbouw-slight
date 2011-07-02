#
#
#

#infer return type
def hoger(int a, int b):
	if a > b:
		return(a)
	return (b)

def main():
    int c = hoger(1, 2)
    ensure(c==2)
