#
# Alle builtins zoals in de documentatie
#
#

def main():
	# and			    &	bool	        &	bool	        &	bool\\
	ensure(True and True)
	# +               &   string          &   string          &   string\\
	ensure("aap" + "noot" == "aapnoot")
	# +               &   string          &   char            &   string\\
	ensure("aa" + 'p' == "aap")
	# +               &   string          &   int             &   string\\
	ensure("string" + 1 == "string1")
	# +               &   string          &   bool            &   string\\
		ensure("True" + True == "Truetrue")
	# +			    &	int	            &	int		        & 	int\\
	ensure(22 + 20 == 42)
	# -			    &	int	            &	int		        & 	int\\
	ensure(42 - 42 == 0)
	# $\ast$		    &	int	            &	int	            &	int\\
	ensure(3*7 == 21)
	# /			    &	int	            &	int	            &	int\\
	ensure(21 / 7 == 3)
	# \% (modulo)	    &	int	            &	int	            &	int\\
	ensure(144 % 12 == 0)

