package finall;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The server that run by the GUI
 * clients can connect to and talk to
 * @author oravi
 *
 */

public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;

/**
 * Server constructor that receive the port to listen to for connection as parameter
 * @param port
 * @param sg
 */
	public Server(int port, ServerGUI sg) {
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
	}

	/**
	 * Start the all thing of the server and stay
	 * in loop to listen to some more clients
	 */
	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while(keepGoing) 
			{
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
	/**
	 * 
	 * For the GUI to stop the server
	 */
	public void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/**
	 * Display an event (not a message) to the GUI
	 * @param msg
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		sg.appendEvent(time + "\n");
	}

	/**
	 * to broadcast a message to all Clients
	 * @param message
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// display message on GUI

		sg.appendRoom(messageLf);     // append in the room window

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				broadcast("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	/**
	 * Send messages to specific client as private message
	 * @param message
	 * @param name
	 * @param out
	 */
	private void byName(String message, String name,ObjectOutputStream out) {
		String time = sdf.format(new Date());
		String messageLf = "PrivateMsg: " + time + " " + message + "\n"; // The message
		boolean find = false;
		for(int i = al.size(); --i >= 0;) { // Search for the client
			ClientThread ct = al.get(i);
			if(ct.username.equals(name)) {
				// try to write to the Client 
				ct.writeMsg(messageLf);
				find = true;
			}
		}
		try {
			if(find) {
				out.writeObject(messageLf); // If find Send to the origin the original message as well
			}
			else {
				out.writeObject("The name: '"+ name +"'  Not found in the server! \n"); // If not find send to origin that it failed
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * for a client who logoff using the LOGOUT message
	 * @param id
	 */
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	/**
	 * For new Users check if there is a duplicate name to avoid problem (as private messages)
	 * @param username
	 * @return
	 */
	private boolean checkDuplicate(String username) {
		for(ClientThread ct : al) {
			if(ct.username.equals(username)) {
				return true;
			}
		}
		return false;
	} 

	/**
	 * Instance of thread that run for each client and holding the listen to the client
	 * @author oravi
	 *
	 */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for desconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			try
			{
				// Creating both Data Stream
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				if(checkDuplicate(username)) {
					writeMsg("ErrorDuplicateName");
					close();
				}
				else broadcast(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}
/**
 * run for the client and listen to new message from the client
 */
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					if(cm.getbyName()) {
						byName(username + ": "+ message,cm.getName(),sOutput);
					}
					else 
						broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// scan all the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + "''"+ ct.username+ "'' " +"connected"+ " since " + ct.date);
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			broadcast(username + " disconnected.");
			close();
		}

		/**
		 *  try to close everything - all the scokets and streams that open
		 */
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
				if(sInput != null) sInput.close();
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/**
		 * Write a the message as string to the Client output stream
		 * @param msg
		 * @return 
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}

