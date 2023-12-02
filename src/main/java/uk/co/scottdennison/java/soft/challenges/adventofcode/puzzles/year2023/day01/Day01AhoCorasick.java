package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023.day01;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class Day01AhoCorasick implements IPuzzle {
	// A slimline version of the AhoCorasik algorithm which is memory greedy (using direct array access instead of small arrays with binary search), and only supports a subset of characters.
	private static class AhoCorasickGraph {
		private static final char MAX_ALLOWED_CHARACTER = 0x7F;

		private static interface CharArrayOrderingProvider {
			int getStartIndex(char[] input);
			int getStopIndex(char[] input);
			int getDirection();
		}

		private static class ForwardCharArrayOrderingProvider implements CharArrayOrderingProvider {
			public static final ForwardCharArrayOrderingProvider INSTANCE = new ForwardCharArrayOrderingProvider();

			private ForwardCharArrayOrderingProvider() {}

			@Override
			public int getStartIndex(char[] input) {
				return 0;
			}

			@Override
			public int getStopIndex(char[] input) {
				return input.length;
			}

			@Override
			public int getDirection() {
				return 1;
			}
		}

		private static class ReverseCharArrayOrderingProvider implements CharArrayOrderingProvider {
			public static final ReverseCharArrayOrderingProvider INSTANCE = new ReverseCharArrayOrderingProvider();

			private ReverseCharArrayOrderingProvider() {}

			@Override
			public int getStartIndex(char[] input) {
				return input.length-1;
			}

			@Override
			public int getStopIndex(char[] input) {
				return -1;
			}

			@Override
			public int getDirection() {
				return -1;
			}
		}

		private static class Node {
			private final Node parentNode;
			private final char character;
			private final Node[] childNodesByCharacter;
			private final List<Node> childNodesList;

			private boolean dictionaryNode;
			private int value;
			private Node suffixNode;
			private Node dictionarySuffixNode;

			private Node(Node parentNode, char character) {
				this.parentNode = parentNode;
				this.character = character;
				this.childNodesByCharacter = new Node[MAX_ALLOWED_CHARACTER+1];
				this.childNodesList = new ArrayList<>();
				this.dictionaryNode = false;
			}

			private Node getParentNode() {
				return this.parentNode;
			}

			private char getCharacter() {
				return this.character;
			}

			private Node getChildNodeOrNull(char character) {
				return childNodesByCharacter[character];
			}

			private Node getOrCreateChildNode(char character) {
				Node node = childNodesByCharacter[character];
				if (node == null) {
					node = new Node(this, character);
					childNodesByCharacter[character] = node;
					childNodesList.add(node);
				}
				return node;
			}

			private Node getSuffixNode() {
				return suffixNode;
			}

			private void setSuffixNode(Node suffixNode) {
				this.suffixNode = suffixNode;
			}

			private Node getDictionarySuffixNode() {
				return dictionarySuffixNode;
			}

			private void setDictionarySuffixNode(Node dictionarySuffixNode) {
				this.dictionarySuffixNode = dictionarySuffixNode;
			}

			private boolean isDictionaryNode() {
				return this.dictionaryNode;
			}

			private int getValue() {
				return this.value;
			}

			public void markAsDictionaryNode(int value) {
				this.dictionaryNode = true;
				this.value = value;
			}

			public void forEachChildNode(Consumer<Node> action) {
				this.childNodesList.forEach(action);
			}
		}

		public static class Builder {
			private final CharArrayOrderingProvider charArrayOrderingProvider;
			private final Node rootNode;
			private boolean built;

			private Builder(CharArrayOrderingProvider charArrayOrderingProvider) {
				this.charArrayOrderingProvider = charArrayOrderingProvider;
				this.rootNode = new Node(null, '\0');
				this.built = false;
			}

			public void addEntry(char[] chars, int value) {
				int startIndex = charArrayOrderingProvider.getStartIndex(chars);
				int stopIndex = charArrayOrderingProvider.getStopIndex(chars);
				int direction = charArrayOrderingProvider.getDirection();
				Node node = rootNode;
				for (int index=startIndex; index!=stopIndex; index+=direction) {
					node = node.getOrCreateChildNode(chars[index]);
				}
				node.markAsDictionaryNode(value);
			}

			public AhoCorasickGraph build() {
				if (built) {
					throw new IllegalStateException("Cannot re-build");
				}
				built = true;
				rootNode.setSuffixNode(null);
				Deque<Node> nodesToProcess = new LinkedList<>();
				rootNode.forEachChildNode(
					childNode -> {
						nodesToProcess.addLast(childNode);
						childNode.setSuffixNode(rootNode);
					}
				);
				while (true) {
					Node nodeToProcess = nodesToProcess.pollFirst();
					if (nodeToProcess == null) {
						break;
					}
					char nodeToProcessCharacter = nodeToProcess.getCharacter();
					Node searchNode = nodeToProcess.getParentNode();
					Node suffixNode;
					while (true) {
						Node searchNodeSuffixNode = searchNode.getSuffixNode();
						if (searchNodeSuffixNode == null) {
							suffixNode = rootNode;
							break;
						}
						Node searchNodeSuffixNodeChildNode = searchNodeSuffixNode.getChildNodeOrNull(nodeToProcessCharacter);
						if (searchNodeSuffixNodeChildNode == null) {
							searchNode = searchNodeSuffixNode;
						}
						else {
							suffixNode = searchNodeSuffixNodeChildNode;
							break;
						}
					}
					searchNode = suffixNode;
					Node dictionarySuffixNode;
					while (true) {
						if (searchNode.isDictionaryNode()) {
							dictionarySuffixNode = searchNode;
							break;
						}
						searchNode = searchNode.getSuffixNode();
						if (searchNode == null) {
							dictionarySuffixNode = null;
							break;
						}
					}
					nodeToProcess.setSuffixNode(suffixNode);
					nodeToProcess.setDictionarySuffixNode(dictionarySuffixNode);
					nodeToProcess.forEachChildNode(
						childNode -> {
							nodesToProcess.addLast(childNode);
						}
					);
				}
				return new AhoCorasickGraph(charArrayOrderingProvider, rootNode);
			}
		}

		private final CharArrayOrderingProvider charArrayOrderingProvider;
		private final Node rootNode;

		private AhoCorasickGraph(CharArrayOrderingProvider charArrayOrderingProvider, Node rootNode) {
			this.charArrayOrderingProvider = charArrayOrderingProvider;
			this.rootNode = rootNode;
		}

		public static Builder builder(boolean reverseInput) {
			return new Builder(reverseInput?ReverseCharArrayOrderingProvider.INSTANCE:ForwardCharArrayOrderingProvider.INSTANCE);
		}

		public OptionalInt find(char[] chars) {
			int startIndex = charArrayOrderingProvider.getStartIndex(chars);
			int stopIndex = charArrayOrderingProvider.getStopIndex(chars);
			int direction = charArrayOrderingProvider.getDirection();
			boolean hadResult = false;
			int result = -1;
			Node currentNode = rootNode;
			for (int index=startIndex; index!=stopIndex && !hadResult; index+=direction) {
				while (true) {
					Node childNode = currentNode.getChildNodeOrNull(chars[index]);
					if (childNode == null) {
						currentNode = currentNode.getSuffixNode();
						if (currentNode == null) {
							currentNode = rootNode;
							break;
						}
					}
					else {
						currentNode = childNode;
						break;
					}
				}
				Node resultNode = currentNode;
				while (resultNode != null) {
					if (resultNode.isDictionaryNode()) {
						if (hadResult) {
							throw new IllegalStateException("Multiple results found at index " + index);
						}
						hadResult = true;
						result = resultNode.getValue();
					}
					resultNode = resultNode.getDictionarySuffixNode();
				}
			}
			if (hadResult) {
				return OptionalInt.of(result);
			}
			else {
				return OptionalInt.empty();
			}
		}
	}

	private static final class TextualDigit {
		private final String digitSpeltOutString;
		private final String digitRawString;
		private final int value;

		public TextualDigit(String digitSpeltOutString, String digitRawString, int value) {
			this.digitSpeltOutString = digitSpeltOutString;
			this.digitRawString = digitRawString;
			this.value = value;
		}

		public String getDigitSpeltOutString() {
			return digitSpeltOutString;
		}

		public String getDigitRawString() {
			return digitRawString;
		}

		public int getValue() {
			return value;
		}

		public static TextualDigit create(String digitSpeltOut, int value) {
			return new TextualDigit(digitSpeltOut, Integer.toString(value), value);
		}
	}

	private static final TextualDigit[] TEXTUAL_DIGITS = {
		TextualDigit.create("one", 1),
		TextualDigit.create("two", 2),
		TextualDigit.create("three", 3),
		TextualDigit.create("four", 4),
		TextualDigit.create("five", 5),
		TextualDigit.create("six", 6),
		TextualDigit.create("seven", 7),
		TextualDigit.create("eight", 8),
		TextualDigit.create("nine", 9)
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		return new BasicPuzzleResults<>(
			solve(inputCharacters, false),
			solve(inputCharacters, true)
		);
	}

	private static int solve(char[] inputCharacters, boolean useSpeltOutDigits) {
		AhoCorasickGraph forwardSearch;
		AhoCorasickGraph reverseSearch;
		AhoCorasickGraph.Builder forwardSearchBuilder = AhoCorasickGraph.builder(false);
		AhoCorasickGraph.Builder reverseSearchBuilder = AhoCorasickGraph.builder(true);
		AhoCorasickGraph.Builder[] builders = {forwardSearchBuilder,reverseSearchBuilder};
		for (TextualDigit textualDigit : TEXTUAL_DIGITS) {
			int value = textualDigit.getValue();
			char[] digitRawStringCharArray = textualDigit.getDigitRawString().toCharArray();
			for (AhoCorasickGraph.Builder builder : builders) {
				builder.addEntry(digitRawStringCharArray, value);
			}
			if (useSpeltOutDigits) {
				char[] digitSpeltOutStringCharArray = textualDigit.getDigitSpeltOutString().toCharArray();
				for (AhoCorasickGraph.Builder builder : builders) {
					builder.addEntry(digitSpeltOutStringCharArray, value);
				}
			}
		}
		forwardSearch = forwardSearchBuilder.build();
		reverseSearch = reverseSearchBuilder.build();
		int sum = 0;
		for (char[] inputLine : LineReader.charArrays(inputCharacters)) {
			OptionalInt firstDigit = forwardSearch.find(inputLine);
			boolean firstDigitPresent = firstDigit.isPresent();
			OptionalInt lastDigit = reverseSearch.find(inputLine);
			boolean lastDigitPresent = lastDigit.isPresent();
			if (firstDigitPresent && lastDigitPresent) {
				sum += (firstDigit.getAsInt() * 10) + lastDigit.getAsInt();
			}
			else if (firstDigitPresent || lastDigitPresent) {
				throw new IllegalStateException("Somehow found one of a first digit/last digit but not the other");
			}
		}
		return sum;
	}
}
