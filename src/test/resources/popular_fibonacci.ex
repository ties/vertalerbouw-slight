#
#
# fibonacci(n)
#
#
#f(0) = 0
#f(1) = 1
#f(n) = f(n - 1) + f(n - 2)
def fibonacci(int n) -> int:
	if n == 0:
		return 0
	if n == 1:
		return 1
	return fibonacci(n - 1) + fibonacci(n - 2)

def main():
	int i = 0
    ensure(fibonacci(9)==34)	
    while(i > 0):
		print("Fibonacci(" + i + ")" + fibonacci(i))
		i = i-1
    
