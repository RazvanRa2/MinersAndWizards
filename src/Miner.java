import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
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
		// TODO IMPLEMENT ME 
	}
}
