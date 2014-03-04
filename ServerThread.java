import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
	private String threadId;
	private Socket socket;
	private Server server;

	private String ipAddress;

	private BufferedReader in;
	private PrintWriter out;

	public ServerThread(String threadId, Socket socket, Server server) {
		this.threadId = threadId;
		this.socket = socket;
		this.server = server;
		this.ipAddress = socket.getInetAddress().getHostAddress();

		System.out.println("connection opened for thread #" + threadId + " at "
				+ ipAddress);
	}

	public void run() {
		try {
			// Set up input and output from socket
			this.in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);

			// User authentication
			boolean authenticated = false;
			do {
				out.println("Username: ");
				String username = in.readLine();
				out.println("Password: ");
				String password = in.readLine();

				try {
					authenticated = server.authenticateUser(username, password,
							this);

					if (authenticated) {
						// User authenticated!
						out.println(Server.NEWLINE
								+ "Welcome to simple chat server!"
								+ Server.NEWLINE + Server.NEWLINE
								+ Server.PROMPT);
						authenticated = true;
					} else {
						// User not authenticated
						out.print("Sorry! Incorrect username and password combination.");
					}

				} catch (User.UserAlreadyLoggedInException e) {
					out.print(username + " is already logged in!");
				} catch (User.IpAddressBlockedException e) {
					out.println("Sorry! " + username
							+ " has been blocked from ip address " + ipAddress
							+ "." + Server.NEWLINE + "Please wait " + e.getSecondsLeft() + " seconds before attempting to login again." + Server.EXIT);
					break;
				}

				if (!authenticated) {
					out.print(" Please try again." + Server.NEWLINE
							+ Server.NEWLINE);
				}

			} while (!authenticated);

			// Communicate with client
			String fromClient;
			while ((fromClient = in.readLine()) != null) {
				// Replace all NEWLINE with newline character
				fromClient = fromClient.replaceAll(Server.NEWLINE, "\n");

				// Interpret client data and come up with correct response
				String toClient = server.processClientInput(fromClient, this);
				out.println(toClient);
			}

		} catch (IOException e) {
			Server.error(e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				Server.error(e.getMessage());
			}
			System.out.println("connection closed for thread #" + threadId
					+ " at " + ipAddress);
			server.removeServerThread(threadId);
		}
	}

	/**
	 * @return id of this <code>ServerThread</code>
	 */
	public String getThreadId() {
		return this.threadId;
	}

	/**
	 * @return ip address of the connected client
	 */
	public String getIpAddress() {
		return this.ipAddress;
	}
	
	/**
	 * Prints a message to <code>out</code>.
	 * 
	 * @param message
	 */
	public void print(String message) {
		out.println(message);
	}
}