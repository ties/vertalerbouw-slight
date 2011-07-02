#
#
# n!
#
#
#
def faculty(int a) -> int:
	if(a > 0):
		return a * faculty(a - 1)
	else:
		return 1

int a = 5

def main():
	print("5!: " + faculty(a))
    ensure(faculty(5)==120)
