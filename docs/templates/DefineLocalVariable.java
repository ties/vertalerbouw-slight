
public class DefineLocalVariable {
	public DefineLocalVariable(String arg){
		int a;
		String b;
		char c;
		String d;
		c = 'j';
		b = "Foobar";
		a = 42;
		d = arg;
	}
	
	public static void main(String[] args) {
		new DefineLocalVariable("arg");
	}
}
