package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;

@SuppressWarnings("DuplicatedCode")
public class Day14 implements IPuzzle {
	private static final int REQUIRED_REPETITON = 3;
	private static final int REQUIRED_SUB_REPETITON = 5;
	private static final int REQUIRED_WINDOW = 1000;
	private static final int REQUIRED_KEYS = 64;
	private static final int PART_B_REHASHES = 2016;
	private static final byte[] HEX_LOOKUP = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		byte[] secretKey = new String(inputCharacters).trim().getBytes(StandardCharsets.UTF_8);
		return new BasicPuzzleResults<>(
			run(printWriter, "A", secretKey, (messageDigest, resultDigest) -> resultDigest),
			run(printWriter, "B", secretKey, (messageDigest, resultDigest) -> {
				int digestLength = resultDigest.length;
				for (int rehash=1; rehash<=PART_B_REHASHES; rehash++) {
					for (int resultDigestByteIndex = 0; resultDigestByteIndex < digestLength; resultDigestByteIndex++) {
						byte resultDigestByte = resultDigest[resultDigestByteIndex];
						messageDigest.update(HEX_LOOKUP[(resultDigestByte>>4)&0xF]);
						messageDigest.update(HEX_LOOKUP[resultDigestByte&0xF]);
					}
					resultDigest = messageDigest.digest();
				}
				return resultDigest;
			})
		);
	}

	private int run(PrintWriter printWriter, String part, byte[] secretKey, BiFunction<MessageDigest,byte[],byte[]> digestManipulator) {
		printWriter.println("Running part " + part);
		printWriter.flush();
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unable to get MD5 instance", ex);
		}
		Map<Integer, SortedSet<Integer>> pendingOccurances = new HashMap<>();
		for (int value=0x0; value<=0xF; value++) {
			pendingOccurances.put(value,new TreeSet<>());
		}
		byte[] valueAsciiBytes = new byte[]{'0'};
		int valueAsciiBytesStartIndex = 0;
		int valueInt = 0;
		int digestLength = messageDigest.getDigestLength();
		Set<Integer> keyIndicies = new HashSet<>();
		boolean shouldContinue = true;
		Integer stopValue = null;
		while (stopValue == null || valueInt <= stopValue) {
			messageDigest.update(secretKey);
			messageDigest.update(valueAsciiBytes);
			byte[] resultDigest = digestManipulator.apply(messageDigest,messageDigest.digest());
			int previousResultDigestValue = -1;
			int repetitionCount = 0;
			boolean tripletAdded = false;
			for (int resultDigestByteIndex = 0; resultDigestByteIndex < digestLength && shouldContinue; resultDigestByteIndex++) {
				byte resultDigestByte = resultDigest[resultDigestByteIndex];
				for (int bitShift = 4; bitShift >= 0; bitShift -= 4) {
					int resultDigestValue = (resultDigestByte >> bitShift) & 0xF;
					if (resultDigestValue == previousResultDigestValue) {
						repetitionCount++;
						if (repetitionCount == 3 || repetitionCount == 5) {
							if (repetitionCount == 3 && !tripletAdded) {
								pendingOccurances.get(resultDigestValue).add(valueInt);
								tripletAdded = true;
							}
							if (repetitionCount == 5) {
								Iterator<Integer> pendingOccurancesIterator = pendingOccurances.get(resultDigestValue).iterator();
								while (pendingOccurancesIterator.hasNext()) {
									int possibleOccurance = pendingOccurancesIterator.next();
									if (possibleOccurance >= valueInt) {
										break;
									}
									int gap = valueInt - possibleOccurance;
									if (gap > REQUIRED_WINDOW) {
										pendingOccurancesIterator.remove();
									} else {
										keyIndicies.add(possibleOccurance);
										if (stopValue == null && keyIndicies.size() >= REQUIRED_KEYS) {
											stopValue = valueInt+REQUIRED_WINDOW+1;
										}
										printWriter.println("Found key " + keyIndicies.size() + " at pair " + possibleOccurance + "/" + valueInt + "");
										printWriter.flush();
									}
								}
							}
						}
					} else {
						previousResultDigestValue = resultDigestValue;
						repetitionCount = 1;
					}
				}
			}
			for (int index = valueAsciiBytesStartIndex; index >= -1; index--) {
				if (index < 0) {
					int oldLength = valueAsciiBytes.length;
					byte[] newValue = new byte[oldLength + 1];
					System.arraycopy(valueAsciiBytes, 0, newValue, 1, oldLength);
					newValue[0] = '1';
					valueAsciiBytes = newValue;
					valueAsciiBytesStartIndex = oldLength;
				}
				else {
					if (++valueAsciiBytes[index] > '9') {
						valueAsciiBytes[index] = '0';
					}
					else {
						break;
					}
				}
			}
			valueInt++;
		}
		List<Integer> sortedKeyIndicies = new ArrayList<>(keyIndicies);
		Collections.sort(sortedKeyIndicies);
		return sortedKeyIndicies.get(REQUIRED_KEYS-1);
	}
}
