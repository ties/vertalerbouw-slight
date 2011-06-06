# Test typechecking op int-bool optelling
# checkerexpected: SymbolTableException


int 	a 	= 0
bool 	b 	= True
char 	c	= 'c'
string	d	= "Str"

# Incorrecte types
string e = a + d
