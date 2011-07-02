#
#
# DeMorgan
#
#
# DeMorgan: !(A ^ B) <=> !A v !B
#
def deMorgan(bool a, bool b):
	if(!(a and b) and (!a or !b)):
		return True
	else:
		return False

def main():
	ensure(!deMorgan(True, True))
	ensure(deMorgan(False, False))
	ensure(deMorgan(True, False))
	ensure(deMorgan(False, True))
