#
#
# Test van random int generator
# print 10 random getallen tussen 0 .. i, met i aflopend van 10..0
#
#
var i = 10
const j = 10

def main():
	while(i > 0):
		print(random(j))
		i = i-1

