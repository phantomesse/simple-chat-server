import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

/**
 * TODO: Write a blurb about the {@link Server}.
 * 
 * @author Lauren Zou
 */
public class Server {
	public static final String EXIT = "<exit>";
	public static final String NEWLINE = "<br>";

	private static final String USERNAME_PASSWORD_FILE_PATH = "user_pass.txt";
	private HashMap<String, String> usernamePasswordDatabase;
	private ArrayList<ServerThread> serverThreads;

	public Server(int portNumber) {
		// Set username-password combination database
		usernamePasswordDatabase = getUsernamePasswordDatabase();

		// Start server listener
		serverThreads = new ArrayList<ServerThread>();
		try {
			ServerSocket listener = new ServerSocket(portNumber);
			try {
				while (true) {
					ServerThread thread = new ServerThread(this, listener.accept());
					serverThreads.add(thread);
					thread.start();
				}
			} finally {
				listener.close();
			}
		} catch (IOException e) {
			Utilities.error(
					"could not start server socket listener at port number "
							+ portNumber, true);
		}
	}

	/**
	 * Interprets the client's input and returns the correct response.
	 * 
	 * @param clientInput
	 *            input from client
	 * @return server's response
	 */
	public String processClientInput(ServerThread serverThread, String clientInput) {
		clientInput = clientInput.toLowerCase();

		String response = "";

		if (clientInput.equals("time")) {
			// Displays the date and time
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEEEE, MMMMM d, yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
			Calendar cal = Calendar.getInstance();
			response = "It is currently " + timeFormat.format(cal.getTime())
					+ " on " + dateFormat.format(cal.getTime()) + ".";
		}

		else if (clientInput.equals("whoelse")) {
			// Displays name of other connected users
			// TODO
		}

		else if (clientInput.equals("wholastr")) {
			// Displays name of only those users that connected within the last
			// hour
			// TODO
		}

		else if (clientInput.equals("logout")) {
			// Logout user
			return "Goodbye!" + EXIT;
		}

		else if (clientInput.startsWith("broadcast")) {
			// Broadcasts <message> to all connected users
			// TODO
		}

		else if (clientInput.startsWith("message")) {
			// message <user> <message> Private <message> to a <user
			// TODO
		}

		else if (clientInput.startsWith("block")) {
			// block <user> Blocks the <user> from sending any messages. If
			// <user> is self, display error.
			// TODO
		}

		else if (clientInput.startsWith("unblock")) {
			// unblock <user> Unblocks the <user> who has been previously
			// blocked. If <user> was not already blocked, display error.
			// TODO
		}

		else if (clientInput.startsWith("hello")
				|| clientInput.startsWith("hi")
				|| clientInput.startsWith("hey")) {
			// Responds with a random greeting
			String[] greetings = { "Hi", "Hello", "Greetings", "Howdy",
					"Hey", "Yo" };
			response = greetings[(int) (Math.random() * greetings.length)] + " " + serverThread.getClientUsername() + "!";
		}

		else {
			// Responds with an error message
			response = "Sorry, I did not understand what you just said.";
		}

		return response + NEWLINE + NEWLINE + "Command: ";
	}

	/**
	 * Checks if username and password combination is in the username-password
	 * combination database.
	 * 
	 * @param username
	 * @param password
	 * @return true if username and password combination exists and false if
	 *         otherwise
	 */
	public boolean authenticateUsernamePassword(String username, String password) {
		return usernamePasswordDatabase.containsKey(username) ? usernamePasswordDatabase
				.get(username).equals(password) : false;
	}

	/**
	 * Reads usernames and passwords from a text file into a {@link HashMap}.
	 * 
	 * @return a {@link HashMap} of all usernames and password combinations with
	 *         the username as the key and the password as the value
	 */
	private static HashMap<String, String> getUsernamePasswordDatabase() {
		HashMap<String, String> usernamePasswordDatabase = new HashMap<String, String>();
		try {
			Scanner userPassFileReader = new Scanner(new File(
					USERNAME_PASSWORD_FILE_PATH));
			while (userPassFileReader.hasNextLine()) {
				String[] userPass = userPassFileReader.nextLine().split(" ");
				usernamePasswordDatabase.put(userPass[0], userPass[1]);
			}
		} catch (FileNotFoundException e) {
			Utilities.error("could not read " + USERNAME_PASSWORD_FILE_PATH,
					true);
		} catch (IndexOutOfBoundsException e) {
			Utilities.error("could not read " + USERNAME_PASSWORD_FILE_PATH,
					true);
		}
		return usernamePasswordDatabase;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			Utilities.error("usage: java Server <server_port_no>", true);
		}

		// Get port number
		int portNumber = Utilities.parsePortNumber(args[0]);

		// Create server
		new Server(portNumber);
	}
}
