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
		while (minerChannel.isEmpty()) {
			try {
				this.wait();
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}

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
		while (wizardChannel.isEmpty()) {
			try {
				this.wait();
			} catch (Exception ex) {
				System.out.println();
			}
		}

		return wizardChannel.poll();
	}
}
