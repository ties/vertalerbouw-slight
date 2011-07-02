#
#
#

#infer return type
def hoger(int a, int b):
    print("a: "+a)
    print("b: "+b)    
    print(a > b)
	if a > b:
        print("tset")
		return(a)
    print(b)
	return(b)

var c = hoger(1, 2)

def main():
    print(c)
    ensure(c==2)

