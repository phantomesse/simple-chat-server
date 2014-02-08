import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * TODO: Write a blurb about the {@link Client}.
 * 
 * @author Lauren Zou
 */
public class Client {
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
				fromServer = fromServer.replaceAll(Utilities.NEWLINE, "\n");
				
				// Exit if signaled by server
				if (fromServer.endsWith(Utilities.EXIT)) {
					System.out.println(fromServer.substring(0, fromServer.indexOf(Utilities.EXIT)));
					return;
				}
				
				// Print server output
				System.out.print(fromServer);
								
				// Send user input to server
				out.println(stdIn.nextLine());
			}
			
		} catch (UnknownHostException e) {
			Utilities.error(ipAddress + " at port " + portNumber
					+ " is unknown", true);
		} catch (IOException e) {
			Utilities.error("could not start a connection", true);
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			Utilities
					.error("usage: java Client <server_IP_address> <server_port_no>", true);
		}

		// Get IP address
		String ipAddress = args[0];

		// Get port number
		int portNumber = Utilities.parsePortNumber(args[1]);

		new Client(ipAddress, portNumber);
	}
}
