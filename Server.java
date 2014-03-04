import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

public class Server {
	public static final String NEWLINE = "<br>";
	public static final String EXIT = "<exit>";
	public static final String PROMPT = "Command: ";

	public static final int BLOCK_TIME = 60; // Seconds

	private static final String USER_DATABASE_PATH = "user_pass.txt";

	/**
	 * @key username
	 * @value <code>User</code>
	 */
	private HashMap<String, User> userDatabase;

	/**
	 * @key threadId
	 * @value <code>ServerThread</code>
	 */
	private HashMap<String, ServerThread> onlineThreads;

	public Server(int portNumber) {
		// Retrieve and populate user database
		userDatabase = new HashMap<String, User>();
		try {
			Scanner userDatabaseScanner = new Scanner(new File(
					USER_DATABASE_PATH));
			while (userDatabaseScanner.hasNextLine()) {
				String[] userPassword = userDatabaseScanner.nextLine().split(
						" ");
				userDatabase.put(userPassword[0], new User(userPassword[0],
						userPassword[1]));
			}
		} catch (FileNotFoundException e) {
			error(e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			error(e.getMessage());
		}

		// Start server listener
		onlineThreads = new HashMap<String, ServerThread>();
		try {
			ServerSocket listener = new ServerSocket(portNumber);
			try {
				while (true) {
					String threadId = UUID.randomUUID().toString();
					ServerThread thread = new ServerThread(threadId,
							listener.accept(), this);
					onlineThreads.put(threadId, thread);
					thread.start();
				}
			} finally {
				listener.close();
				
				// Close online threads
				for (ServerThread thread : onlineThreads.values()) {
					thread.print("Server has crashed!" + EXIT);
				}
			}
		} catch (IOException e) {
			error(e.getMessage());
		}
	}

	/**
	 * Removes a closed connection {@link ServerThread} from {@link HashMap} of
	 * server threads.
	 */
	public void removeServerThread(String threadId) {
		onlineThreads.remove(threadId);
		User user = getUserFromThreadId(threadId);
		user.setOffline();
	}

	/**
	 * Processes what the client has sent to the <code>Server</code> and returns
	 * a {@link String} with the appropriate response.
	 * 
	 * @param clientInput
	 *            {@link String} from the client
	 * @param thread
	 *            {@link SeverThread} from which the client is connecting to
	 * @return response from the <code>Server</code>
	 */
	public String processClientInput(String clientInput, ServerThread thread) {
		String errorMessage = "Sorry! I did not understand what you were trying to say. Please try again."
				+ NEWLINE + NEWLINE + PROMPT;

		try {
			// Get user of thread that is asking
			User currentUser = getUserFromThreadId(thread.getThreadId());

			// Get command
			Command command = Command.valueOf(clientInput.split(" ")[0]
					.toUpperCase());

			String str = "";
			switch (command) {
			case WHOELSE: // Displays name of other connected users
				// Iterate through user database
				Iterator<User> iter = userDatabase.values().iterator();
				while (iter.hasNext()) {
					User user = iter.next();
					if (user.isOnline() && !user.equals(currentUser)) {
						str += user.getUsername() + NEWLINE;
					}
				}

				if (str.length() == 0) {
					// No other users online
					str = "Nobody else is here!" + NEWLINE;
				}

				return str + NEWLINE + PROMPT;
			case WHOLASTHR:
				// TODO
				break;
			case BROADCAST:
				// TODO
				break;
			case MESSAGE:
				try {
					String[] clientInputArr = clientInput.split(" ");

					// Get user to send the message to
					User toUser = userDatabase.get(clientInputArr[1]);
					if (toUser == null) {
						// User does not exist
						str = "Sorry! " + clientInputArr[1]
								+ " is not a valid user. Please try again!";
					} else {
						// Get the message
						if (clientInputArr.length < 3) {
							// There aren't any messages
							str = "Please write a message to send to "
									+ clientInput + ".";
						} else {
							// Send message
							String messageStr = clientInputArr[2];
							for (int i = 3; i < clientInputArr.length; i++) {
								messageStr += " " + clientInputArr[i];
							}
							Message message = new Message(messageStr,
									currentUser, new User[] { toUser });
							sendMessage(message);
						}
					}

				} catch (IndexOutOfBoundsException e) {
					// Message protocol not followed
					str = "If you would like to send a message to a peer, please format your message:"
							+ NEWLINE + "message <user> <message>";
				}

				return str + NEWLINE + PROMPT;
			case BLOCK:
				// TODO
				break;
			case UNBLOCK:
				// TODO
				break;
			case LOGOUT:
				return "Goodbye!" + NEWLINE + EXIT;
			default:
				return errorMessage;
			}
		} catch (Exception e) {
			// Did not understand command
			return errorMessage;
		}

		return errorMessage;
	}
	
	/**
	 * Sends a message.
	 * 
	 * @param message
	 */
	private void sendMessage(Message message) {
		User[] toUsers = message.getToUsers();
		for (User toUser : toUsers) {
			toUser.addMessage(message);
			
			if (toUser.isOnline()) {
				ServerThread thread = onlineThreads.get(toUser.getThreadId());
				thread.print(toUser.popMessage().getMessage());
			}
		}
	}

	/**
	 * Checks if a username and password combination exists in the {@link User}
	 * database. Also associates the {@link User} with the {@link ServerThread}.
	 * 
	 * @param username
	 * @param password
	 * @param thread
	 *            {@link SeverThread} from which the client is connecting to
	 * @return true if the username-password combination exists and false
	 *         otherwise
	 * @throws UserAlreadyLoggedInException
	 * @throws User.IpAddressBlockedException
	 */
	public boolean authenticateUser(String username, String password,
			ServerThread thread) throws User.UserAlreadyLoggedInException,
			User.IpAddressBlockedException {
		if (!userDatabase.containsKey(username)) {
			// Username does not exist
			return false;
		}

		// Get user
		User user = userDatabase.get(username);

		if (!user.matchUsernamePassword(username, password,
				thread.getIpAddress())) {
			// Password is incorrect
			return false;
		}

		// Associate User with ServerThread
		user.setOnline(thread.getThreadId());
		return true;
	}

	/**
	 * Retrieves a {@link User} object from a thread id.
	 * 
	 * @param threadId
	 *            id of the thread that the user has logged in
	 * @return {@link User} connected to the thread
	 */
	private User getUserFromThreadId(String threadId) {
		Iterator<User> iter = userDatabase.values().iterator();
		while (iter.hasNext()) {
			User user = iter.next();
			if (threadId.equals(user.getThreadId())) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Parses a port number in the form of a {@link String} into an integer.
	 * 
	 * @param portNumberStr
	 *            string to parse into a port number
	 * @return port number as an integer
	 */
	public static int parsePortNumber(String portNumberStr) {
		int portNumber = -1;
		try {
			portNumber = Integer.parseInt(portNumberStr);
		} catch (NumberFormatException e) {
			error("port number must be an integer");
		}
		if (portNumber < 1 || portNumber > 65535) {
			error("port number must a positive integer between 1 and 65535");
		}
		return portNumber;
	}

	/**
	 * Prints the error message and then exits the program.
	 * 
	 * @param message
	 *            error message
	 */
	public static void error(String message) {
		System.err.println(message);
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			error("usage: java Server <server_port_no>");
		}

		// Get port number
		int portNumber = parsePortNumber(args[0]);

		// Create server
		new Server(portNumber);
	}
}