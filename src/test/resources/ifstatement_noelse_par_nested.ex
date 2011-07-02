#
# Test correcte werking van booleans
#
#

bool a = True
bool b = True
bool c = False

def main():
	if(a):
		if(b):
			if(a==b):
				if(a!=(!b)):
				    c = True
    ensure(c)

