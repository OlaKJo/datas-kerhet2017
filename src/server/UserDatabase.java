package server;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class UserDatabase {

	private String recordDir;
	private HashMap<String, User> users = new HashMap<String, User>();

	public UserDatabase(String recordDir) {
		this.recordDir = recordDir;
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void addUser(User u) {
		users.put(u.getName(), u);
	}

	public boolean userExists(String name) {
		return users.containsKey(name);
	}

	public void printDatabase() {
		for (String s : users.keySet()) {
			System.out.println("\n");
			users.get(s).printUser();
		}
	}

	public int init() {
		File folder = new File(recordDir);
		File[] listOfFiles = folder.listFiles();
	
		if (listOfFiles == null) {
			return 0;
		}
		
		for (int i = 0; i < listOfFiles.length; i++) {
			String fileName = listOfFiles[i].getName();
			createUsersFromFile(recordDir, fileName);
		}
		
		return listOfFiles.length;
	}

	public void createUsersFromFile(String dir, String fileName) {
		List<String> lines = IOHandler.read(dir + "/" + fileName);

		String division = null;
		String doctor = null;
		String nurse = null;
		String patient = null;

		for (String line : lines) {
			if (line.startsWith("Division: ")) {
				division = line.substring("Division: ".length());
			} else if (line.startsWith("Doctor: ")) {
				doctor = line.substring("Doctor: ".length());
			} else if (line.startsWith("Nurse: ")) {
				nurse = line.substring("Nurse: ".length());
			} else if (line.startsWith("Patient: ")) {
				patient = line.substring("Patient: ".length());
			}
		}

		MedRecord mr = new MedRecord(dir, fileName, doctor, nurse, division, patient);
		createUsersFromRecord(mr);
	}

	public void createUsersFromRecord(MedRecord mr) {

		String division = mr.getDivision();
		String doctor = mr.getDoctor();
		String nurse = mr.getNurse();
		String patient = mr.getPatient();

		Doctor d = userExists(doctor) ? (Doctor) getUser(doctor) : new Doctor(doctor, division);
		Nurse n = userExists(nurse) ? (Nurse) getUser(nurse) : new Nurse(nurse, division);
		Patient p = userExists(patient) ? (Patient) getUser(patient) : new Patient(patient);

		d.addAssociationWithRecord(mr);
		n.addAssociationWithRecord(mr);
		p.addAssociationWithRecord(mr);
		addUser(d);
		addUser(n);
		addUser(p);
	}
}
