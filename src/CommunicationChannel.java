import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.lang.*;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */

	ConcurrentLinkedQueue<Message> minerChannel;
	ConcurrentLinkedQueue<Message> wizardChannel;

	ConcurrentHashMap<Integer, LinkedList<Message>> wizardMessages;

	public CommunicationChannel() {
		minerChannel = new ConcurrentLinkedQueue<Message>();
		wizardChannel = new ConcurrentLinkedQueue<Message>();

		wizardMessages =  new ConcurrentHashMap<Integer, LinkedList<Message>>();
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		System.out.println("minner channel: " + message.getData());
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

		// if it's an exit message, we don't care. just put it there
		if (message.getData() == Wizard.EXIT) {

			wizardChannel.add(message);
			return;
		}

		// get the wizard's id
		int wizardId = (int) Thread.currentThread().getId();

		// if it's an end message, add all the messages for this end message
		if (message.getData() == Wizard.END) {
			LinkedList<Message> wizardList = wizardMessages.get(wizardId);
			wizardList.add(message);

			synchronized (wizardChannel) {
				wizardChannel.addAll(wizardList);
			}

			return;
		}

		// if it's a room message, add it to the stored messages
		if (wizardMessages.containsKey(wizardId)) {
			LinkedList<Message> wizardOldList = wizardMessages.get(wizardId);
			@SuppressWarnings("unchecked")  // damn java warnings
			LinkedList<Message> wizardNewList = (LinkedList<Message>) wizardOldList.clone();
			wizardNewList.add(message);
			wizardMessages.replace(wizardId, wizardOldList, wizardNewList);
		} else {
			LinkedList<Message> newWizardList = new LinkedList<Message>();
			newWizardList.add(message);
			wizardMessages.put(wizardId, newWizardList);
		}
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
