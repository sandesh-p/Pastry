/**
 * Class Pastry allows for an object to be created which simulates
 * routing using the Pastry substrate.
 *
 * @author  Richard Ballard
 * @author  Sandesh Pardeshi
 * @author  Mohanish Sawant
 */
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import edu.rit.util.Random;

public class Pastry {
	private int nodeCount; // number of nodes
	private final int LEAF_RANGE = 16; // how many nodes per leaf set
	public BigInteger[] nodes; // node IDs
	public int seed; // seed for prng
	private Random random = Random.getInstance(seed); // Random with seed

	public Pastry(int nodeCount, int seed) {
		// set the number of nodes
		this.nodeCount = nodeCount;
		this.seed = seed;
		nodes = new BigInteger[nodeCount];

		// populate the array of node ids
		popNodes();
	}// Pastry (int nodeCount)

	/**
	 * Perform a single random trial.
	 */
	public int runTrial() {
		return route(random.nextInt(nodeCount), random.nextInt(nodeCount));
	}// runTrial()

	/**
	 * Generate a specified number of random node IDs, then sort those IDs and
	 * store them in an array of BigIntegers.
	 */
	private void popNodes() {
		// create arraylist used to sort node IDs
		ArrayList<BigInteger> nodeList = new ArrayList<BigInteger>();

		// Integer sample = random.nextInteger(100000000);
		Integer sample = random.nextInteger(100);

		for (int i = 0; i < nodeCount; i++) {
			sample++; // next random node

			// 128 bit MD5 hashing
			byte[] hash = sample.toString().getBytes();
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");

				BigInteger nodeID = new BigInteger(md.digest(hash));

				// add to the nodeList
				nodeList.add(nodeID.abs());
			} catch (NoSuchAlgorithmException e) {
			}
		}

		// sort the nodeList
		Collections.sort(nodeList);

		nodes = nodeList.toArray(new BigInteger[nodeList.size()]);
	}// popNodes()

	/**
	 * Simulate routing and return the number of hops needed get from the source
	 * to the destination. Source and destination are indices to a sorted array
	 * containing all of the node ids in the simulated network.
	 * 
	 * @param int sourceIndex
	 * @param int destIndex
	 */
	public int route(int sourceIndex, int destIndex) {
		int hops = 0;
		int prefix = 0;

		// whether the destination has been found
		boolean destFound = (sourceIndex == destIndex);

		// whether prefix matching should search left or right
		boolean left = (sourceIndex > destIndex);
		String destination;

		while (destFound == false) {
			// increment counters
			hops++;
			prefix++;

			// check the leafset
			destFound = leafSet(sourceIndex, destIndex);

			// match prefix if not in leaf set
			if (destFound == false) {
				destination = String.format("%032x", nodes[destIndex]);
				sourceIndex = prefixMatch(destination, prefix, sourceIndex,
						left);
			} else
				sourceIndex = destIndex;

			if (sourceIndex == destIndex)
				destFound = true;

		}// while(destFound == false) {

		return hops;
	}// route(int sourceIndex, int destIndex)

	/**
	 * Check whether the destination index is in the leafset of the source node.
	 * 
	 * @param int sourceIndex
	 * @param int destIndex
	 */
	private Boolean leafSet(int sourceIndex, int destIndex) {
		for (int i = 1; i <= LEAF_RANGE / 2; i++) {
			if ((mod((sourceIndex + i), nodeCount) == (destIndex))
					|| (mod((sourceIndex - i), nodeCount) == (destIndex))) {
				return true;
			}
		}
		return false;
	}// leafSet(int sourceIndex, int destIndex) {

	/**
	 * Simulate the behavior of a routing table lookup.
	 * 
	 * @param String
	 *            nodeID
	 * @param int prefix
	 * @param int sourceIndex
	 * @param boolean left
	 */
	private int prefixMatch(String nodeID, int prefix, int sourceIndex,
			boolean left) {
		// if the source id is greater than the node id, search the nodes
		// array to the left
		if (left) {
			for (int i = sourceIndex; i < nodes.length; i = mod(i - 1,
					nodeCount)) {
				if (nodeID.regionMatches(0, String.format("%032x", nodes[i]),
						0, prefix))

					return i;
			}
		}// if(left) {

		// if the source id is less than the node id, search the nodes
		// array to the right
		else {
			for (int i = sourceIndex; i < nodes.length; i = mod(i + 1,
					nodeCount)) {

				if (nodeID.regionMatches(0, String.format("%032x", nodes[i]),
						0, prefix))

					return i;
			}
		}

		// if the nodes array does not contain a prefix match
		return -1;
	}// prefixMatch(String nodeID, int prefix) {

	/**
	 * Implementation of mod that handles negative values correctly.
	 * 
	 * @param int x
	 * @param int y
	 */
	private int mod(int x, int y) {
		int result = x % y;

		return result < 0 ? result + y : result;
	}// mod(int x, int y) {
}// Pastry