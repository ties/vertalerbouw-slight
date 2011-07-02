#
#
# If statement without parentheses
#
bool a
def main():
	if False:
		a = True
	else:
		a = False
    ensure(!a)
