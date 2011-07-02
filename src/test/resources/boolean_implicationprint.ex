#
#
# Boolean logic
#
#Implication;
# a -> b
def implication(bool a, bool b):
	if(a):
		if(b):
            return True
        else:
            return False
	else:
		return True

def main():
    ensure(implication(False, False))
	ensure(implication(False, True))
	ensure(!implication(True, False))
	ensure(implication(True, True))
