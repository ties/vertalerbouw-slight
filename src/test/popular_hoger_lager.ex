#
#
# Hoger/Lager spelletje
#
#

const random = random(50)

def main():
	print("Hoger/Lager..")
	var i = -1
	while(i != random): 
		print("Je gokte " + i + " nieuwe poging: ")
		read(i)
		if(i < random):
			print("Hoger")
		if(i > random):
			print("Lager")
	print("Geraden!")
