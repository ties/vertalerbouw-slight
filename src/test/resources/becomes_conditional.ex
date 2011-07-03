#
#
#
# tests conditional assignment

int a
bool b = true
bool c = false

def main():
    a = if(b) return 1 else return 0
    ensure(a==1)
    a = if(c) return 2 else return 3
    ensure(a==3)

