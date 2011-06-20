
public class FinalVariable {
	private final int TRUTH = getValue();
	
	private int getValue(){
		return 42;
	}
	
	public FinalVariable() {
		System.out.println(TRUTH);
	}
}
