# Test typechecking op int-bool optelling
# checkerexpected: SymbolTableException

int 	a 	= 0
bool 	b 	= True
char 	c	= 'c'
string	d	= "Str"

# Incorrecte types
bool e = a + d
