#
#
# Test correcte werking van scopes. Definitie van een variabele op een nieuwe scope staat hoger in rang dan de definitie van deze variabele in een scope lager.
#
#

def main():
	int a = 0
	int b = 0
	if(a==b):
		int a = 1
		ensure(a!=b)
