package server;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MedRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private String dir;
	private String fileName;
	private String path;
	private String division;
	private String doctor;
	private String nurse;
	private String patient;
	private AuditLogger log;

	public MedRecord(String dir, String fileName, String doctor, String nurse, String division, String patient) {
		this.dir = dir;
		this.fileName = fileName;
		path = dir + "/" + fileName;
		this.doctor = doctor;
		this.nurse = nurse;
		this.division = division;
		this.patient = patient;
	}

	public String getDoctor() {
		return doctor;
	}

	public String getNurse() {
		return nurse;
	}

	public String getPatient() {
		return patient;
	}

	private List<String> createHeader() {
		ArrayList<String> headerLines = new ArrayList<>();
		headerLines.add("Division: " + division);
		headerLines.add("Doctor: " + doctor);
		headerLines.add("Nurse: " + nurse);
		headerLines.add("Patient: " + patient);
		return headerLines;
	}

	public String getPath() {
		return path;
	}

	public void createFile() {
		IOHandler.createFile(path);
		IOHandler.append(path, createHeader());
	}

	public void append(String data, String name) {
		List<String> entry = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formatedDate = dateFormat.format(date);
		entry.add("");
		entry.add("Entry added " + formatedDate + " by " + name);
		entry.add(data);
		IOHandler.append(path, entry);
	}

	public void erase() {
		IOHandler.eraseFile(path);
	}

	public String getDivision() {
		return division;
	}

	public String toString() {
		return fileName;
	}

	public List<String> getData() {
		return IOHandler.read(path);
	}
}
