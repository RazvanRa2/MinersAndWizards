import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */

	ConcurrentLinkedQueue<Message> minerChannel;
	ConcurrentLinkedQueue<Message> wizardChannel;

	public CommunicationChannel() {
		minerChannel = new ConcurrentLinkedQueue<Message>();
		wizardChannel = new ConcurrentLinkedQueue<Message>();
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		minerChannel.add(message);
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		// wait until there is a message on the channel
		while (minerChannel.isEmpty()) {
			try {
				this.wait();
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
		// and when there is at least one, serve it
		return minerChannel.poll();
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write
	 * to and miners read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
		wizardChannel.add(message);
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 *
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		// wait until there is at least one message on the channel
		while (wizardChannel.isEmpty()) {
			try {
				this.wait();
			} catch (Exception ex) {
				System.out.println();
			}
		}
		// and then serve it
		return wizardChannel.poll();
	}
}
