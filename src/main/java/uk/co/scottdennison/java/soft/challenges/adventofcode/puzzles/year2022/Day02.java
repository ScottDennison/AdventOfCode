package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day02 implements IPuzzle {
	private static final int[] SCORES_PART_A = new int[] {4,8,3,1,5,9,7,2,6};
	private static final int[] SCORES_PART_B = new int[] {3,4,8,1,5,9,2,6,7};

	public enum State {
		ELF_ACTION,
		WHITESPACE,
		MY_ACTION,
		NEWLINE
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		return new BasicPuzzleResults<>(
			run(inputCharacters, SCORES_PART_A),
			run(inputCharacters, SCORES_PART_B)
		);
	}

	private static int run(char[] inputCharacters, int[] scores) {
		int index = 0;
		int total = 0;
		State state = State.ELF_ACTION;
		for (char inputCharacter : inputCharacters) {
			switch (state) {
				case NEWLINE:
					switch (inputCharacter) {
						case ' ':
						case '\t':
						case '\r':
						case '\n':
							continue;
						default:
							// Fall through.
					}
					state = State.ELF_ACTION;
				case ELF_ACTION:
					switch (inputCharacter) {
						case 'A':
							index += 0;
							break;
						case 'B':
							index += 1;
							break;
						case 'C':
							index += 2;
							break;
						default:
							throw new IllegalStateException("Unexpected elf action character");
					}
					index *= 3;
					state = State.WHITESPACE;
					break;
				case WHITESPACE:
					switch (inputCharacter) {
						case ' ':
						case '\t':
							continue;
						default:
							// Fall through.
					}
					state = State.MY_ACTION;
				case MY_ACTION:
					switch (inputCharacter) {
						case 'X':
							index += 0;
							break;
						case 'Y':
							index += 1;
							break;
						case 'Z':
							index += 2;
							break;
						default:
							throw new IllegalStateException("Unexpected my action character");
					}
					total += scores[index];
					index = 0;
					state = State.NEWLINE;
					break;
			}
		}
		if (state == State.WHITESPACE || state == State.MY_ACTION) {
			throw new IllegalStateException("Incomplete input");
		}
		return total;
	}
}
