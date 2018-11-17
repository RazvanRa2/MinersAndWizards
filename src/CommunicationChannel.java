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
		System.out.println("minner channel: " + message.getData() + "\t" + message.getCurrentRoom() + "\t" + message.getParentRoom());
		minerChannel.add(message);
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		Message messageFromMiners = null;
		do {
			messageFromMiners = minerChannel.poll();
		} while (messageFromMiners == null);

		return messageFromMiners;
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write
	 * to and miners read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
		System.out.println("wizard channel: " + message.getData());
		wizardChannel.add(message);
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 *
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		Message messageFromWizards = null;
		do {
			messageFromWizards = wizardChannel.poll();
		} while (messageFromWizards == null);

		return messageFromWizards;
	}
}
