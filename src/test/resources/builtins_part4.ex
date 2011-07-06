#
# Alle builtins zoals in de documentatie
#
#

def main():
	# $<$	            &	int	            &	int	            &	bool\\
	ensure(0 < 1)
	# $<$=            &	int	            &	int	            &	bool\\
	ensure(0 <= 1)
	ensure(!(1 <= 0))
	# $>$	            &	int         	&	int	            &	bool\\
	ensure(1 > 0)
	# $>$=            &	int	            &	int	            &	bool\\
	ensure(0 >= 0)
	ensure(1 >= 0)
	# $<$	            &	char            &	char            &	bool\\
	ensure('a' < 'b')
	# $<$=            &	char	        &	char	        &	bool\\
	ensure('a' <= 'a')
	# $>$	            &	char         	&	char	        &	bool\\
	ensure('b' > 'a')
	# $>$=            &	char	        &	char	        &	bool\\
	ensure('b' >= 'b')
	
