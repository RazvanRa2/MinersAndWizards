import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.concurrent.Semaphore;
import java.lang.*;
/**
 * Class for a miner.
 */
public class Miner extends Thread {
	/**
	 * Creates a {@code Miner} object.
	 *
	 * @param newHashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param newSolved
	 *            set containing the IDs of the solved rooms
	 * @param newChannel
	 *            communication channel between the miners and the wizards
	 */

	public static int hashCount = -1;
	public static ConcurrentHashMap<Integer, Object> solved = null;
	public static CommunicationChannel channel = null;

	private static Semaphore minerSemaphore = null;
	private static AtomicInteger minerIndex = null;

	private boolean didReadParent = false;
	private int parentRoomNo = -1;
	// method from solver
	private static String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }
	// method for solver
    private static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // convert to string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
            String hex = Integer.toHexString(0xff & messageDigest[i]);
            if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

	public Miner(Integer newHashCount, Set<Integer> newSolved,
	CommunicationChannel newChannel) {
		channel = newChannel;

		hashCount = newHashCount;

		if (minerSemaphore == null)
			minerSemaphore = new Semaphore(1);
		
		if (minerIndex == null)
			minerIndex = new AtomicInteger(1);
		
		if (solved == null)
			solved = new ConcurrentHashMap<Integer, Object>();

		for (Integer value : newSolved) {
			solved.putIfAbsent(value, new Object());
		}
	}

	@Override
	public void run() {
		while (true) {
			if (!didReadParent) {
				try { minerSemaphore.acquire();} catch (Exception ex) {};
			}

			Message messageFromWizards = channel.getMessageWizardChannel();

			// we still didn't get a message from the wizards. Wait.
			if  (messageFromWizards == null) {
				minerSemaphore.release();
				continue;
			}
			// end message case -> nothing to do. release taken semaphore.
			if (messageFromWizards.getData() == Wizard.END) {
				//System.out.println("Read END");
				minerSemaphore.release();
				continue;
			}
			// exit message case=> miner releases semaphore then goes home
			if (messageFromWizards.getData() == Wizard.EXIT) {
				//System.out.println("Read EXIT - " + Thread.currentThread().getId());				
				minerSemaphore.release();
				break;
			}

			if (!didReadParent) {  // reading parent room
				//System.out.println("Read PARENT");
				parentRoomNo = messageFromWizards.getCurrentRoom();
				didReadParent = true;
			} else {
				//System.out.println("Read CHILD");
				minerSemaphore.release();
				didReadParent = false;

				if (!solved.containsKey(messageFromWizards.getCurrentRoom())) {
					int childRoomNo = messageFromWizards.getCurrentRoom();
					String crypticMessage = messageFromWizards.getData();

					channel.putMessageMinerChannel(new Message(parentRoomNo, childRoomNo, 
							encryptMultipleTimes(crypticMessage, hashCount)));

					solved.put(messageFromWizards.getCurrentRoom(), new Object());
				}
			}
		}
		System.out.println("Miner " +  minerIndex.incrementAndGet() + " " + Thread.currentThread().getId() + " going home");
	}
}
