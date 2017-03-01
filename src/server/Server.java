package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

public class Server implements Runnable {

	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private String recordDir = Server.class.getClassLoader().getResource("").getPath() + "records";
	private UserDatabase udb;
	private static String[] enabledSuites = { "TLS_DHE_DSS_WITH_AES_128_CBC_SHA" };
	private int fileNameCounter = 0;
	private String logPath = Server.class.getClassLoader().getResource("").getPath() + "auditlog.txt";
	private AuditLogger auditLogger;

	public Server(int port) {
		String type = "TLS";
		serverSocket = getServerSocket(port, type);
		udb = new UserDatabase(recordDir);
		fileNameCounter = udb.init();
		auditLogger = new AuditLogger(logPath);
		newListener();
		System.out.println("\nServer Started\n");
	}

	private ServerSocket getServerSocket(int port, String type) {
		ServerSocket ss = null;

		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ss = ssf.createServerSocket(port);

			((SSLServerSocket) ss).setEnabledCipherSuites(enabledSuites);
			((SSLServerSocket) ss).setNeedClientAuth(true);
			serverSocket = ss;
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			System.exit(-1);
		}

		return ss;
	}

	public static void main(String args[]) {
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		new Server(port);
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public void run() {
		try {
			SessionAttributes sessionAttributes = initSession();

			printConnectionInfo(sessionAttributes.cert);
			sendLoginMessage(sessionAttributes);
			while (sessionAttributes.sessionActive) {
				mainMenu(sessionAttributes);
			}

			closeConnection(sessionAttributes);

		} catch (IOException e) {
			System.out.println("Client disconnected");
			numConnectedClients--;
			return;
		} catch (NullPointerException e) {
			numConnectedClients--;
			System.out.println("Client disconnected");
			return;
		}
	}

	private void sendLoginMessage(SessionAttributes sessionAttributes) {
		sessionAttributes.out.println("User autheticated as: " + sessionAttributes.currentUser.toString());
		sessionAttributes.out.println("done");
		
	}

	private SessionAttributes initSession() throws IOException {
		SessionAttributes sessionAttributes = new SessionAttributes(serverSocket);
		newListener();
		numConnectedClients++;
		return sessionAttributes;
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;

			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");

				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				ks.load(Server.class.getResourceAsStream("serverkeystore"), password);
				ts.load(Server.class.getResourceAsStream("servertruststore"), password);

				kmf.init(ks, password);
				tmf.init(ts);
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}

		return null;
	}

	private void printConnectionInfo(X509Certificate cert) {
		String subject = cert.getSubjectDN().getName();
		System.out.println("client connected");
		System.out.println("client name (cert subject DN field): " + subject);
		System.out.println("Issuer " + cert.getIssuerDN());
		System.out.println("Serial: " + cert.getSerialNumber());
		System.out.println(numConnectedClients + " concurrent connection(s)\n");
	}

	private String makeNextFileName() {
		return "Medical_record_" + fileNameCounter++;
	}

	private String getClientMessage(SessionAttributes sessionAttributes) throws IOException, NullPointerException {
		boolean reading = true;
		StringBuilder sb = new StringBuilder();
		while (reading) {
			String s = sessionAttributes.in.readLine();
			if (s.equals("done")) {
				reading = false;
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	private User createUser(X509Certificate cert) {
		// cert.get
		String DN = cert.getSubjectDN().getName();
		String[] DNFragments = DN.split(" ");

		String name = DNFragments[0].substring(3, DNFragments[0].length() - 1);
		String div = DNFragments[1].substring(3, DNFragments[1].length() - 1);
		String type = DNFragments[2].substring(2, DNFragments[2].length() - 1);

		User newUser = null;
		newUser = udb.getUser(name);
		if (newUser != null) {
			return newUser;
		}

		switch (type) {
		case "Patient":
			newUser = new Patient(name);
			break;
		case "Doctor":
			newUser = new Doctor(name, div);
			break;
		case "Nurse":
			newUser = new Nurse(name, div);
			break;
		case "Government":
			newUser = new Government(name);
			break;

		default:
			System.out.println("Undefined user!!!");
			break;
		}
		udb.addUser(newUser);
		return newUser;
	}

	private void createRecord(SessionAttributes sessionAttributes) throws IOException {
		sessionAttributes.out.println("Patient name:");
		sessionAttributes.out.println("done");
		sessionAttributes.out.flush();
		String patient = getClientMessage(sessionAttributes);

		sessionAttributes.out.println("Associated nurse:");
		sessionAttributes.out.println("done");
		sessionAttributes.out.flush();
		String nurse = getClientMessage(sessionAttributes);

		String filename = makeNextFileName();
		MedRecord mr = new MedRecord(recordDir, filename, sessionAttributes.currentUser.getName(), nurse,
				sessionAttributes.currentUser.getDivision(), patient);

		mr.createFile();

		udb.createUsersFromRecord(mr);

		sessionAttributes.out.println("Created record");
		
		auditLogger.addEntry("Successfully created the file ", sessionAttributes.currentUser.getName());
	}

	private HashMap<String, MedRecord> getAvailableRecords(String name, SessionAttributes sessionAttributes)
			throws IOException {
		User u = udb.getUser(name);

		if (u == null) {
			return null;
		}

		HashMap<String, MedRecord> availableRecords = sessionAttributes.ac.getRecords(u);

		if (availableRecords != null) {
			for (String key : availableRecords.keySet()) {
				sessionAttributes.out.println(key);
			}
		}

		return availableRecords;
	}

	private void printMainMenuCommands(SessionAttributes sessionAttributes) {
		sessionAttributes.out.println("");
		sessionAttributes.out.println("Main menu");
		sessionAttributes.out.println("Possible commands: ");
		sessionAttributes.out.println("search name");
		sessionAttributes.out.println("create");
		sessionAttributes.out.println("exit");
		sessionAttributes.out.println("done");
		sessionAttributes.out.flush();
	}

	private void mainMenu(SessionAttributes sessionAttributes) throws IOException {
		printMainMenuCommands(sessionAttributes);

		String command = getClientMessage(sessionAttributes);

		handleMainMenuCommand(command, sessionAttributes);
	}

	private void handleMainMenuCommand(String command, SessionAttributes sessionAttributes) throws IOException {
		String[] commandFragments = command.split(" ");
		switch (commandFragments[0]) {
		case "search":
			String searchEntry = commandFragments[1];
			HashMap<String, MedRecord> availableRecords = getAvailableRecords(searchEntry, sessionAttributes);

			auditLogger.addEntry("search " + searchEntry, sessionAttributes.currentUser.getName());
			
			if (availableRecords == null) {
				sessionAttributes.out.println("");
				sessionAttributes.out.println("Found no available records, returning to the main menu.");
				return;
			}

			boolean editing = true;

			while (editing) {
				System.out.println("Entering edit menu");
				editing = editMenu(availableRecords, sessionAttributes);
			}

			break;
		case "create":
			auditLogger.addEntry("Tried to create a file ", sessionAttributes.currentUser.getName());
			if (sessionAttributes.ac.allowedToCreate()) {
				createRecord(sessionAttributes);
			} else {
				sessionAttributes.out.println("");
				sessionAttributes.out.println("File creation denied");
			}
			break;
		case "exit":
			sessionAttributes.sessionActive = false;
			break;
		default:
			sessionAttributes.out.println("Command not recognized");
			break;
		}
	}

	private void printEditingCommands(SessionAttributes sessionAttributes) {
		sessionAttributes.out.println("");
		sessionAttributes.out.println("Edit menu");
		sessionAttributes.out.println("Possible commands: ");
		sessionAttributes.out.println("read filename");
		sessionAttributes.out.println("append filename entry");
		sessionAttributes.out.println("delete filename");
		sessionAttributes.out.println("back");
		sessionAttributes.out.println("done");
		sessionAttributes.out.flush();
	}

	private boolean editMenu(HashMap<String, MedRecord> availableRecords, SessionAttributes sessionAttributes)
			throws IOException {
		printEditingCommands(sessionAttributes);

		String command = getClientMessage(sessionAttributes);

		return handleEditCommand(command, availableRecords, sessionAttributes);
	}

	private boolean handleEditCommand(String command, HashMap<String, MedRecord> availableRecords,
			SessionAttributes sessionAttributes) {
		String[] commandFragments = command.split(" ");
		String fileName = null;
		MedRecord record = null;

		switch (commandFragments[0]) {
		case "read":
			fileName = commandFragments[1];
			record = availableRecords.get(fileName);
			
			auditLogger.addEntry("Tried to read " + fileName, sessionAttributes.currentUser.getName());

			if (record != null) {
				for (String s : record.getData()) {
					sessionAttributes.out.println(s);
				}
				auditLogger.addEntry("Successfully read " + fileName, sessionAttributes.currentUser.getName());
			} else {
				sessionAttributes.out.println("Invalid file name");
			}
			break;
		case "append":
			fileName = commandFragments[1];
			record = availableRecords.get(fileName);
			auditLogger.addEntry("Tried to append to " + fileName, sessionAttributes.currentUser.getName());
			if (sessionAttributes.ac.allowedToWrite(record)) {
				fileName = commandFragments[1];
				record = availableRecords.get(fileName);
				String entry = command
						.substring("append".length() + " ".length() + commandFragments[1].length() + " ".length());
				record.append(entry, sessionAttributes.currentUser.getName());
				sessionAttributes.out.println("Entry added to medical record");
				auditLogger.addEntry("Successfully to appended to " + fileName, sessionAttributes.currentUser.getName());
			} else {
				sessionAttributes.out.println("Access denied.");
			}

			break;
		case "delete":
			auditLogger.addEntry("Tried to delete " + fileName, sessionAttributes.currentUser.getName());
			if (sessionAttributes.ac.allowedToDelete()) {
				fileName = commandFragments[1];
				record = availableRecords.get(fileName);
				record.erase();
				availableRecords.remove(fileName);
				(udb.getUser(record.getDoctor())).removeAssociationWithRecord(record);
				(udb.getUser(record.getNurse())).removeAssociationWithRecord(record);
				(udb.getUser(record.getPatient())).removeAssociationWithRecord(record);
				sessionAttributes.out.println("File deleted.");
				auditLogger.addEntry("Successfully deleted " + fileName, sessionAttributes.currentUser.getName());
			} else {
				sessionAttributes.out.println("Access denied.");
			}
			
			break;
		case "back":
			return false;

		default:
			sessionAttributes.out.println("Command not recognized");
			break;
		}

		return true;
	}

	private void closeConnection(SessionAttributes sessionAttributes) throws IOException {
		sessionAttributes.in.close();
		sessionAttributes.out.close();
		sessionAttributes.socket.close();
		numConnectedClients--;
		printDisconnectInfo();
	}

	private void printDisconnectInfo() {
		System.out.println("client disconnected");
		System.out.println(numConnectedClients + " concurrent connection(s)\n");
	}

	private class SessionAttributes {
		SSLSocket socket;
		SSLSession session;
		X509Certificate cert;
		User currentUser;
		AccessController ac;
		PrintWriter out;
		BufferedReader in;
		boolean sessionActive = true;

		public SessionAttributes(ServerSocket serverSocket) throws IOException {
			socket = (SSLSocket) serverSocket.accept();
			session = socket.getSession();
			cert = session.getPeerCertificateChain()[0];
			currentUser = createUser(cert);
			ac = new AccessController(currentUser);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
	}
}
