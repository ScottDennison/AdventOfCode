package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Day06 {
	public static void main(String[] args) throws IOException {
		int yesAnswersForOne = 0;
		int yesAnswersForAll = 0;
		Set<Character> yesAnswerQuestionsForOneInGroup = new TreeSet<>();
		Set<Character> yesAnswerQuestionsForAllInGroup = new TreeSet<>();
		Set<Character> yesAnswerQuestionsForPerson = new TreeSet<>();
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		fileLines.add("");
		boolean isFirstEntryForGroup = true;
		for (String fileLine : fileLines) {
			fileLine = fileLine.trim();
			if (fileLine.isEmpty()) {
				yesAnswersForOne += yesAnswerQuestionsForOneInGroup.size();
				yesAnswersForAll += yesAnswerQuestionsForAllInGroup.size();
				yesAnswerQuestionsForOneInGroup.clear();
				yesAnswerQuestionsForAllInGroup.clear();
				isFirstEntryForGroup = true;
			}
			else {
				char[] characters = fileLine.toCharArray();
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
		outputSummary("one person", yesAnswersForOne);
		outputSummary("all people", yesAnswersForAll);
	}

	private static void outputSummary(String type, int count) {
		System.out.format("Yes answers for %s in each group: %d%n", type, count);
	}
}
