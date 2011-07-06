#
# Alle builtins zoals in de documentatie
#
#

def main():
	# ==              &   bool            &   bool            &   bool\\
	ensure(True == True)
	ensure(False == False)
	# ==              &   int             &   int             &   bool\\
	ensure(0 == 0)
	# ==              &   char            &   char            &   bool\\
	ensure('a' == 'a')
	# ==              &   string          &   string          &   bool\\
	ensure("a" == "a")
	# !=              &   bool            &   bool            &   bool\\
	ensure(True != False)
	ensure(False != True)
	# !=              &   int             &   int             &   bool\\
	ensure(0 != 1)
	# !=              &   char            &   char            &   bool\\
	ensure('a' != 'b')
	# !=              &   string          &   string          &   bool\\
	ensure("a" != "b")
	# or			    &	bool	        &	bool	        &	bool\\
	ensure(True or False)
	ensure(False or True)

