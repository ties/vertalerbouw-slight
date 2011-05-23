# Test correcte werking van scopes. Definitie van een variabele op een nieuwe scope staat hoger in rang dan de definitie van deze variabele in een scope lager.

int a = 0
int b = 0
if(a==b):
	int a = 1
	if(a==b):
		print("Incorrect: variable a is redefined in new scope")
	else:
		print("Correct")
else:
	print("Incorrect: A and B both initialised to value 0")
