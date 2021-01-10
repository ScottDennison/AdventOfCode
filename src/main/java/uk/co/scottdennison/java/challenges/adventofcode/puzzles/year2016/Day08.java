package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day08 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?:(?:rect (?<rectWidth>[0-9]+)x(?<rectHeight>[0-9]+))|(?:(?:(?:rotate row y=(?<rotateY>[0-9]+))|(?:rotate column x=(?<rotateX>[0-9]+))) by (?<rotateAmount>[0-9]+)))$");

	private static final int SCREEN_WIDTH = 50;
	private static final int SCREEN_HEIGHT = 6;

	private static final char LIGHT_ON = '#';
	private static final char LIGHT_OFF = '.';

	private static final int CHARACTER_WIDTH = 5;
	private static final int CHARACTER_HEIGHT = 6;
	// Table generated from data gathered in https://github.com/petertseng/adventofcode-rb-2016/blob/master/extras/gen_08/letters.rb, as otherwise I would not have enough letters to build proper detection.
	private static final CharacterKnownDisplay[] CHARACTER_KNOWN_DISPLAYS = {
		new CharacterKnownDisplay('A', ".##..#..#.#..#.####.#..#.#..#."),
		new CharacterKnownDisplay('B', "###..#..#.###..#..#.#..#.###.."),
		new CharacterKnownDisplay('C', ".##..#..#.#....#....#..#..##.."),
		new CharacterKnownDisplay('D', "###..#..#.#..#.#..#.#..#.###.."),
		new CharacterKnownDisplay('E', "####.#....###..#....#....####."),
		new CharacterKnownDisplay('F', "####.#....###..#....#....#...."),
		new CharacterKnownDisplay('G', ".##..#..#.#....#.##.#..#..###."),
		new CharacterKnownDisplay('H', "#..#.#..#.####.#..#.#..#.#..#."),
		new CharacterKnownDisplay('I', ".###...#....#....#....#...###."),
		new CharacterKnownDisplay('J', "..##....#....#....#.#..#..##.."),
		new CharacterKnownDisplay('K', "#..#.#.#..##...#.#..#.#..#..#."),
		new CharacterKnownDisplay('L', "#....#....#....#....#....####."),
		new CharacterKnownDisplay('M', "#...###.###.#.##...##...##...#"),
		new CharacterKnownDisplay('N', "#...###..##.#.##.#.##..###...#"),
		new CharacterKnownDisplay('O', ".##..#..#.#..#.#..#.#..#..##.."),
		new CharacterKnownDisplay('P', "###..#..#.#..#.###..#....#...."),
		new CharacterKnownDisplay('Q', ".##..#..#.#..#.#.##.#..#..##.#"),
		new CharacterKnownDisplay('R', "###..#..#.#..#.###..#.#..#..#."),
		new CharacterKnownDisplay('S', ".###.#....#.....##.....#.###.."),
		new CharacterKnownDisplay('T', "#####..#....#....#....#....#.."),
		new CharacterKnownDisplay('U', "#..#.#..#.#..#.#..#.#..#..##.."),
		new CharacterKnownDisplay('V', "#...##...##...#.#.#..#.#...#.."),
		new CharacterKnownDisplay('W', "#...##...##.#.##.#.##.#.#.#.#."),
		new CharacterKnownDisplay('X', "#...#.#.#...#...#.#..#.#.#...#"),
		new CharacterKnownDisplay('Y', "#...##...#.#.#...#....#....#.."),
		new CharacterKnownDisplay('Z', "####....#...#...#...#....####."),
		new CharacterKnownDisplay(' ', ".............................."),
	};

	private static class CharacterKnownDisplay {
		private final char character;
		private final String display;

		private CharacterKnownDisplay(char character, String display) {
			this.character = character;
			this.display = display;
			int displayLength = display.length();
			if (displayLength != (CHARACTER_WIDTH * CHARACTER_HEIGHT)) {
				throw new IllegalStateException("Bad character display size");
			}
			for (int displayIndex = 0; displayIndex < displayLength; displayIndex++) {
				switch (display.charAt(displayIndex)) {
					case LIGHT_ON:
					case LIGHT_OFF:
						break;
					default:
						throw new IllegalStateException("Invalid character in display");
				}
			}
		}

		public char getCharacter() {
			return this.character;
		}

		public String getDisplay() {
			return this.display;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		//noinspection ConstantConditions
		if (SCREEN_WIDTH % CHARACTER_WIDTH != 0 || SCREEN_HEIGHT % CHARACTER_HEIGHT != 0) {
			throw new IllegalStateException("Invalid screen size.");
		}

		int currentScreenIndex = 0;
		boolean[][][] screens = new boolean[2][SCREEN_HEIGHT][SCREEN_WIDTH];
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line.");
			}
			boolean[][] currentScreen = screens[currentScreenIndex];
			String temporaryGroupValue;
			if ((temporaryGroupValue = matcher.group("rotateAmount")) != null) {
				int newScreenIndex = (currentScreenIndex + 1) % 2;
				boolean[][] newScreen = screens[newScreenIndex];
				int rotateAmount = Integer.parseInt(temporaryGroupValue);
				int rotateY;
				int rotateX;
				if ((temporaryGroupValue = matcher.group("rotateY")) != null) {
					rotateY = Integer.parseInt(temporaryGroupValue);
					rotateX = -1;

				}
				else {
					rotateX = Integer.parseInt(matcher.group("rotateX"));
					rotateY = -1;
				}
				for (int y = 0; y < SCREEN_HEIGHT; y++) {
					boolean[] currentScreenRow = currentScreen[y];
					for (int x = 0; x < SCREEN_WIDTH; x++) {
						newScreen[(x == rotateX ? ((y + rotateAmount) % SCREEN_HEIGHT) : y)][(y == rotateY ? ((x + rotateAmount) % SCREEN_WIDTH) : x)] = currentScreenRow[x];
					}
				}
				currentScreenIndex = newScreenIndex;
			}
			else {
				int rectWidth = Integer.parseInt(matcher.group("rectWidth"));
				int rectHeight = Integer.parseInt(matcher.group("rectHeight"));
				for (int y = 0; y < rectHeight; y++) {
					boolean[] currentScreenRow = currentScreen[y];
					for (int x = 0; x < rectWidth; x++) {
						currentScreenRow[x] = true;
					}
				}
			}
		}

		int messageHeight = SCREEN_HEIGHT / CHARACTER_HEIGHT;
		int messageWidth = SCREEN_WIDTH / CHARACTER_WIDTH;
		Map<String, Character> knownDisplayToCharacterLookup = new HashMap<>();
		for (CharacterKnownDisplay characterKnownDisplay : CHARACTER_KNOWN_DISPLAYS) {
			String knownDisplay = characterKnownDisplay.getDisplay();
			if (knownDisplayToCharacterLookup.put(knownDisplay, characterKnownDisplay.getCharacter()) != null) {
				throw new IllegalStateException("Multiple characters mapped to the same known display.");
			}
		}

		char[] messageCharacters = new char[((messageWidth + 1) * messageHeight) - 1];
		int characterDisplaySize = CHARACTER_WIDTH * CHARACTER_HEIGHT;
		int lightsOn = 0;
		int messageCharacterIndex = 0;
		boolean[][] currentScreen = screens[currentScreenIndex];
		for (int characterY = 0; characterY < messageHeight; characterY++) {
			//noinspection ConstantConditions (IntelllJ has this wrong as demonstrated by the code working)
			if (characterY != 0) {
				messageCharacters[messageCharacterIndex++] = '\n';
			}
			for (int characterX = 0; characterX < messageWidth; characterX++) {
				char[] characterDisplayCharacters = new char[characterDisplaySize];
				//noinspection ConstantConditions (IntelllJ has this wrong as demonstrated by the code working)
				int screenStartY = (characterY * CHARACTER_HEIGHT);
				int screenStartX = (characterX * CHARACTER_WIDTH);
				int screenEndY = screenStartY + CHARACTER_HEIGHT - 1;
				int screenEndX = screenStartX + CHARACTER_WIDTH - 1;
				int characterDisplayCharacterIndex = 0;
				for (int screenY = screenStartY; screenY <= screenEndY; screenY++) {
					boolean[] currentScreenRow = currentScreen[screenY];
					for (int screenX = screenStartX; screenX <= screenEndX; screenX++) {
						char lightRepresentation;
						if (currentScreenRow[screenX]) {
							lightRepresentation = LIGHT_ON;
							lightsOn++;
						}
						else {
							lightRepresentation = LIGHT_OFF;
						}
						characterDisplayCharacters[characterDisplayCharacterIndex++] = lightRepresentation;
					}
				}
				String characterDisplayString = new String(characterDisplayCharacters);
				Character messageCharacter = knownDisplayToCharacterLookup.get(characterDisplayString);
				if (messageCharacter == null) {
					throw new IllegalStateException("No found character for character display " + characterDisplayString);
				}
				messageCharacters[messageCharacterIndex++] = messageCharacter;
			}
		}

		return new BasicPuzzleResults<>(
			lightsOn,
			new String(messageCharacters)
		);
	}
}
