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

	public static AtomicInteger hashCount;
	public static ConcurrentHashMap<Integer, Object> solved = new ConcurrentHashMap<>();
	public static CommunicationChannel channel = new CommunicationChannel();

	private static Semaphore minerSemaphore = new Semaphore(1);

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

	public Miner(Integer newHashCount, Set<Integer> newSolved, CommunicationChannel newChannel) {
		channel = newChannel;
		hashCount = new AtomicInteger((int)newHashCount);

		for (Integer value : newSolved) {
			solved.putIfAbsent(value, new Object());
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				minerSemaphore.acquire();
			} catch (Exception ex) {
				System.out.println(ex);
			}
			Message messageFromWizards = channel.getMessageWizardChannel();

			// EXIT case -> thread (miner) should die
			if (messageFromWizards.getData() == Wizard.EXIT) {
				System.out.println("READ EXIT");
				minerSemaphore.release();
				return;
			}

			// END case -> 2nd message should not be read
			if (messageFromWizards.getData() == Wizard.END) {
				System.out.println("READ END");
				minerSemaphore.release();
				continue;
			}

			// parent room case
			int parentRoomNo = messageFromWizards.getCurrentRoom();
			System.out.println("READ PARENT");
			// read child room after reading parent room
			messageFromWizards = channel.getMessageWizardChannel();
			System.out.println("READ ROOM");
			// release semaphore asap
			minerSemaphore.release();

			int childRoomNo = messageFromWizards.getCurrentRoom();

			// check previously solved room
			if (!solved.containsKey(childRoomNo)) {
				String crypticMessage = messageFromWizards.getData();

				String decryptedMessage = encryptMultipleTimes(crypticMessage,
				 							hashCount.get());

				// build answer for the mighty wizards
				Message messageToWizards = new Message(parentRoomNo, childRoomNo,
											decryptedMessage);

				// mark room as solved
				solved.put(childRoomNo, new Object());

				// tell Dumbledore
				channel.putMessageMinerChannel(messageToWizards);
			}
		}
	}

}
