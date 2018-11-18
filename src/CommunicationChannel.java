import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.lang.*;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */

	ConcurrentLinkedQueue<Message> minerChannel;
	ConcurrentLinkedQueue<Message> wizardChannel;

	static ConcurrentHashMap<Integer, LinkedList<Message>> wizardMessages;
	static Semaphore wizardChannelSemaphore;
	static long lastThread;
	public CommunicationChannel() {
		minerChannel = new ConcurrentLinkedQueue<Message>();
		wizardChannel = new ConcurrentLinkedQueue<Message>();

		wizardMessages =  new ConcurrentHashMap<Integer, LinkedList<Message>>();
		wizardChannelSemaphore = new Semaphore(1);

		lastThread = -1;
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
		if (message.getData() != Wizard.EXIT
		&& message.getData() != Wizard.END) {
			long currentThread = Thread.currentThread().getId();
			if (currentThread == lastThread || lastThread == -1) {
				wizardChannel.add(message);
				lastThread = -1;
			} else {
				try {
					wizardChannelSemaphore.acquire();
					wizardChannel.add(message);
					lastThread = Thread.currentThread().getId();
				} catch (Exception ex) {
				}
				wizardChannelSemaphore.release();
			}
		} else {
			try {
				wizardChannelSemaphore.acquire();
			} catch (Exception ex) {
			}
			wizardChannel.add(message);
			wizardChannelSemaphore.release();
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
