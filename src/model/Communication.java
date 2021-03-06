/**
 * Each connection to the server should initiate a new Communication object.
 *
 * @author Henrik Johansson
 * @version 2013-02-17
 */

package model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import controller.Workflow;

public class Communication implements Observer {
	private ServerSocket server;
	private Boolean recieveInited = false;
	private CommRecieve recComm;

	private InetAddress iaddr = null;
	private String message = null;
	private FileManagement fileMan;

	private final int CLIENT_PORT = 4444;

	/**
	 * @param server
	 * @param flow
	 */
	public Communication(ServerSocket server, Workflow flow) {
		this.server = server;
		fileMan = new FileManagement();

		recieveInit();
		System.out.println("Under recieve"); // TODO remove this debug
	}

	public void update(Observable o, Object arg) {
		if (o instanceof CommRecieve) {

			if (arg instanceof InetAddress) {
				System.out.println("You have recieved a message from "
						+ (InetAddress) arg);
				iaddr = (InetAddress) arg;
			} else if (arg instanceof String) {
				System.out.println("You recieved this message " + (String) arg);
				message = (String) arg;
				messageRecieved();
			}
		}
	}

	/**
	 * Check what type of message has been recieved. For now it only check for
	 * login recieved.
	 */
	private void messageRecieved() {
		if (iaddr != null && message != null) {
			loginRecieved(iaddr, message);
			iaddr = null;
			message = null;
		}
	}

	/**
	 * A login has been recieved
	 * 
	 * @param iaddr
	 *            InetAddress
	 * @param message
	 *            The message to be sent
	 * @param comm
	 *            What communication object to use
	 */
	public void loginRecieved(InetAddress iaddr, String message) {
		String[] persNrPass = message.split(" ");
		try {
			// If you send empty password server will crash without Catch
			String recievedPassword = persNrPass[1];

			String realPassword = fileMan.getPassword("inlogg.txt",
					persNrPass[0]);

			// Checks with the inlogg file to see if sent password is correct
			if (recievedPassword.equals(realPassword)) {
				// Now true is to be sent back
				System.out.println("True is sent back due to RIGHT password");
				send(iaddr, CLIENT_PORT, true); // Sends back to port
			} else {
				System.out.println("False is sent back due to WRONG password");
				send(iaddr, CLIENT_PORT, false);
			}
		} catch (Exception e) {
			System.out.println("Did not recieve password");
			System.out
					.println("False is sent back due to INVALID messsage recieved");
			send(iaddr, CLIENT_PORT, false);
		}
	}

	/**
	 * Send message to specific ip address and port with the message "message".
	 * 
	 * @param ipAddress
	 *            ipaddress
	 * @param port
	 *            port nr
	 * @param message
	 *            the message to be sent
	 */
	public void send(InetAddress ipAddress, int port, String message) {
		try {
			Socket sendSoc = new Socket(ipAddress, port);
			DataOutputStream out = new DataOutputStream(sendSoc
					.getOutputStream());
			out.writeBytes(message);
			sendSoc.close();

		} catch (IOException e) {
			System.out.println("Client is not recieving, kill it with fire!");
			e.printStackTrace();
		}
	}

	/**
	 * Send message to specific ip address and port with the message "message".
	 * 
	 * @param ipAddress
	 *            ipaddress
	 * @param port
	 *            port nr
	 * @param bool
	 *            message the message to be sent
	 */
	public void send(InetAddress ipAddress, int port, Boolean boolMessage) {
		try {
			Socket sendSoc = new Socket(ipAddress, port);
			DataOutputStream out = new DataOutputStream(sendSoc
					.getOutputStream());
			out.writeBoolean(boolMessage);
			sendSoc.close();

		} catch (IOException e) {
			System.out
					.println("Client not active, did you close clients recieveing part?");
			e.printStackTrace();
		}
	}

	/**
	 * Initialise recieve part
	 */
	private void recieveInit() {
		if (!recieveInited) {
			recieveInited = true;
			CommRecieve recComm = new CommRecieve(server);
			recComm.addObserver(this);
			Thread recieve = new Thread(recComm);
			recieve.start();
		}
	}

}
