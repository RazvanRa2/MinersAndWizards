import java.util.concurrent.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */

	static ConcurrentLinkedQueue<Message> minerChannel;
	static ConcurrentLinkedQueue<Message> wizardChannel;

	static ConcurrentHashMap<Integer, LinkedList<Message>> wizardMessages;
	static Semaphore nextMessageSemaphore;
	static Semaphore endExitSemaphore;
	static long lastThread = -1;
	
	public CommunicationChannel() {
		if (minerChannel == null)
			minerChannel = new ConcurrentLinkedQueue<Message>();
		if (wizardChannel == null)
			wizardChannel = new ConcurrentLinkedQueue<Message>();
		if (wizardMessages == null)
			wizardMessages =  new ConcurrentHashMap<Integer, LinkedList<Message>>();
		if (nextMessageSemaphore == null)
			nextMessageSemaphore = new Semaphore(1);
		if (endExitSemaphore == null)
			endExitSemaphore = new Semaphore(1);
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		//System.out.println("minner channel: " + message.getData());
		minerChannel.add(message);
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 *
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
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
		long currentThread = Thread.currentThread().getId();

		if (message.getData() != Wizard.EXIT && message.getData() != Wizard.END) {
			if (currentThread == lastThread) {
				wizardChannel.add(message);
				nextMessageSemaphore.release();
			} else {
				try {
					nextMessageSemaphore.acquire();
					endExitSemaphore.acquire();
				} catch (Exception ex) {
				}

				wizardChannel.add(message);
				lastThread = currentThread;
			}
		} else {
			wizardChannel.add(message);
			nextMessageSemaphore.release();
			endExitSemaphore.release();
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
		} while(messageFromWizards == null);
		
		return messageFromWizards;
	}
}
