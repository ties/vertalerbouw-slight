#
#
# Constant declaration + initialization
#
#
const var a = 0
const int b = 1
const char c = 'd'
const bool d = True
const bool e = False
const string f = "this is a string"

def main():
    ensure(a==0)
    ensure(b==1)
    ensure(c=='d')
    ensure(d)
    ensure(!e)
    ensure(f=="this is a string")
