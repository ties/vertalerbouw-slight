#
#
# Return in eerste helft if + buiten if
#
#
def rettest(int i):
	if(i > 0):
		return -1
	else:
		
	return 1

def main():
	ensure(rettest(5) == -1)
	ensure(rettest(-5) == 1)
