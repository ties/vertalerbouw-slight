#
#
#
int c = 5

#functie zonder parameter aanmaken
def verlagen():
	c = c - 1

def main():
    ensure(c==5)	
    verlagen()
    ensure(c==4)
    verlagen()
    ensure(c==3)
