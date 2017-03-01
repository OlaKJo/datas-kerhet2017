package client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client {
	private String host;
	private int port;
	private BufferedReader read;
	private PrintWriter toServer;
	private BufferedReader fromServer;
	private SSLSocket socket;
	private String USBname = "USB-JOCKE";
	private boolean communicating;
	private static String[] enabledSuites = { "TLS_DHE_DSS_WITH_AES_128_CBC_SHA" };
	private boolean usbVerification = true;
	private boolean printSuites = false;

	public Client(String[] args) throws IOException {
		dealWithArgs(args);

		read = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter name of USB-drive:");
		String en = read.readLine();
		if(!en.equals("")){
			USBname = en;
		}

//		System.out.print("Enter password:");
//		String password = read.readLine();
		Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(-1);
        }
        char password[] = console.readPassword("Enter password:");

		SSLSocketFactory factory = getSocketFactory(password);

		socket = (SSLSocket) factory.createSocket(host, port);
		

		socket.setEnabledCipherSuites(enabledSuites);

		if (printSuites) {
			printEnableSuites();
		}

		// prepare communication
		initCommunication();

		// communicate
		while (communicating) {
			communicate();
		}

		System.out.println("Closing connection");

		// close conneciton
		closeConnection();
	}

	/**
	 * @throws IOException
	 */
	private void closeConnection() throws IOException {
		fromServer.close();
		toServer.close();
		read.close();
		socket.close();
	}

	/**
	 * @throws IOException
	 */
	private void initCommunication() throws IOException {
		toServer = new PrintWriter(socket.getOutputStream(), true);
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		communicating = true;
	}

	private void printEnableSuites() {
		System.out.println("Enabled cipher suites");
		for (String s : socket.getEnabledCipherSuites()) {
			System.out.println(s);
		}
	}

	private void dealWithArgs(String[] args) {
		if (args.length < 2) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}
		try { /* get input parameters */
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}
	}

	private SSLSocketFactory getSocketFactory(char[] entryPassword) {
		SSLSocketFactory factory = null;
		try {

			KeyStore ks = KeyStore.getInstance("JKS");
			KeyStore ts = KeyStore.getInstance("JKS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			SSLContext ctx = SSLContext.getInstance("TLS");

			if (usbVerification) {
				String usbPath = "/media/" + System.getProperty("user.name") + "/" + USBname + "/";
				if(System.getProperty("os.name").equals("Windows 7")){
					usbPath = "E:\\"; 					
				}
				ks.load(new FileInputStream(usbPath + "clientkeystore"), "password".toCharArray()); // keystore password
																										
				// (storepass)
				ts.load(new FileInputStream(usbPath + "clienttruststore"), "password".toCharArray()); // truststore password
																										// (storepass)
			} else {
				ks.load(Client.class.getResourceAsStream("../clientkeystore"), "password".toCharArray());

				ts.load(Client.class.getResourceAsStream("../clienttruststore"), "password".toCharArray());
			}

			kmf.init(ks, entryPassword); // user password
			tmf.init(ts);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			factory = ctx.getSocketFactory();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return factory;
	}

	private void communicate() throws IOException {
		System.out.println(getServerMessage());
		System.out.println(getServerMessage());
		String msg;
		for (;;) {
			System.out.print(">");
			msg = read.readLine();
			if (msg.equalsIgnoreCase("exit")) {
				communicating = false;
				break;
			}

			toServer.println(msg);
			toServer.println("done");
			toServer.flush();

			System.out.println(getServerMessage());
		}
	}

	private String getServerMessage() throws IOException {
		boolean reading = true;
		StringBuilder sb = new StringBuilder();

		while (reading) {
			String s = fromServer.readLine();
			if (s.equals("done")) {
				reading = false;
			} else {
				sb.append(s);
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		new Client(args);
	}
}