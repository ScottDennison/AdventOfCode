package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Day05 {
	private static final int PASSWORD_LENGTH = 8;
	private static final int ZEROS_REQUIRED = 5;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		String secretKeyString = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim();
		byte[] secretKey = secretKeyString.getBytes(StandardCharsets.UTF_8);
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		byte[] value = new byte[]{'0'};
		int startIndex = -1;
		int digestLength = messageDigest.getDigestLength();
		int targetByte1Index = ZEROS_REQUIRED / 2;
		//noinspection ConstantConditions
		int targetByte1BitShift = ZEROS_REQUIRED % 2 == 0 ? 4 : 0;
		int targetByte2Index = (ZEROS_REQUIRED + 1) / 2;
		//noinspection ConstantConditions
		int targetByte2BitShift = ZEROS_REQUIRED % 2 == 0 ? 0 : 4;
		char[] password1 = new char[PASSWORD_LENGTH];
		char[] password2 = new char[PASSWORD_LENGTH];
		int password1CharactersFound = 0;
		int password2CharactersBitfield = (1 << PASSWORD_LENGTH) - 1;
		while (true) {
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
			if (zerosFound >= ZEROS_REQUIRED) {
				int passwordCharacterValue1 = getPasswordValue(resultDigest, targetByte1Index, targetByte1BitShift);
				int passwordCharacterValue2 = getPasswordValue(resultDigest, targetByte2Index, targetByte2BitShift);
				if (password1CharactersFound < PASSWORD_LENGTH) {
					password1[password1CharactersFound++] = convertValueToSingleDigitHex(passwordCharacterValue1);
				}
				if (passwordCharacterValue1 >= 0 && passwordCharacterValue1 < PASSWORD_LENGTH) {
					int bitMask = 1 << passwordCharacterValue1;
					if ((password2CharactersBitfield & bitMask) != 0) {
						password2[passwordCharacterValue1] = convertValueToSingleDigitHex(passwordCharacterValue2);
						password2CharactersBitfield &= ~bitMask;
					}
				}
				if (password2CharactersBitfield == 0) {
					break;
				}
			}
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
		}
		outputSummary(1, password1);
		outputSummary(2, password2);
	}

	private static void outputSummary(int passwordNumber, char[] password) {
		System.out.format("Password %d is %s%n", passwordNumber, new String(password));
	}

	private static int getPasswordValue(byte[] resultDigest, int byteIndex, int byteBitshift) {
		return (resultDigest[byteIndex] >> byteBitshift) & 0xF;
	}

	private static char convertValueToSingleDigitHex(int value) {
		if (value > 9) {
			return (char) ((value - 10) + 'a');
		}
		else {
			return (char) (value + '0');
		}
	}
}
