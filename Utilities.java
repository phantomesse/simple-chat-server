public class Utilities {
	public static final String EXIT = "<exit>";
	public static final String NEWLINE = "<br>";

	/**
	 * Encodes a message from the <code>Server</code> to the <code>Client</code>
	 * .
	 * 
	 * @param prompt
	 * @param message
	 * @return encoded message
	 */
	public static String encodeMessage(String prompt, String message) {
		message = message.replaceAll("\n", NEWLINE);
		return "<prompt>" + prompt + "</prompt><message>" + message
				+ "</message>";
	}

	/**
	 * Decodes a message from the <code>Server</code> to the <code>Client</code>
	 * .
	 * 
	 * @param encodedMessage
	 *            encoded message to decode
	 * @return a {@link String} array with the first value being the prompt and
	 *         the second value being the message
	 */
	public static String[] decodeMessage(String encodedMessage) {
		int startPromptIndex = encodedMessage.indexOf("<prompt>")
				+ "<prompt>".length();
		int endPromptIndex = encodedMessage.indexOf("</prompt>",
				startPromptIndex);
		String prompt = encodedMessage.substring(startPromptIndex,
				endPromptIndex);

		int startMessageIndex = encodedMessage.indexOf("<message>",
				endPromptIndex) + "<message>".length();
		int endMessageIndex = encodedMessage.indexOf("</message>",
				startMessageIndex);
		String message = encodedMessage.substring(startMessageIndex,
				endMessageIndex).replaceAll(NEWLINE, "\n");

		return new String[] { prompt, message };
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
}
