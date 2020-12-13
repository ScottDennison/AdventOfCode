package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Day09 {
	private static class State {
		private final String characterCountString;
		private final String repetitionCountString;
		private final int decompressedLength;
		private final int requiredCharacters;

		public State(String characterCountString, String repetitionCountString, int decompressedLength, int requiredCharacters) {
			this.characterCountString = characterCountString;
			this.repetitionCountString = repetitionCountString;
			this.decompressedLength = decompressedLength;
			this.requiredCharacters = requiredCharacters;
		}

		public String getCharacterCountString() {
			return this.characterCountString;
		}

		public String getRepetitionCountString() {
			return this.repetitionCountString;
		}

		public int getDecompressedLength() {
			return this.decompressedLength;
		}

		public int getRequiredCharacters() {
			return this.requiredCharacters;
		}
	}

	private static class ActionResult {
		private final Action nextAction;
		private final State state;
		private final boolean allowEnd;

		public ActionResult(Action nextAction, State state, boolean allowEnd) {
			this.nextAction = nextAction;
			this.state = state;
			this.allowEnd = allowEnd;
		}

		public Action getNextAction() {
			return this.nextAction;
		}

		public State getState() {
			return this.state;
		}

		public boolean isAllowEnd() {
			return this.allowEnd;
		}
	}

	private enum Action {
		NORMAL_INPUT {
			@Override
			ActionResult process(State state, char character) {
				if (character == '(') {
					return new ActionResult(
						CHARACTER_COUNT_INPUT,
						new State(
							"",
							null,
							state.getDecompressedLength(),
							-1
						),
						false
					);
				}
				else {
					return new ActionResult(
						NORMAL_INPUT,
						new State(
							null,
							null,
							state.getDecompressedLength() + 1,
							-1
						),
						true
					);
				}
			}
		},
		CHARACTER_COUNT_INPUT {
			@Override
			ActionResult process(State state, char character) {
				if (character >= '0' && character <= '9') {
					return new ActionResult(
						CHARACTER_COUNT_INPUT,
						new State(
							state.getCharacterCountString() + character,
							null,
							state.getDecompressedLength(),
							-1
						),
						false
					);
				}
				else if (character == 'x' || character == 'X') {
					return new ActionResult(
						REPETITION_COUNT_INPUT,
						new State(
							state.getCharacterCountString(),
							"",
							state.getDecompressedLength(),
							-1
						),
						false
					);
				}
				else {
					throw new IllegalStateException("Unexpected character");
				}
			}
		},
		REPETITION_COUNT_INPUT {
			@Override
			ActionResult process(State state, char character) {
				if (character >= '0' && character <= '9') {
					return new ActionResult(
						REPETITION_COUNT_INPUT,
						new State(
							state.getCharacterCountString(),
							state.getRepetitionCountString() + character,
							state.getDecompressedLength(),
							-1
						),
						false
					);
				}
				else if (character == ')') {
					int characterCount = Integer.parseInt(state.getCharacterCountString());
					if (characterCount <= 0) {
						return new ActionResult(
							NORMAL_INPUT,
							new State(
								null,
								null,
								state.getDecompressedLength(),
								characterCount
							),
							true
						);
					}
					else {
						int repetitionCount = Integer.parseInt(state.getRepetitionCountString());
						return new ActionResult(
							REPETITION_CONSUMPTION,
							new State(
								null,
								null,
								state.getDecompressedLength() + (characterCount * repetitionCount),
								characterCount
							),
							false
						);
					}

				}
				else {
					throw new IllegalStateException("Unexpected character");
				}
			}
		},
		REPETITION_CONSUMPTION {
			@Override
			ActionResult process(State state, char character) {
				int newRequiredCharacters = state.getRequiredCharacters() - 1;
				if (newRequiredCharacters < 0) {
					throw new IllegalStateException("Invalid required characters count.");
				}
				else if (newRequiredCharacters == 0) {
					return new ActionResult(
						NORMAL_INPUT,
						new State(
							null,
							null,
							state.getDecompressedLength(),
							-1
						),
						true
					);
				}
				else {
					return new ActionResult(
						REPETITION_CONSUMPTION,
						new State(
							null,
							null,
							state.getDecompressedLength(),
							newRequiredCharacters
						),
						false
					);
				}
			}
		};

		abstract ActionResult process(State state, char character);
	}

	public static void main(String[] args) throws IOException {
		ActionResult actionResult = new ActionResult(
			Action.NORMAL_INPUT,
			new State(
				null,
				null,
				0,
				-1
			),
			false
		);
		for (char fileCharacter : new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim().toCharArray()) {
			actionResult = actionResult.getNextAction().process(actionResult.getState(), fileCharacter);
		}
		if (!actionResult.isAllowEnd()) {
			throw new IllegalStateException("Unexpected EOF");
		}
		System.out.format("Decompressed length: %d%n", actionResult.getState().getDecompressedLength());
	}
}
