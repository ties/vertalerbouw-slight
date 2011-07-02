#
#
# Global + A local variable
#
#
var global = 21

def main():
	var local = 2
    ensure(global==21 and local==2)
