import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.UUID;

public class Server {

	public enum Command {
		WHOELSE, WHOLASTHR, BROADCAST, MESSAGE, BLOCK, UNBLOCK, LOGOUT;
	}

	public static final int BLOCK_TIME = 60; // Seconds
	public static final int LAST_HOUR = 60 * 60; // Seconds
	public static final int TIME_OUT = 60 * 30; // Seconds

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
			Utilities.error(e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			Utilities.error(e.getMessage());
		}

		// Start server listener
		onlineThreads = new HashMap<String, ServerThread>();
		try {
			ServerSocket listener = new ServerSocket(portNumber);

			// Handle control+C signal
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					// Send EXIT to clients
					Iterator<ServerThread> iter = onlineThreads.values().iterator();
					while (iter.hasNext()) {
						ServerThread serverThread = iter.next();
						serverThread.print("Server is shutting down" + Utilities.EXIT);
					}
				}
			});

			try {
				// Create a thread that checks for time out
				Thread timeoutCheckerThread = new Thread() {
					public void run() {
						while (true) {
							// Check for inactive users
							Iterator<User> iter = userDatabase.values()
									.iterator();
							while (iter.hasNext()) {
								User user = iter.next();
								if (user.isOnline() && user.getLastActive() > 0) {
									// Check the last time this user was active
									int timeSinceLastActive = (int) ((System
											.currentTimeMillis() - user
											.getLastActive()) / 1000);
									if (timeSinceLastActive > TIME_OUT) {
										// Kick user off
										ServerThread serverThread = onlineThreads
												.get(user.getThreadId());
										serverThread
												.print("Sorry! You have been inactive for too long"
														+ Utilities.EXIT);
									}
								}
							}

							// Timeout for a second
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								Utilities.error(e.getMessage());
							}
						}
					}
				};
				timeoutCheckerThread.start();

				// Listen for clients accepting
				while (true) {
					String threadId = UUID.randomUUID().toString();
					ServerThread thread = new ServerThread(threadId,
							listener.accept(), this);
					onlineThreads.put(threadId, thread);
					thread.start();
				}
			} finally {
				listener.close();
			}
		} catch (IOException e) {
			Utilities.error(e.getMessage());
		}
	}

	/**
	 * Removes a closed connection {@link ServerThread} from {@link HashMap} of
	 * server threads.
	 */
	public void removeServerThread(String threadId) {
		String ipAddress = onlineThreads.get(threadId).getIpAddress();
		onlineThreads.remove(threadId);

		try {
			User user = getUserFromThreadId(threadId);
			user.setOffline();
		} catch (NullPointerException e) {
			// Wasn't connected to a user. Don't worry about it.
		}

		System.out.println("connection closed for thread #" + threadId + " at "
				+ ipAddress);
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
		// Set default error message
		String defaultErrorMessage = "Sorry! I have no idea what you're trying to say. Please try again.\n\n";

		// Get current user
		User currentUser = getUserFromThreadId(thread.getThreadId());

		// Update last active for current user
		currentUser.updateLastActive();

		// Get command
		String[] clientInputArray = clientInput.split(" ");
		try {
			Command command = Command
					.valueOf(clientInputArray[0].toUpperCase());

			String str = "";
			switch (command) {

			case WHOELSE: // Displays name of other connected users
				Iterator<User> iter = userDatabase.values().iterator();
				while (iter.hasNext()) {
					User user = iter.next();
					if (user.isOnline()
							&& !user.getUsername().equals(
									currentUser.getUsername())) {
						str += user.getUsername() + "\n";
					}
				}

				if (str.length() == 0) {
					str = "Nobody else is here. :(\n";
				}

				return str + "\n";

			case WHOLASTHR: // Displays name of only those users that connected
							// within the last hour
				iter = userDatabase.values().iterator();
				while (iter.hasNext()) {
					User user = iter.next();
					int timePassedSinceLogin = (int) ((System
							.currentTimeMillis() - user.getLastLoggedIn()) / 1000);
					int timeSinceLastActive = (int) ((System
							.currentTimeMillis() - user.getLastActive()) / 1000);
					if ((user.isOnline() || timePassedSinceLogin < LAST_HOUR || timeSinceLastActive < LAST_HOUR)
							&& !user.getUsername().equals(
									currentUser.getUsername())) {
						str += user.getUsername() + "\n";
					}
				}

				if (str.length() == 0) {
					str = "Nobody was here within the last " + LAST_HOUR
							+ " seconds. :(\n";
				}

				return str + "\n";

			case BROADCAST: // Broadcasts <message> to all connected users
				if (clientInputArray.length < 2) {
					// Not the right arguments
					str = "usage: broadcast <message>\nPlease try again.\n";
				} else {
					// Create message
					String messageStr = clientInputArray[1];
					for (int i = 2; i < clientInputArray.length; i++) {
						messageStr += " " + clientInputArray[i];
					}

					// Find all online users
					LinkedList<User> onlineUsers = new LinkedList<User>();
					iter = userDatabase.values().iterator();
					while (iter.hasNext()) {
						User user = iter.next();
						if (user.isOnline()
								&& !user.getUsername().equals(
										currentUser.getUsername())) {
							onlineUsers.add(user);
						}
					}
					Message message = new Message(messageStr, currentUser,
							onlineUsers.toArray(new User[0]));
					sendMessage(message);
				}

				return str + "\n";

			case MESSAGE:
				if (clientInputArray.length < 3) {
					// Not the right arguments
					str = "usage: message <user> <message>\nPlease try again.\n";
				} else {
					// Check if user exists
					User toUser = userDatabase.get(clientInputArray[1]);
					if (toUser == null) {
						str = clientInputArray[1]
								+ " is not a valid user.\nPlease try again.\n";
					} else {
						// Send the message
						String messageStr = clientInputArray[2];
						for (int i = 3; i < clientInputArray.length; i++) {
							messageStr += " " + clientInputArray[i];
						}

						Message message = new Message(messageStr, currentUser,
								new User[] { toUser });
						sendMessage(message);
					}
				}
				return str + "\n";

			case BLOCK:
				if (clientInputArray.length < 2) {
					// Not the right arguments
					str = "usage: block <user>";
				} else {
					// Check if the user exists
					User user = userDatabase.get(clientInputArray[1]);
					if (user == null) {
						str = clientInputArray[1]
								+ " is not a valid user.\nPlease try again.\n";
					} else if (user.getUsername().equals(
							currentUser.getUsername())) {
						// Check if the user is not yourself
						str = "You cannot block yourself, silly!\n";
					} else {
						// Block user
						currentUser.blockUser(user);
						str = "You have successfully blocked "
								+ clientInputArray[1]
								+ " from sending you messages.\n";
					}
				}

				return str + "\n";

			case UNBLOCK:
				if (clientInputArray.length < 2) {
					// Not the right arguments
					str = "usage: unblock <user>";
				} else {
					// Check if the user exists
					User user = userDatabase.get(clientInputArray[1]);
					if (user == null) {
						str = clientInputArray[1]
								+ " is not a valid user.\nPlease try again.\n";
					} else if (user.getUsername().equals(
							currentUser.getUsername())) {
						// Check if the user is not yourself
						str = "Error! You cannot block yourself!\n";
					} else {
						// Unblock user
						boolean unblocked = currentUser.unblockUser(user);
						if (unblocked) {
							str = "You have successfully unblocked "
									+ clientInputArray[1] + ".\n";
						} else {
							str = "You didn't block " + clientInputArray[1]
									+ " to begin with!\n";
						}
					}
				}

				return str + "\n";

			case LOGOUT:
				return "Goodbye" + Utilities.EXIT;

			default:
				return defaultErrorMessage;
			}

		} catch (Exception e) {
			// Could not understand client input
			return defaultErrorMessage;
		}
	}

	/**
	 * Sends a {@link Message}.
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {
		User[] toUsers = message.getToUsers();
		for (User toUser : toUsers) {
			// Check if toUser has blocked fromUser
			if (toUser.hasBlocked(message.getFromUser())) {
				// Alert the fromUser that message could not be sent
				ServerThread thread = onlineThreads.get(message.getFromUser()
						.getThreadId());
				thread.print("You cannot send any messages to "
						+ toUser.getUsername()
						+ ". You have been blocked by the user.\n");
				continue;
			}

			if (toUser.isOnline()) {
				// User is online, so send the message immediately
				ServerThread thread = onlineThreads.get(toUser.getThreadId());
				thread.print(message.getFromUser().getUsername() + " says: "
						+ message.getMessage() + "\n\n");
			} else {
				// Save message for offline messages
				toUser.addMessage(message);
			}
		}
	}

	/**
	 * Retrives offline messages for a given username.
	 * 
	 * @param username
	 * @return all offline messages formatted in a {@link String}
	 */
	public String getOfflineMessages(String username) {
		User user = userDatabase.get(username);

		String str = "";
		Message message = user.popMessage();
		while (message != null) {
			str += message.getFromUser().getUsername() + " said: "
					+ message.getMessage() + "\n";
			message = user.popMessage();
		}

		return str.length() == 0 ? "No offline messages!\n\n" : str + "\n";
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

	public static void main(String[] args) {
		if (args.length < 1) {
			Utilities.error("usage: java Server <server_port_no>");
		}

		// Get port number
		int portNumber = Utilities.parsePortNumber(args[0]);

		// Create server
		new Server(portNumber);
	}
}