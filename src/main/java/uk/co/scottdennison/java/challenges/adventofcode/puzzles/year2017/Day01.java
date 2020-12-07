package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Day01 {
	public static void main(String[] args) throws IOException {
		char[] fileCharacters = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim().toCharArray();
		int fileCharactersLength = fileCharacters.length;
		int captcha2Offset = fileCharactersLength / 2;
		int captcha1 = 0;
		int captcha2 = 0;
		for (int fileCharacterIndex = 0; fileCharacterIndex < fileCharactersLength; fileCharacterIndex++) {
			captcha1 += checkCharacter(fileCharacters, fileCharacterIndex, 1);
			captcha2 += checkCharacter(fileCharacters, fileCharacterIndex, captcha2Offset);
		}
		outputSummary(1, captcha1);
		outputSummary(2, captcha2);
	}

	private static void outputSummary(int captchaNumber, int result) {
		System.out.format("Captcha %d answer: %d%n", captchaNumber, result);
	}

	private static int checkCharacter(char[] fileCharacters, int currentIndex, int offset) {
		char currentFileCharacter = fileCharacters[currentIndex];
		char checkFileCharacter = fileCharacters[(currentIndex + offset) % fileCharacters.length];
		if (currentFileCharacter == checkFileCharacter) {
			if (currentFileCharacter >= '0' && currentFileCharacter <= '9') {
				return (currentFileCharacter - '0');
			}
			else {
				throw new IllegalStateException("Invalid file character.");
			}
		}
		else {
			return 0;
		}
	}
}
