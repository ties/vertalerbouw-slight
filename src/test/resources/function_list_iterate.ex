#
#
# Testen van de lijst functionaliteit
#
#
def main():
	var a = newList()
	int i = 10
	while(i > 0):
		putInt(a, i)
		i = i - 1
	print("Nu lezen")
	int j = 0
	while(j < length(a)):
		print(getInt(a, j)
		j = j + 1
