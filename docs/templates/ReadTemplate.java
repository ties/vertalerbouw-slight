import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadTemplate {
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	
	private String readString(){
		try{
			return input.readLine();
		} catch(IOException e){
			return "";
		}
	}
	
	private int readInt(){
		try{
			return input.read();
		} catch(IOException e){
			return -1;
		}
	}
	
	private void readInteger(Integer i){
		try{
			i = new Integer(input.read());
		} catch(IOException e){
		}
	}
	
	public ReadTemplate(){
		int i = readInt();
		String aap = readString();
	}
}
