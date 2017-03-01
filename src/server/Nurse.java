package server;

public class Nurse extends User {
	public Nurse(String name, String division) {
		super(name, division);

	}
	
	public String toString(){
		return name + ", Nurse at " + division;
	}
}
