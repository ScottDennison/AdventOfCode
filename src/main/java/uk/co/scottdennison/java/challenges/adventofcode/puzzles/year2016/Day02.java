package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;

public class Day02 implements IPuzzle {
	private static final class PadDefinition {
		private final char[][] characters;
		private final int rows;
		private final int columns;
		private final int initialX;
		private final int initialY;

		public PadDefinition(char[][] characters, int initialX, int initialY) {
			this.rows = characters.length;
			this.columns = characters[0].length;
			this.characters = new char[rows][columns];
			for (int y = 0; y < this.rows; y++) {
				char[] row = characters[y];
				if (row.length != this.columns) {
					throw new IllegalStateException("Incorrectly sized 2d array.");
				}
				System.arraycopy(row, 0, this.characters[y], 0, this.columns);
			}
			if (!this.canPress(initialX, initialY)) {
				throw new IllegalStateException("Initial co-ordinates of " + initialX + "," + initialY + " point to no key.");
			}
			this.initialX = initialX;
			this.initialY = initialY;
		}

		public int getInitialX() {
			return this.initialX;
		}

		public int getInitialY() {
			return this.initialY;
		}

		private char getActualCharacter(int x, int y) {
			if (x < 0 || x >= this.columns || y < 0 || y >= this.rows) {
				throw new IllegalArgumentException("Invalid co-ordinates: " + x + "," + y);
			}
			return this.characters[y][x];
		}

		public char getCodeCharacter(int x, int y) {
			char actualCharacter = this.getActualCharacter(x, y);
			if (actualCharacter == 0) {
				throw new IllegalStateException("Desired co-ordinates " + x + "," + y + " point to no key.");
			}
			return actualCharacter;
		}

		public boolean canPress(int x, int y) {
			return this.getActualCharacter(x, y) != 0;
		}
	}

	private static final class PadState {
		private final PadDefinition padDefinition;
		private final StringBuilder codeBuilder;
		private int x;
		private int y;

		public PadState(PadDefinition padDefinition) {
			this.padDefinition = padDefinition;
			this.codeBuilder = new StringBuilder();
			this.reset();
		}

		public void move(int xAddition, int yAddition) {
			int newX = this.x + xAddition;
			int newY = this.y + yAddition;
			if (this.padDefinition.canPress(newX, newY)) {
				this.x = newX;
				this.y = newY;
			}
		}

		public void press() {
			this.codeBuilder.append(this.padDefinition.getCodeCharacter(this.x, this.y));
		}

		public String getCurrentCode() {
			return this.codeBuilder.toString();
		}

		public void reset() {
			this.codeBuilder.setLength(0);
			this.x = padDefinition.getInitialX();
			this.y = padDefinition.getInitialY();
		}
	}

	private static final PadDefinition PART_A_PAD_DEFINITION =
		new PadDefinition(
			new char[][]{
				// @formatter:off
				{ 0  ,  0  ,  0  ,  0  ,  0},
				{ 0  , '1' , '2' , '3' ,  0},
				{ 0  , '4' , '5' , '6' ,  0},
				{ 0  , '7' , '8' , '9' ,  0},
				{ 0  ,  0  ,  0  ,  0  ,  0}
				// @formatter:on
			},
			2,
			2
		);

	private static final PadDefinition PART_B_PAD_DEFINITION =
		new PadDefinition(
			new char[][]{
				// @formatter:off
				{ 0  ,  0  ,  0  ,  0  ,  0  ,  0  ,  0},
				{ 0  ,  0  ,  0  , '1' ,  0  ,  0  ,  0},
				{ 0  ,  0  , '2' , '3' , '4' ,  0  ,  0},
				{ 0  , '5' , '6' , '7' , '8' , '9' ,  0},
				{ 0  ,  0  , 'A' , 'B' , 'C' ,  0  ,  0},
				{ 0  ,  0  ,  0  , 'D' ,  0  ,  0  ,  0},
				{ 0  ,  0  ,  0  ,  0  ,  0  ,  0  ,  0}
				// @formatter:on
			},
			1,
			3
		);

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		PadDefinition[] padDefinitions = {
			PART_A_PAD_DEFINITION,
			PART_B_PAD_DEFINITION
		};
		int padCount = padDefinitions.length;
		PadState[] padStates = new PadState[padCount];
		for (int padIndex = 0; padIndex < padCount; padIndex++) {
			padStates[padIndex] = new PadState(padDefinitions[padIndex]);
		}
		for (char[] inputLine : LineReader.charArrays(inputCharacters)) {
			for (char character : inputLine) {
				final int xAddition, yAddition;
				switch (character) {
					case 'U':
						xAddition = 0;
						yAddition = -1;
						break;
					case 'D':
						xAddition = 0;
						yAddition = 1;
						break;
					case 'L':
						xAddition = -1;
						yAddition = 0;
						break;
					case 'R':
						xAddition = 1;
						yAddition = 0;
						break;
					default:
						throw new IllegalStateException("Unexpected character");
				}
				for (PadState padState : padStates) {
					padState.move(xAddition, yAddition);
				}
			}
			for (PadState padState : padStates) {
				padState.press();
			}
		}
		return new BasicPuzzleResults<>(
			padStates[0].getCurrentCode(),
			padStates[1].getCurrentCode()
		);
	}
}
