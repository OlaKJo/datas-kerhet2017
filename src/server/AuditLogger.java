package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditLogger {
	String loggPath;

	public AuditLogger(String loggPath) {
		this.loggPath = loggPath;
		IOHandler.createFile(loggPath);
	}

	public void addEntry(String content, String name) {

		List<String> entry = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formatedDate = dateFormat.format(date);
		entry.add("");
		entry.add(content + " " + formatedDate + " by " + name);
		IOHandler.append(loggPath, entry);
	}
}
