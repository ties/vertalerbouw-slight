# Test incorrect gebruik van een comment: commentaarregel wordt niet afgesloten

/# This is a single line comment
/# This
	is
	a
	multiline comment #/
def /#comment nested in the line defining a new function#/ foo():
	# This is a comment inside a definition
	int a = 1

/#
	comment comment comment
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	#/
