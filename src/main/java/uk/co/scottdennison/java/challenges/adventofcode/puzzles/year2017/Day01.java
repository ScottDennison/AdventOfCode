package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day01 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[] inputCharactersTrimmed = new String(inputCharacters).trim().toCharArray();
		int inputCharactersLength = inputCharactersTrimmed.length;
		int captcha2Offset = inputCharactersLength / 2;
		int captcha1 = 0;
		int captcha2 = 0;
		for (int inputCharacterIndex = 0; inputCharacterIndex < inputCharactersLength; inputCharacterIndex++) {
			captcha1 += checkCharacter(inputCharactersTrimmed, inputCharacterIndex, 1);
			captcha2 += checkCharacter(inputCharactersTrimmed, inputCharacterIndex, captcha2Offset);
		}
		return new BasicPuzzleResults<>(
			captcha1,
			captcha2
		);
	}

	private static int checkCharacter(char[] inputCharacters, int currentIndex, int offset) {
		char currentInputCharacter = inputCharacters[currentIndex];
		char checkInputCharacter = inputCharacters[(currentIndex + offset) % inputCharacters.length];
		if (currentInputCharacter == checkInputCharacter) {
			if (currentInputCharacter >= '0' && currentInputCharacter <= '9') {
				return (currentInputCharacter - '0');
			}
			else {
				throw new IllegalStateException("Invalid input character.");
			}
		}
		else {
			return 0;
		}
	}
}
