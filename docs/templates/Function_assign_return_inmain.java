public class Function_assign_return_inmain {
	
	private static int hoger(int a, int b){
		if (a > b)
			return(a);
		return (b);
	}
	
	public static void main(String[] args){
	    int c = hoger(1, 2);
	}
}
