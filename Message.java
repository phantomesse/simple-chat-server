
public class Message {
	private String message;
	private User fromUser;
	private User[] toUsers;
	
	public Message(String message, User fromUser, User[] toUsers) {
		this.message = message;
		this.fromUser = fromUser;
		this.toUsers = toUsers;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the {@link User} who wrote the <code>Message</code>
	 */
	public User getFromUser() {
		return fromUser;
	}

	/**
	 * @return the {@link User}s who should receive the <code>Message</code>
	 */
	public User[] getToUsers() {
		return toUsers;
	}
}
