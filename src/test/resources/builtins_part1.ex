#
# Alle builtins zoals in de documentatie
#
#

def main():
	# =               &   bool            &   bool            &   bool\\
	bool a = True
	bool b = False
	b = a
	ensure(b == a)
	# &   char            &   char            &   char\\
	char c = 'a'
	char d = 'b'
	c = d
	ensure(c == d)
	# =               &   int             &   int             &   int\\
	int e = 42
	int f = -1
	f = e
	ensure(f == e)
	# =               &   string          &   string          &   string\\
	string g = "String1"
	string h = "String2"
	h = g
	ensure(h == g)

