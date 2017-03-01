package server;

import java.util.ArrayList;
import java.util.List;

public abstract class User {

	protected String name;
	protected String division;
	private List<MedRecord> associatedRecords;

	public User(String name, String division) {
		this.name = name;
		this.division = division;
		associatedRecords = new ArrayList<>();
	}

	public String getDivision() {
		return division;
	}

	public String getName() {
		return name;
	}

	public List<MedRecord> getAssociatedRecords() {
		return associatedRecords;
	}

	public boolean associatedWith(MedRecord mr) {
		return associatedRecords.contains(mr);
	}

	public void addAssociationWithRecord(MedRecord mr) {
		associatedRecords.add(mr);
	}

	public void removeAssociationWithRecord(MedRecord mr) {
		associatedRecords.remove(mr);
	}

	public void printUser() {
		System.out.println("name: " + name);
		System.out.println("division: " + division);
	}
}
