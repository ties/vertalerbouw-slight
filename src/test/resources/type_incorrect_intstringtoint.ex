# Test typechecking op int-bool optelling
# checkerexpected: IncompatibleTypesException

int 	a 	= 0
bool 	b 	= True
char 	c	= 'c'
string	d	= "Str"

# Incorrecte types
int e = a + d
