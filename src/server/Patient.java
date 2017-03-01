package server;

public class Patient extends User {
	public Patient(String name) {
		super(name, "noDiv");
	}
	
	public String toString(){
		return name + ", Patient";
	}
}
