package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day05 implements IPuzzle {
	private static final boolean DEBUG = false;

	private static final Pattern MOVE_PATTERN = Pattern.compile("^move (?<amount>[0-9]+) from (?<fromStackNumber>[0-9]) to (?<toStackNumber>[0-9])$");

	public enum ParseState {
		CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE,
		CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE_POSSIBLY_CRATE_NUMBERS_LINE,
		CRANE_STATE_CRATE_CONTENTS,
		CRANE_STATE_CRATE_RIGHT_SIDE,
		CRANE_STATE_NO_CRATE_CONTENTS,
		CRANE_STATE_NO_CRATE_CONTENTS_OR_CRATE_NUMBER,
		CRANE_STATE_NO_CRATE_RIGHT_SIDE,
		CRANE_STATE_GAP,
		CRATE_NUMBERS_CRATE_LEFT_SIDE,
		CRATE_NUMBERS_CRATE_NUMBER,
		CRATE_NUMBERS_CRATE_RIGHT_SIDE,
		CRATE_NUMBERS_GAP,
		GAP_LINE_START,
		INSTRUCTIONS_START
	}

	private static SortedMap<Integer,Deque<Character>> copyStacks(SortedMap<Integer,Deque<Character>> inputNumberedStacks) {
		SortedMap<Integer,Deque<Character>> copiedNumberedStacks = new TreeMap<>();
		for (Map.Entry<Integer,Deque<Character>> inputNumberedStacksEntry : inputNumberedStacks.entrySet()) {
			copiedNumberedStacks.put(inputNumberedStacksEntry.getKey(),new LinkedList<>(inputNumberedStacksEntry.getValue()));
		}
		return copiedNumberedStacks;
	}

	private static String createResult(SortedMap<Integer,Deque<Character>> numberedStacks) {
		String result = "";
		for (Deque<Character> stack : numberedStacks.values()) {
			result += stack.peekFirst();
		}
		return result;
	}

	private static void summarizeState(PrintWriter printWriter, String stackName, SortedMap<Integer,Deque<Character>> numberedStacks) {
		for (Map.Entry<Integer,Deque<Character>> numberedStacksEntry : numberedStacks.entrySet()) {
			printWriter.print(stackName + " - Stack " + numberedStacksEntry.getKey() + " - ");
			Deque<Character> stack = numberedStacksEntry.getValue();
			if (stack.isEmpty()) {
				printWriter.print("(Empty)");
			}
			else {
				Iterator<Character> stackIterator = stack.iterator();
				while (stackIterator.hasNext()) {
					printWriter.print(stackIterator.next());
				}
			}
			printWriter.println();
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		int inputCharacterIndex = 0;
		ParseState parseState = ParseState.CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE; // First line can't be crate numbers.
		List<Deque<Character>> nonNumberedStacks = new ArrayList<>();
		SortedMap<Integer,Deque<Character>> numberedStacks = new TreeMap<>();
		int stackIndex = 0;
		while (parseState != ParseState.INSTRUCTIONS_START) {
			if (inputCharacterIndex >= inputCharacters.length) {
				throw new IllegalStateException("Ran out of characters while parsing.");
			}
			char inputCharacter = inputCharacters[inputCharacterIndex++];
			if (inputCharacter == '\r') {
				if (inputCharacterIndex < inputCharacters.length && inputCharacters[inputCharacter] == '\n') {
					inputCharacterIndex++;
				}
				inputCharacter = '\n';
			}
			boolean rerunParseSwitch;
			do {
				if (DEBUG) {
					printWriter.println("Running case " + parseState + " with input character _" + inputCharacter + "_");
				}
				rerunParseSwitch = false;
				switch (parseState) {
					case CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRANE_STATE_NO_CRATE_CONTENTS;
						}
						else if (inputCharacter == '[') {
							parseState = ParseState.CRANE_STATE_CRATE_CONTENTS;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE_POSSIBLY_CRATE_NUMBERS_LINE:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRANE_STATE_NO_CRATE_CONTENTS_OR_CRATE_NUMBER;
						}
						else if (inputCharacter == '[') {
							parseState = ParseState.CRANE_STATE_CRATE_CONTENTS;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_CRATE_CONTENTS:
						if (inputCharacter >= 'A' && inputCharacter <= 'Z') {
							for (int stackIndexIter=nonNumberedStacks.size(); stackIndexIter<=stackIndex; stackIndexIter++) {
								nonNumberedStacks.add(new LinkedList<>());
							}
							nonNumberedStacks.get(stackIndex++).addLast(inputCharacter);
							parseState = ParseState.CRANE_STATE_CRATE_RIGHT_SIDE;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_CRATE_RIGHT_SIDE:
						if (inputCharacter == ']') {
							parseState = ParseState.CRANE_STATE_GAP;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_NO_CRATE_CONTENTS:
						if (inputCharacter == ' ') {
							for (int stackIndexIter=nonNumberedStacks.size(); stackIndexIter<=stackIndex; stackIndexIter++) {
								nonNumberedStacks.add(new LinkedList<>());
							}
							if (!nonNumberedStacks.get(stackIndex++).isEmpty()) {
								throw new IllegalStateException("Trying to insert an empty space under some creates");
							}
							parseState = ParseState.CRANE_STATE_NO_CRATE_RIGHT_SIDE;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_NO_CRATE_CONTENTS_OR_CRATE_NUMBER:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRANE_STATE_NO_CRATE_CONTENTS;
							rerunParseSwitch = true;
						}
						else {
							parseState = ParseState.CRATE_NUMBERS_CRATE_NUMBER;
							rerunParseSwitch = true;
							break;
						}
						break;
					case CRANE_STATE_NO_CRATE_RIGHT_SIDE:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRANE_STATE_GAP;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRANE_STATE_GAP:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE;
						}
						else if (inputCharacter == '\n') {
							parseState = ParseState.CRANE_STATE_CRATE_LEFT_SIDE_OR_NO_CRATE_LEFT_SIDE_POSSIBLY_CRATE_NUMBERS_LINE;
							stackIndex = 0;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRATE_NUMBERS_CRATE_LEFT_SIDE:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRATE_NUMBERS_CRATE_NUMBER;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRATE_NUMBERS_CRATE_NUMBER:
						if (inputCharacter >= '1' && inputCharacter <= '9') {
							for (int stackIndexIter=nonNumberedStacks.size(); stackIndexIter<=stackIndex; stackIndexIter++) {
								nonNumberedStacks.add(new LinkedList<>());
							}
							if (numberedStacks.put(inputCharacter-'0',nonNumberedStacks.get(stackIndex++)) != null) {
								throw new IllegalStateException("Duplicate stack number");
							}
							parseState = ParseState.CRATE_NUMBERS_CRATE_RIGHT_SIDE;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRATE_NUMBERS_CRATE_RIGHT_SIDE:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRATE_NUMBERS_GAP;
						}
						else if (inputCharacter == '\n') {
							parseState = ParseState.GAP_LINE_START;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case CRATE_NUMBERS_GAP:
						if (inputCharacter == ' ') {
							parseState = ParseState.CRATE_NUMBERS_CRATE_LEFT_SIDE;
						}
						else if (inputCharacter == '\n') {
							parseState = ParseState.GAP_LINE_START;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
					case GAP_LINE_START:
						if (inputCharacter == '\n') {
							parseState = ParseState.INSTRUCTIONS_START;
						}
						else {
							throw new IllegalStateException("Unexpected input character");
						}
						break;
				}
			} while (rerunParseSwitch);
		}
		if (DEBUG) {
			summarizeState(printWriter, "Initial State", numberedStacks);
		}
		SortedMap<Integer,Deque<Character>> partAStacks = copyStacks(numberedStacks);
		SortedMap<Integer,Deque<Character>> partBStacks = copyStacks(numberedStacks);
		for (String instructionLine : LineReader.strings(Arrays.copyOfRange(inputCharacters, inputCharacterIndex, inputCharacters.length))) {
			if (DEBUG) {
				printWriter.println("Instruction: " + instructionLine);
			}
			Matcher matcher = MOVE_PATTERN.matcher(instructionLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse instruction line");
			}
			int amount = Integer.parseInt(matcher.group("amount"));
			int fromStackNumber = Integer.parseInt(matcher.group("fromStackNumber"));
			int toStackNumber = Integer.parseInt(matcher.group("toStackNumber"));
			if (fromStackNumber == toStackNumber) {
				throw new IllegalStateException("From stack and to stack are the same stack");
			}
			Deque<Character> partAFromStack = partAStacks.get(fromStackNumber);
			Deque<Character> partAToStack = partAStacks.get(toStackNumber);
			Deque<Character> partBFromStack = partBStacks.get(fromStackNumber);
			Deque<Character> partBToStack = partBStacks.get(toStackNumber);
			if (partAFromStack == null) {
				throw new IllegalStateException("From stack does not exist");
			}
			if (partAToStack == null) {
				throw new IllegalStateException("To stack does not exist");
			}
			Deque<Character> partBTempStack = new LinkedList<>();
			// Given we duplicated the stacks map, if part we are here, part B's stacks are also fine.
			for (int iteration=1; iteration<=amount; iteration++) {
				Character partACharacter = partAFromStack.pollFirst();
				if (partACharacter == null) {
					throw new IllegalStateException("Nothing left on part A stack");
				}
				partAToStack.addFirst(partACharacter);
				Character partBCharacter = partBFromStack.pollFirst();
				if (partBCharacter == null) {
					throw new IllegalStateException("Nothing left on part B stack");
				}
				partBTempStack.addFirst(partBCharacter);
			}
			for (int iteration=1; iteration<=amount; iteration++) {
				partBToStack.addFirst(partBTempStack.removeFirst());
			}
			if (DEBUG) {
				summarizeState(printWriter, "Part A State", partAStacks);
				summarizeState(printWriter, "Part B State", partBStacks);
			}
		}
		return new BasicPuzzleResults<>(
			createResult(partAStacks),
			createResult(partBStacks)
		);
	}
}
