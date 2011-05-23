#
#
# DeMorgan
#
#

#
# DeMorgan: !(A ^ B) <=> !A v !B
#
def deMorgan(bool a, bool b):
	if(!(a and b) and (!a or !b)):
		return true
	else:
		return false

deMorgan(True, True)
deMorgan(False, False)
deMorgan(True, False)
deMorgan(False, True)
