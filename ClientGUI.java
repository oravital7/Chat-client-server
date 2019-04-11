package finall;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;


/**
 * The GUI of the client
 * @author oravi
 *
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// to hold the Username and later on the messages
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort;
	// to Logout and get the list of the users 
	private JButton login, logout, whoIsIn, Send;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client object 
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;
	private JRadioButton rdName;
	private JTextField txtName;
	private JTextField textFieldName;
	private JLabel lblConnected;

	/**
	 * Constructor connection receiving a socket number
	 * @param host
	 * @param port
	 */
	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port; 
		defaultHost = host;

		// The NorthPanel with:
		JPanel northPanel = new JPanel();
		// the server name and the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		getContentPane().add(northPanel, BorderLayout.NORTH);
		GroupLayout gl_northPanel = new GroupLayout(northPanel);
		gl_northPanel.setHorizontalGroup(
				gl_northPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(serverAndPort, GroupLayout.PREFERRED_SIZE, 584, GroupLayout.PREFERRED_SIZE)
				);
		gl_northPanel.setVerticalGroup(
				gl_northPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(serverAndPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				);
		northPanel.setLayout(gl_northPanel);

		// The CenterPanel which is the chat room
		ta = new JTextArea("", 80, 80);
		ta.setEditable(false);
		JPanel centerPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(ta);
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		tf = new JTextField("Write your message here");
		tf.setEnabled(false);
		tf.setEditable(false);
		tf.setBackground(Color.WHITE);
		tf.requestFocus();

		textFieldName = new JTextField();
		textFieldName.setText("Guest");
		textFieldName.setColumns(10);

		JLabel lblYourName = new JLabel("Your Name:");

		Send = new JButton("Send");
		Send.setEnabled(false);
		Send.addActionListener(this);
		
		lblConnected = new JLabel();
		lblConnected.setFont(new Font("Tahoma", Font.PLAIN, 14));

		GroupLayout gl_centerPanel = new GroupLayout(centerPanel);
		gl_centerPanel.setHorizontalGroup(
			gl_centerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_centerPanel.createSequentialGroup()
					.addComponent(lblYourName)
					.addGap(50)
					.addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 169, Short.MAX_VALUE)
					.addComponent(lblConnected, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
					.addGap(27))
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
				.addGroup(gl_centerPanel.createSequentialGroup()
					.addComponent(tf, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(Send, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
					.addGap(38))
		);
		gl_centerPanel.setVerticalGroup(
			gl_centerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_centerPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_centerPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblYourName)
						.addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblConnected, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_centerPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tf, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(Send, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		centerPanel.setLayout(gl_centerPanel);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		// you have to login before being able to logout
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

		JPanel southPanel = new JPanel();

		//  RadioButton to private message option
		rdName = new JRadioButton("Send by name");
		rdName.addActionListener(this);
		rdName.setEnabled(false);

		southPanel.add(rdName);

		// The text filled for the specific name
		txtName = new JTextField();
		txtName.setEnabled(false);

		txtName.setToolTipText("Enter name");
		southPanel.add(txtName);
		txtName.setColumns(10);
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(555, 600);
		setVisible(true);

	}

	/**
	 * called by the Client class to append text in the TextArea 
	 * @param str
	 */
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	/**
	 * called by the GUI is the connection failed
	 * we reset our buttons, label, textfield
	 */
	void connectionFailed() {
		// reset Buttons
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		tf.setText("Write your message here");
		tf.setEnabled(false);
		Send.setEnabled(false);
		rdName.setSelected(false);
		rdName.setEnabled(false);
		txtName.setText("");
		txtName.setEnabled(false);
		lblConnected.setText("");
		
		// reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		textFieldName.setEditable(true);
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}

	/**
	 * Button or JTextField clicked
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}
		// if it the who is in button
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			return;
		}
		// if it's the radio button for the private message
		if(o == rdName) {
			if(rdName.isSelected()) {
				txtName.setEnabled(true);
				txtName.setText("Enter name");
			}
			else {
				txtName.setEnabled(false);
				txtName.setText("");
			}
			return;
		}

		// ok it is coming from the JTextField
		if(connected || o == Send) {
			// just have to send the message
			if(rdName.isSelected()) {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText(), txtName.getText()));				
				tf.setText("");
				return;
			}
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}


		if(o == login) {
			// ok it is a connection request
			String username = textFieldName.getText();
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port number, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client
			if(!client.start()) 
				return;
			ta.append("Welcome to the Chat room\r\n");
			tf.setText("");
			tf.setEnabled(true);
			tf.setEditable(true);
			textFieldName.setEditable(false);
			lblConnected.setText("Connected!");
			connected = true;
			rdName.setEnabled(true);
			Send.setEnabled(true);
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}

	}
	/**
	 * If there is a duplicate usernames in the server
	 * show error message
	 * @param infoMessage
	 * @param titleBar
	 */
	public void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "Error: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}


	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}
}
