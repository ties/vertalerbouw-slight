#
#
# n!
#
#
#
def faculty(int a):
	if(a > 0):
		return faculty(a - 1)
	else:
		return 1

int a = 5

print("5!: " + faculty(a))
