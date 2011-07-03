#
#
#
# Becomes with variable lhs


int a = 1
int b = 2 

def main():
	a=b=a+b
    ensure(a==3 and b==3)
