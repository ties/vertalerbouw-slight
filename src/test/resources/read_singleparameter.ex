#
#
# tests assingment of read with single parameter

int a = 2
int b = 4

def main():
    b = read(a)
    ensure(a==b)
