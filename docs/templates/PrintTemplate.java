
public class PrintTemplate {
	private String instance_var = "Aap";
	private final static String instance_constant = "Noot";
	private int instance_primitive = 42;
	
	public void print(int i){
		System.out.println(i);
	}
	
	public void print(String s){
		System.out.println(s);
	}
	
	public PrintTemplate(){
		int i = 3;
		//Print local
		print(i);
		//Print constant string
		print("Hello");
		String hello = "hello";
		//Print local var
		print(hello);
		//Instance variable
		print(instance_var);
		//Constant String
		print(instance_constant);
		//Primitive
		print(instance_primitive);
	}
}
