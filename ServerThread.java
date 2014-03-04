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
			String str = "";
			boolean authenticated = false;
			do {
				authenticated = true;
				
				String username = null, password = null;
				while (username == null || password == null) {
					out.println(Utilities.encodeMessage("Username", str));
					username = in.readLine();
					out.println(Utilities.encodeMessage("Password", ""));
					password = in.readLine();
				}

				try {
					authenticated = server.authenticateUser(username, password,
							this);

					if (authenticated) {
						// User authenticated!
						out.println(Utilities.encodeMessage("Command",
								"\nWelcome to simple chat server!\n\n"));
					} else {
						// User not authenticated
						str = "Sorry! Incorrect username and password combination.";
						authenticated = false;
					}

				} catch (User.UserAlreadyLoggedInException e) {
					str = username + " is already logged in!";
					authenticated = false;
				} catch (User.IpAddressBlockedException e) {
					str = "Sorry! " + username
							+ " has been blocked from ip address " + ipAddress
							+ ".\nPlease wait " + e.getSecondsLeft()
							+ " seconds before attempting to login again."
							+ Utilities.EXIT;
					out.println(Utilities.encodeMessage("", str));
					break;
				}

				if (!authenticated) {
					str += " Please try again!\n\n";
				}

			} while (!authenticated);

			// Communicate with client
			String fromClient;
			while ((fromClient = in.readLine()) != null) {				
				// Interpret client data and come up with correct response
				String toClient = server.processClientInput(fromClient, this);
				out.println(Utilities.encodeMessage("Command", toClient));
			}

		} catch (IOException e) {
			Utilities.error(e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				Utilities.error(e.getMessage());
			}
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