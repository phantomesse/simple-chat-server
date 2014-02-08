import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * <code>ServerThread</code> handles the connection between the server and a
 * client.
 * 
 * @author Lauren Zou
 */
public class ServerThread extends Thread {
	private Server server;
	private Socket socket;
	private String clientIpAddress;
	private BufferedReader in;
	private PrintWriter out;

	public ServerThread(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		clientIpAddress = socket.getInetAddress().getHostAddress();

		System.out.println("connection started for client at " + clientIpAddress);
	}

	public void run() {
		try {
			// Set up input and output from socket
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			// Authenticate user
			boolean authenticated = false;
			int tries = 0;
			do {
				out.println("Username: ");
				String username = in.readLine();
				out.println("Password: ");
				String password = in.readLine();
				if (server.authenticateUsernamePassword(username, password)) {
					// User authenticated!
					out.println(Server.NEWLINE
							+ "Welcome to simple chat server!" + Server.NEWLINE + "Command: ");
					authenticated = true;
				} else {
					// User not authenticated
					out.print("Sorry! Incorrect username and password combination.");
					if (++tries == 3) {
						// Third try
						out.println(Server.EXIT);
						
						// TODO: Set timeout for this client IP address
					} else {
						out.print(" Please try again." + Server.NEWLINE
								+ Server.NEWLINE);
					}
				}
			} while (!authenticated && tries < 3);

			// Communicate with client
			String fromClient;
			while ((fromClient = in.readLine()) != null) {
				// Replace all Protocol.NEWLINE with newline character
				fromClient = fromClient.replaceAll(Server.NEWLINE, "\n");

				// Interpret client data and come up with correct response
				String toClient = server.processClientInput(fromClient);
				out.println(toClient);
			}

		} catch (IOException e) {
			Utilities.error("error handling client at " + clientIpAddress, false);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				Utilities.error("could not close the socket", false);
			}
			System.out.println("connection closed for client at " + clientIpAddress);
		}
	}
}
