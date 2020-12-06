package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Day04 {
	private static final int MAX_REQUIRED_ZEROS = 6;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		String secretKeyString = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim();
		byte[] secretKey = secretKeyString.getBytes(StandardCharsets.UTF_8);
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
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
				String valueString = new String(value, StandardCharsets.UTF_8);
				while (bestZerosFound < zerosFound) {
					bestZerosFound++;
					System.out.format("%d zero(s) found with value: %s%n", bestZerosFound, valueString);
				}
				if (zerosFound >= MAX_REQUIRED_ZEROS) {
					break;
				}
			}
		}
	}
}
