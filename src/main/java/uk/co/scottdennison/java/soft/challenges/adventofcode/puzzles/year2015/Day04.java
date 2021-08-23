package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("DuplicatedCode")
public class Day04 implements IPuzzle {
	private static final int REQUIRED_ZEROS_A = 5;
	private static final int REQUIRED_ZEROS_B = 6;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String secretKeyString = new String(inputCharacters).trim();
		byte[] secretKey = secretKeyString.getBytes(StandardCharsets.UTF_8);
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unable to get MD5 instance", ex);
		}
		int maxZerosRequired = Math.max(REQUIRED_ZEROS_A, REQUIRED_ZEROS_B);
		long[] zerosFoundAt = new long[maxZerosRequired + 1];
		byte[] value = new byte[0];
		int startIndex = -1;
		int bestZerosFound = 0;
		int digestLength = messageDigest.getDigestLength();
		while (true) {
			for (int index = startIndex; index >= -1; index--) {
				if (index < 0) {
					int oldLength = value.length;
					byte[] newValue = new byte[oldLength + 1];
					System.arraycopy(value, 0, newValue, 1, oldLength);
					newValue[0] = '1';
					value = newValue;
					startIndex = oldLength;
				}
				else {
					if (++value[index] > '9') {
						value[index] = '0';
					}
					else {
						break;
					}
				}
			}
			messageDigest.update(secretKey);
			messageDigest.update(value);
			byte[] resultDigest = messageDigest.digest();
			int zerosFound = 0;
			for (int byteIndex = 0; byteIndex < digestLength; byteIndex++) {
				byte byteValue = resultDigest[byteIndex];
				if (byteValue == 0) {
					zerosFound += 2;
				}
				else if ((byteValue & 0xF0) == 0) {
					zerosFound += 1;
					break;
				}
				else {
					break;
				}
			}
			if (zerosFound > bestZerosFound) {
				String valueString = new String(value, StandardCharsets.US_ASCII);
				long valueLong = Long.parseLong(valueString);
				while (bestZerosFound < zerosFound) {
					bestZerosFound++;
					if (bestZerosFound > maxZerosRequired) {
						break;
					}
					printWriter.format("%d zero(s) found with value: %s%n", bestZerosFound, valueString);
					printWriter.flush();
					zerosFoundAt[bestZerosFound] = valueLong;
				}
				if (zerosFound >= maxZerosRequired) {
					break;
				}
			}
		}
		return new BasicPuzzleResults<>(
			zerosFoundAt[REQUIRED_ZEROS_A],
			zerosFoundAt[REQUIRED_ZEROS_B]
		);
	}
}
