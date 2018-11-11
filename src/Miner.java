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

	public Miner(Integer newHashCount, Set<Integer> newSolved, CommunicationChannel newChannel) {
		channel = newChannel;
		hashCount = new AtomicInteger((int)newHashCount);

		for (Integer value : newSolved) {
			solved.putIfAbsent(value, new Object());
		}
	}

	@Override
	public void run() {
		// get a message from the wizard telling the miner what to mine
		// as long as it is not the exit message
		Message messageFromWizard = channel.getMessageWizardChannel();
		
		while (msmgFromWizard.getData() != "EXIT") {
			// read the data to be mined from the message
			String data = msgFromWizard.getData();
			// mine it
			String dataForWizard = encryptMultipleTimes(data, hashCount.get());

			// after finishing mining, mark the room as finished
			solved.putIfAbsent(messageFromWizard.getCurrentRoom(), new Object());
			// and notify the wizard
			Message messageToWizard = new Message(messageFromWizard.getCurrentRoom(), dataForWizard);
			channel.putMessageMinerChannel(messageToWizard);

			// at the end, get a new message from the wizard
			messageFromWizard = channel.getMessageWizardChannel();
		}
	}

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
}
