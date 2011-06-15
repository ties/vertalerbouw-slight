#
#
# Test van 'paren'
#
# De set paren wordt niet afgesloten. De error handling van ANTLR lost dit goed op.
# Intern wordt er een MissingTokenException gegooid, maar die wordt opgegeten.
def main():
	(3 + 5

