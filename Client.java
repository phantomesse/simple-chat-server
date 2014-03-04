import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client {
	private static final int COUNTDOWN = 3;
	private static final int GUI_WIDTH = 600;
	private static final int GUI_HEIGHT = 400;
	private static final Color GUI_BG_COLOR = new Color(30, 30, 30);
	private static final Color GUI_FG_COLOR = new Color(255, 192, 215);
	private static final Color GUI_FG_INPUT_COLOR = new Color(255, 138, 181);
	private static final Font GUI_FONT = new Font("Consolas", Font.BOLD, 12);

	private static final String EMOTICON_FOLDER_PATH = "emoticons/";
	private static final String[] EMOTICONS = { ":D", "XD", ":)", ";)", "-.-",
			">.<", "o.o", ":(" };

	private JTextPane outputBox;
	private StyledDocument outputBoxDoc;
	private JTextField inputBox;
	private JLabel prompt;
	private boolean printing = false;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	public Client(String ipAddress, int portNumber) {
		try {
			socket = new Socket(ipAddress, portNumber);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			// Set up the GUI
			setupGUI();

			// Communicate with server
			String fromServer;
			while ((fromServer = in.readLine()) != null) {
				// Decode message from server
				String[] fromServerArr = Utilities.decodeMessage(fromServer);
				fromServer = fromServerArr[1];

				// Set prompt
				prompt.setText(fromServerArr[0] + ":");

				// Exit if signaled by server
				if (fromServer.endsWith(Utilities.EXIT)) {

					// Print server output
					print(fromServer.substring(0,
							fromServer.indexOf(Utilities.EXIT)), null);

					// Disable text box (basically exiting without closing
					// window)
					inputBox.setEditable(false);
					inputBox.setBackground(GUI_BG_COLOR);
					inputBox.setCaretColor(GUI_BG_COLOR);
					prompt.setText("");
					prompt.getParent().setBackground(GUI_BG_COLOR);

					// Start exiting
					for (int i = 0; i < COUNTDOWN; i++) {
						print(".", null);
						try {
							Thread.sleep(600);
						} catch (InterruptedException e) {
							Utilities.error(e.getMessage());
						}
					}
					System.exit(0);
				}

				// Print server output
				print(fromServer, null);
			}

		} catch (UnknownHostException e) {
			Utilities.error(e.getMessage());
		} catch (IOException e) {
			Utilities.error(e.getMessage());
		}
	}

	/**
	 * Sets up the javax.swing GUI.
	 */
	private void setupGUI() {
		// Set up the JFrame
		JFrame frame = new JFrame("Simple Chat Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
		frame.setLocationRelativeTo(null);

		// Set the layout
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
		frame.pack();

		// Create output box
		outputBox = new JTextPane();
		outputBox.setBackground(GUI_BG_COLOR);
		outputBox.setForeground(GUI_FG_COLOR);
		outputBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		outputBox.setEditable(false);
		outputBox.setFont(GUI_FONT);
		outputBoxDoc = outputBox.getStyledDocument();
		JScrollPane scrollPane = new JScrollPane(outputBox);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Create prompt field
		prompt = new JLabel();
		prompt.setForeground(GUI_FG_COLOR);
		prompt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
		prompt.setFont(GUI_FONT);

		// Create input box
		inputBox = new JTextField();
		inputBox.setBackground(GUI_BG_COLOR.darker());
		inputBox.setForeground(GUI_FG_COLOR);
		inputBox.setCaretColor(GUI_FG_COLOR);
		inputBox.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
		inputBox.setFont(GUI_FONT);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(GUI_BG_COLOR.darker());
		panel.add(prompt, BorderLayout.WEST);
		panel.add(inputBox, BorderLayout.CENTER);
		contentPane.add(panel, BorderLayout.SOUTH);

		// Add action listener for input box to send to server
		inputBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = inputBox.getText();

				if (text.equals("")) {
					// Nothing was entered
					return;
				}

				inputBox.setText("");
				out.println(text);

				// Style input text
				SimpleAttributeSet inputStyle = new SimpleAttributeSet();
				StyleConstants.setForeground(inputStyle, GUI_FG_INPUT_COLOR);

				// Append input text to output box
				print(prompt.getText() + " ", null);
				print(text + "\n", inputStyle);
			}
		});

		// Set focus on input box
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				// Set focus on input box
				inputBox.requestFocus();
			}
		});

		// Display the window.
		frame.setVisible(true);
	}

	/**
	 * Prints a message in a defined style to the output box.
	 * 
	 * @param message
	 * @param style
	 */
	private void print(String message, SimpleAttributeSet style) {

		// Wait until printing to output box is done before proceeding
		while (printing) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Utilities.error(e.getMessage());
			}
		}

		// Print
		printing = true;
		try {
			// Check if there is an emoticon
			int[] emoticon = findEmoticon(message);
			if (emoticon == null) {
				// No emoticons, so print normally
				outputBoxDoc.insertString(outputBoxDoc.getLength(), message,
						style);
			} else {
				// There are emoticons

				String temp = message;
				
				Style emoticonStyle = ((StyledDocument) outputBox
						.getDocument()).addStyle("emoticonStyle", null);

				while (emoticon != null) {
					// Print up to emoticon
					outputBoxDoc.insertString(outputBoxDoc.getLength(),
							temp.substring(0, emoticon[0]), style);
					
					// Print emoticon
					StyleConstants.setIcon(emoticonStyle, new ImageIcon(
							EMOTICON_FOLDER_PATH + emoticon[1] + ".png"));
					outputBoxDoc.insertString(outputBoxDoc.getLength(),
							EMOTICONS[emoticon[1]], emoticonStyle);
					
					// Set temp to the rest of the message
					temp = temp.substring(emoticon[0] + EMOTICONS[emoticon[1]].length());
					
					// Check again for emoticons
					emoticon = findEmoticon(temp);
				}
				
				// No emoticons, so print normally
				outputBoxDoc.insertString(outputBoxDoc.getLength(), temp,
						style);
			}
		} catch (BadLocationException e) {
			Utilities.error(e.getMessage());
		}

		// Scroll to bottom
		outputBox.setCaretPosition(outputBox.getDocument().getLength());

		printing = false;
	}

	/**
	 * Finds the first instance of an emoticon in a string
	 * 
	 * @param str
	 *            string to find emoticon in
	 * @return an integer array { index of emoticon in string, index of emoticon
	 *         in <code>EMOTICONS</code> array } or null if there are no
	 *         emoticons
	 */
	private int[] findEmoticon(String str) {
		int firstIndex = -1;
		int emoticon = -1;
		for (int i = 0; i < EMOTICONS.length; i++) {
			int index = str.indexOf(EMOTICONS[i]);

			if (index >= 0) {
				// Found an emoticon
				if (firstIndex == -1 || index < firstIndex) {
					firstIndex = index;
					emoticon = i;
				}
			}
		}

		if (emoticon == -1) {
			return null;
		} else {
			return new int[] { firstIndex, emoticon };
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			Utilities
					.error("usage: java Client <server_IP_address> <server_port_no>");
		}

		// Get IP address
		String ipAddress = args[0];

		// Get port number
		int portNumber = Utilities.parsePortNumber(args[1]);

		new Client(ipAddress, portNumber);
	}
}
