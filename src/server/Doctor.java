package server;

public class Doctor extends Nurse {
	public Doctor(String name, String division) {
		super(name, division);

	}
	
	public String toString(){
		return name + ", Doctor at " + division;
	}
}
