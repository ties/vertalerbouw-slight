#
# Test correcte werking van booleans
#
#

bool a = True
bool b = True

def main():
	if(a):
		if(b):
			if(a==b):
				if(a!=(!b)):
					if(!a==b):
						print("Correcte werking van boolean")
