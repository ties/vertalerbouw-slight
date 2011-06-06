#
#
# Test scenario waarbij een variabele wordt gebruikt in een lagere scope dan waarin hij is gedefinieerdy
# checkerexpected: SymbolTableException
#
#

int a = 0
int b = 0
if(a==b):
	int c = 2
# Hieronder hoort een fout op te treden, c bestaat niet op deze scope
print(c)
