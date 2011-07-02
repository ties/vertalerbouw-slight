#
#
#
int c = 5

#infer return type
def hoger(int a, int b):
	if a > b:
		return(a)
	return(b)

#functie zonder parameter aanmaken
def verlagen():
	c = c - 1

def main():
	ensure(hoger(1, 2)==2)
	#print = functie! Haakjes zijn mooi. Niet print 'statement'
	#zie ook http://stackoverflow.com/questions/4413912/why-is-print-not-a-function-in-python
	#functie zonder parameter aanroepen
    ensure(c==5)	
    verlagen()
    ensure(c==4)
    verlagen()
    ensure(c==3)
