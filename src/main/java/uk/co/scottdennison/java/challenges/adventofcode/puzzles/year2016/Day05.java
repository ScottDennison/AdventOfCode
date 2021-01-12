package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("DuplicatedCode")
public class Day05 implements IPuzzle {
	private static final int PASSWORD_LENGTH = 8;
	private static final int ZEROS_REQUIRED = 5;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String secretKeyString = new String(inputCharacters).trim();
		byte[] secretKey = secretKeyString.getBytes(StandardCharsets.UTF_8);
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Could not instantiate MD5", ex);
		}
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
				printWriter.format("Found %d zeros using code %s.%n", zerosFound, new String(value, StandardCharsets.US_ASCII));
				printWriter.flush();
				if (password1CharactersFound < PASSWORD_LENGTH) {
					char newCharacter = convertValueToSingleDigitHex(passwordCharacterValue1);
					printWriter.format("\tSet password #1 character %d to %c.%n", password1CharactersFound, newCharacter);
					printWriter.flush();
					password1[password1CharactersFound++] = newCharacter;
				}
				if (passwordCharacterValue1 >= 0 && passwordCharacterValue1 < PASSWORD_LENGTH) {
					int bitMask = 1 << passwordCharacterValue1;
					if ((password2CharactersBitfield & bitMask) != 0) {
						char newCharacter = convertValueToSingleDigitHex(passwordCharacterValue2);
						printWriter.format("\tSet password #2 character %d to %c.%n", passwordCharacterValue1, newCharacter);
						printWriter.flush();
						password2[passwordCharacterValue1] = newCharacter;
						password2CharactersBitfield &= ~bitMask;
					}
					else {
						printWriter.format("\tUnable to set password #2 character %d as it has already been set.%n", passwordCharacterValue1);
						printWriter.flush();
					}
				}
				else {
					printWriter.format("\tUnable to set password #2 character as %d is not a valid index.%n", passwordCharacterValue1);
					printWriter.flush();
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
		return new BasicPuzzleResults<>(
			new String(password1),
			new String(password2)
		);
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
