/**
 * {@link Utilities} is a static class with methods that the {@link Server} and
 * the {@link Client} share.
 * 
 * @author Lauren Zou
 */
public class Utilities {
	public static final String EXIT = "<exit>";
	public static final String NEWLINE = "<br>";
	
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
			error("port number must be an integer", true);
		}
		if (portNumber < 1 || portNumber > 65535) {
			error("port number must a positive integer between 1 and 65535", true);
		}
		return portNumber;
	}

	/**
	 * Prints an error message to <code>System.err</code> and then exits the
	 * program if specified.
	 * 
	 * @param message
	 *            error message
	 * @param exit
	 *            whether to exit the program or not
	 */
	public static void error(String message, boolean exit) {
		System.err.println(message);
		if (exit) {
			System.exit(1);
		}
	}
}
