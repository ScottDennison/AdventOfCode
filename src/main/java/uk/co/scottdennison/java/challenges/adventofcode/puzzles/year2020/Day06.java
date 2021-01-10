package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Day06 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int yesAnswersForOne = 0;
		int yesAnswersForAll = 0;
		Set<Character> yesAnswerQuestionsForOneInGroup = new TreeSet<>();
		Set<Character> yesAnswerQuestionsForAllInGroup = new TreeSet<>();
		Set<Character> yesAnswerQuestionsForPerson = new TreeSet<>();
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		inputLines.add("");
		boolean isFirstEntryForGroup = true;
		for (String inputLine : inputLines) {
			inputLine = inputLine.trim();
			if (inputLine.isEmpty()) {
				yesAnswersForOne += yesAnswerQuestionsForOneInGroup.size();
				yesAnswersForAll += yesAnswerQuestionsForAllInGroup.size();
				yesAnswerQuestionsForOneInGroup.clear();
				yesAnswerQuestionsForAllInGroup.clear();
				isFirstEntryForGroup = true;
			}
			else {
				char[] characters = inputLine.toCharArray();
				yesAnswerQuestionsForPerson.clear();
				for (char character : characters) {
					yesAnswerQuestionsForPerson.add(character);
				}
				yesAnswerQuestionsForOneInGroup.addAll(yesAnswerQuestionsForPerson);
				if (isFirstEntryForGroup) {
					yesAnswerQuestionsForAllInGroup.addAll(yesAnswerQuestionsForPerson);
					isFirstEntryForGroup = false;
				}
				else {
					yesAnswerQuestionsForAllInGroup.retainAll(yesAnswerQuestionsForPerson);
				}
			}
		}
		return new BasicPuzzleResults<>(
			yesAnswersForOne,
			yesAnswersForAll
		);
	}
}
