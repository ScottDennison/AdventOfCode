package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day08 implements IPuzzle {
	private static final Pattern NODES_PATTERN = Pattern.compile("^(?<name>[0-9A-Z]+) = \\((?<leftName>[0-9A-Z]+), (?<rightName>[0-9A-Z]+)\\)$");

	private static final String PART_A_STARTING_NODE_NAME = "AAA";
	private static final String PART_A_TARGET_NODE_NAME = "ZZZ";

	private static class Node {
		private final String name;
		private final String leftName;
		private final String rightName;

		public Node(String name, String leftName, String rightName) {
			this.name = name;
			this.leftName = leftName;
			this.rightName = rightName;
		}

		public String getName() {
			return name;
		}

		public String getLeftName() {
			return leftName;
		}

		public String getRightName() {
			return rightName;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String[] inputLines = LineReader.stringsArray(inputCharacters, true);
		int inputLineCount = inputLines.length; 
		if (inputLineCount < 3) {
			throw new IllegalStateException("Expected at least 3 lines.");
		}
		if (!inputLines[1].isEmpty()) {
			throw new IllegalStateException("Exected the second line to be empty.");
		}
		char[] directions = inputLines[0].toCharArray();
		Map<String, Node> nodes = new HashMap<>();
		for (int inputLineIndex=2; inputLineIndex<inputLineCount; inputLineIndex++) {
			Matcher matcher = NODES_PATTERN.matcher(inputLines[inputLineIndex]);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to parse input line");
			}
			Node node = new Node(
				matcher.group("name"),
				matcher.group("leftName"),
				matcher.group("rightName")
			);
			if (nodes.put(node.getName(),node) != null) {
				throw new IllegalStateException("Duplicate node");
			}
		}
		return new BasicPuzzleResults<>(
			solvePartA(nodes,directions),
			solvePartB(nodes,directions,partBPotentiallyUnsolvable)
		);
	}

	private static Integer solvePartA(Map<String,Node> nodes, char[] directions) {
		int directionCount = directions.length;
		String currentNodeName = PART_A_STARTING_NODE_NAME;
		if (!nodes.containsKey(currentNodeName)) {
			return null; // The part 3 example does not contain AAA.
		}
		int traversalCount = 0;
		while (!currentNodeName.equals(PART_A_TARGET_NODE_NAME)) {
			Node currentNode = nodes.get(currentNodeName);
			if (currentNode == null) {
				throw new IllegalStateException("No such node");
			}
			switch (directions[(traversalCount++) % directionCount]) {
				case 'L':
					currentNodeName = currentNode.getLeftName();
					break;
				case 'R':
					currentNodeName = currentNode.getRightName();
					break;
				default:
					throw new IllegalStateException("Unexpected direction.");
			}
		}
		return traversalCount;
	}

	private static Long solvePartB(Map<String,Node> nodes, char[] directions, boolean partBPotentiallyUnsolvable) {
		int directionCount = directions.length;
		String[] startingNodeNames = nodes.keySet().stream().filter(name -> name.endsWith("A")).toArray(String[]::new);
		int startingNodeCount = startingNodeNames.length;
		long overallLCM = 1;
		for (int startingNodeIndex=0; startingNodeIndex<startingNodeCount; startingNodeIndex++) {
			Map<Integer,Set<String>> seenNodesAtDirectionIndex = new HashMap<>();
			Deque<Integer> potentialLoopNumbers = new LinkedList<>();
			String currentNodeName = startingNodeNames[startingNodeIndex];
			int traversalCount = 0;
			while (true) {
				Node currentNode = nodes.get(currentNodeName);
				if (currentNode == null) {
					throw new IllegalStateException("No such node");
				}
				int directionIndex = (traversalCount++) % directionCount;
				switch (directions[directionIndex]) {
					case 'L':
						currentNodeName = currentNode.getLeftName();
						break;
					case 'R':
						currentNodeName = currentNode.getRightName();
						break;
					default:
						throw new IllegalStateException("Unexpected direction.");
				}
				if (currentNodeName.endsWith("Z")) {
					potentialLoopNumbers.addLast(traversalCount);
					if (!seenNodesAtDirectionIndex.computeIfAbsent(directionIndex, __ -> new HashSet<>()).add(currentNodeName)) {
						break;
					}
				}
			}
			int firstLoopNumber = potentialLoopNumbers.removeFirst();
			long loopGCD = firstLoopNumber;
			while (true) {
				Integer nextLoopNumber = potentialLoopNumbers.pollFirst();
				if (nextLoopNumber == null) {
					break;
				}
				loopGCD = ExtendedEuclideanAlgorithm.solveForGcdOnly(loopGCD, nextLoopNumber);
			}
			if (loopGCD != firstLoopNumber) {
				if (partBPotentiallyUnsolvable) {
					return null;
				} else {
					// The loop is either in form of (n*x)+y (E.G: [3,7,11,15] aka (n*4)+3) or has a semi-regular pattern (E.G [10,15,30,35,50,55,70,75]). Neither is supported by this solution.
					throw new IllegalStateException("Unsolvable by this solution.");
				}
			}
			overallLCM = (overallLCM * loopGCD) / ExtendedEuclideanAlgorithm.solveForGcdOnly(overallLCM, loopGCD);
		}
		return overallLCM;
	}
}
