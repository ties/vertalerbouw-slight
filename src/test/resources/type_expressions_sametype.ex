# Test typechecking op correcte bewerkingen


int 	a 	= 0
bool 	b 	= True
char 	c	= 'c'
string	d	= "Str"

int e = a + 1
bool f = b and !b
string g = d + d

def main():
    ensure(e==1 && a==0)
    ensure(!f && b)
    ensure(g=="StrStr")
