import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	public static final String NEWLINE = "<br>";
	public static final String EXIT = "<exit>";

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Scanner stdIn;

	public Client(String ipAddress, int portNumber) {
		try {
			socket = new Socket(ipAddress, portNumber);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			stdIn = new Scanner(System.in);

			// Communicate with server
			String fromServer;
			while ((fromServer = in.readLine()) != null) {
				// Replace all Server.NEWLINE with newline character
				fromServer = fromServer.replaceAll(NEWLINE, "\n");

				// Exit if signaled by server
				if (fromServer.endsWith(EXIT)) {
					System.out.println(fromServer.substring(0,
							fromServer.indexOf(EXIT)));
					return;
				}

				// Print server output
				System.out.print(fromServer);

				// Send user input to server
				out.println(stdIn.nextLine());
			}

		} catch (UnknownHostException e) {
			error(e.getMessage());
		} catch (IOException e) {
			error(e.getMessage());
		}
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
		if (args.length < 2) {
			error("usage: java Client <server_IP_address> <server_port_no>");
		}

		// Get IP address
		String ipAddress = args[0];

		// Get port number
		int portNumber = parsePortNumber(args[1]);

		new Client(ipAddress, portNumber);
	}
}
