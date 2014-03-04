import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class User {
	private String username;
	private String password;

	private boolean isOnline;
	private long lastLoggedIn;
	private String threadId;

	/**
	 * @key ip address
	 * @value timestamp of blocked time
	 */
	private HashMap<String, Date> blockedIpAddresses;

	/**
	 * @key ip address
	 * @value number of attempts
	 */
	private HashMap<String, Integer> loginAttempts;
	private final static int MAX_LOGIN_ATTEMPTS = 3;

	private LinkedList<Message> messages;

	public class UserAlreadyLoggedInException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public class IpAddressBlockedException extends Exception {
		private static final long serialVersionUID = 1L;

		private int secondsLeft;

		public IpAddressBlockedException(int secondsLeft) {
			this.secondsLeft = secondsLeft;
		}

		/**
		 * @return seconds left before this ip address is unblocked
		 */
		public int getSecondsLeft() {
			return this.secondsLeft;
		}
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;

		this.isOnline = false;
		this.lastLoggedIn = -1;
		this.threadId = null;

		blockedIpAddresses = new HashMap<String, Date>();
		loginAttempts = new HashMap<String, Integer>();

		this.messages = new LinkedList<Message>();
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Checks if this <code>User</code> matches the given username and password.
	 * Updates loginAttempts and blockedIpAddresses.
	 * 
	 * @param username
	 *            the username to match
	 * @param password
	 *            the password to match
	 * @param ipAddress
	 *            the ip address that this request to match username and
	 *            password is from
	 * @return true if there is a match and false otherwise
	 */
	public boolean matchUsernamePassword(String username, String password,
			String ipAddress) throws IpAddressBlockedException {
		// Check if this ip address is blocked
		if (blockedIpAddresses.containsKey(ipAddress)) {
			// Check if blocked time has expired
			int timePassedSinceBlock = (int) (((new Date()).getTime() - blockedIpAddresses
					.get(ipAddress).getTime()) / 1000);
			if (timePassedSinceBlock < Server.BLOCK_TIME) {
				// Still blocked
				throw new IpAddressBlockedException(Server.BLOCK_TIME
						- timePassedSinceBlock);
			} else {
				// No longer blocked
				blockedIpAddresses.remove(ipAddress);
			}
		}

		boolean match = this.username.equals(username)
				&& this.password.equals(password);

		if (match) {
			// Reset counter in loginAttempts
			if (loginAttempts.containsKey(ipAddress)) {
				loginAttempts.remove(ipAddress);
			}

			return match;
		}

		// Password doesn't match, so update loginAttempts
		if (loginAttempts.containsKey(ipAddress)) {
			int attempts = loginAttempts.get(ipAddress).intValue();
			loginAttempts.put(ipAddress, attempts + 1);

			// Block ip address
			if (attempts == MAX_LOGIN_ATTEMPTS) {
				blockedIpAddresses.put(ipAddress, new Date());
				throw new IpAddressBlockedException(Server.BLOCK_TIME);
			}
		} else {
			loginAttempts.put(ipAddress, 1);
		}
		return false;
	}

	/**
	 * @return whether <code>User</code> is currently connected to the
	 *         {@link Server}
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * Sets this <code>User</code> as online and sets the last logged in
	 * timestamp as the time that this method was called.
	 * 
	 * @param threadId
	 *            of the <code>ServerThread</code> associated with this
	 *            <code>User</code>
	 */
	public void setOnline(String threadId) throws UserAlreadyLoggedInException {
		if (isOnline) {
			throw new UserAlreadyLoggedInException();
		}

		this.isOnline = true;
		this.lastLoggedIn = System.currentTimeMillis();
		this.threadId = threadId;
	}

	/**
	 * Sets this <code>User</code> as offline.
	 */
	public void setOffline() {
		this.isOnline = false;
		this.threadId = null;
	}

	/**
	 * @return threadId of the <code>ServerThread</code> associated with this
	 *         <code>User</code>
	 */
	public String getThreadId() {
		return this.threadId;
	}

	/**
	 * @return timestamp of the last time the user logged in
	 */
	public long getLastLoggedIn() {
		return lastLoggedIn;
	}

	/**
	 * Retrieves a {@link Message} from the list of messages and deletes the
	 * {@link Message} from the list.
	 * 
	 * @return {@link Message} if it exists and <code>null</code> if there are
	 *         no messages
	 */
	public Message popMessage() {
		return this.messages.size() > 0 ? this.messages.pop() : null;
	}

	/**
	 * Adds a {@link Message} to list of messages.
	 * 
	 * @param message
	 *            {@link Message} to add
	 */
	public void addMessage(Message message) {
		messages.add(message);
	}
}