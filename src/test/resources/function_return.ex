#
#
#

#infer return type
def hoger(int a, int b):
	if a > b:
		return(a)
	return(b)

def main():
	print(hoger(1, 2))
	#print = functie! Haakjes zijn mooi. Niet print 'statement'
	#zie ook http://stackoverflow.com/questions/4413912/why-is-print-not-a-function-in-python
	#functie zonder parameter aanroepen
	verlagen()


#functie zonder parameter aanmaken
def verlagen():
	var c = 5 - 1

