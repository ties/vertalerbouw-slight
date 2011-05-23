#
#
# Boolean logic
#
#

#Implication;
# a -> b
def implication(bool a, bool b):
	if(a and b):
		print("a -> b")
	else:
		print("a !-> b")

implication(False, True)
implication(True, False)
implication(True, True)
