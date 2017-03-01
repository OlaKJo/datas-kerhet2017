package server;

import java.util.HashMap;
import java.util.List;

public class AccessController {
	private User loggedInUser;

	public AccessController(User u) {
		this.loggedInUser = u;
		
	}
	
	public HashMap<String,MedRecord> getRecords(User patient){
		HashMap<String,MedRecord> ret = new HashMap<String,MedRecord>();
		
		for(MedRecord r : patient.getAssociatedRecords()){
			if(allowedToRead(r)){
				ret.put(r.toString(),r);
			}
		}
		return ret;
	}
	
	public boolean allowedToRead(MedRecord record) {
		if(loggedInUser.associatedWith(record)){
			return true;
		}else if(loggedInUser.getDivision().equals(record.getDivision())){
			return true;
		}else if(loggedInUser instanceof Government){
			return true;
		}
		return false;
	}
	
	public boolean allowedToWrite(MedRecord record) {
		return (loggedInUser.associatedWith(record) && !(loggedInUser instanceof Patient) || loggedInUser instanceof Government);
	}
	
	public boolean allowedToDelete() {
		return loggedInUser instanceof Government;
	}

	public boolean allowedToCreate() {
		return loggedInUser instanceof Doctor;
	}
}
