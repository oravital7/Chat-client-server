package finall;
import java.net.*;
import java.io.*;

/**
 * The Client Class run by the GUI
 * Connect to the server and talk with
 * @author oravi
 *
 */
public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	// The Gui
	private ClientGUI cg;

	// the server, the port and the username
	private String server, username;
	private int port;


	/**
	 * Constructor call when used from a GUI
	 * @param server
	 * @param port
	 * @param username
	 * @param cg
	 */
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
	}

	/**
	 * 
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}

		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/**
	 * To send a message to the GUI
	 * @param msg
	 */
	private void display(String msg) {
		cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
	}

	/**
	 * To send a message to the server
	 * @param msg
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/**
	 *  When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
			if(sOutput != null) sOutput.close();
			if(socket != null) socket.close();

		}
		catch(Exception e) {} // not much else I can do

		// inform the GUI
		cg.connectionFailed();
	}

	/**
	 * a class that waits for the message from the server and append them to the JTextArea
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					if(msg.equals("ErrorDuplicateName")) {
						cg.infoBox("Username already exists","Unable create client");
					}
					else		cg.append(msg);
				}
				catch(IOException e) {
					display("Server has close the connection: You Log Out \n");
					cg.connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
