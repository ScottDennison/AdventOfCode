package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day12 implements IPuzzle {
	private static final Pattern PATTERN_LINE = Pattern.compile("^(?<springs>[?#.]+) (?<groups>(?:[0-9]+)(?:(?:,(?:[0-9]+))*))$");
	private static final Pattern PATTERN_COMMA = Pattern.compile(",");

	private static final int REPTITIONS = 5;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		long partASum = 0;
		long partBSum = 0;
		int lineNumber = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN_LINE.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line");
			}
			lineNumber++;

			int[] unexpandedGroups = PATTERN_COMMA.splitAsStream(matcher.group("groups")).mapToInt(Integer::parseInt).toArray();
			int unexpandedGroupsCount = unexpandedGroups.length;

			char[] unexpandedSprings = matcher.group("springs").toCharArray();
			int unexpandedSpringsCount = unexpandedSprings.length;

			int[] expandedGroups = new int[unexpandedGroupsCount*REPTITIONS];
			char[] expandedSprings = new char[unexpandedSpringsCount*REPTITIONS+REPTITIONS-1];

			for (int reptitionIndex=0; reptitionIndex<REPTITIONS; reptitionIndex++) {
				if (reptitionIndex > 0) {
					expandedSprings[reptitionIndex*(unexpandedSpringsCount+1)-1] = '?';
				}
				System.arraycopy(unexpandedGroups,0,expandedGroups,reptitionIndex*unexpandedGroupsCount,unexpandedGroupsCount);
				System.arraycopy(unexpandedSprings,0,expandedSprings,reptitionIndex*(unexpandedSpringsCount+1),unexpandedSpringsCount);
			}

			partASum += recurse(unexpandedSprings, unexpandedGroups);
			partBSum += recurse(expandedSprings, expandedGroups);
		}
		return new BasicPuzzleResults<>(
			partASum,
			partBSum
		);
	}

	private static long recurse(char[] springs, int[] groups) {
		int springsCount = springs.length;
		int springsWithStopCharCount = springsCount+1;
		char[] springsWithStopChar = new char[springsWithStopCharCount];
		System.arraycopy(springs, 0, springsWithStopChar, 0, springsCount);
		springsWithStopChar[springsCount] = '!';
		int groupCount = groups.length;
		long[][] possibilityCounts = new long[groupCount+1][springsWithStopCharCount+1];
		for (int groupIndex=0; groupIndex<=groupCount; groupIndex++) {
			Arrays.fill(possibilityCounts[groupIndex],0,springsWithStopCharCount,-1);
		}
		return recurseDecide(possibilityCounts,springsWithStopChar,groups,0,0,groupCount);
	}

	private static long recurseDecide(long[][] possibilityCounts, char[] springs, int[] groups, int springIndex, int groupIndex, int groupCount) {
		long possibilityCount = possibilityCounts[groupIndex][springIndex];
		if (possibilityCount == -1) {
			int nextSpringIndex = springIndex+1;
			switch (springs[springIndex]) {
				case '#':
					possibilityCount = recurseBroken(possibilityCounts, springs, groups, nextSpringIndex, groupIndex, groupCount);
					break;
				case '.':
					possibilityCount = recurseWorking(possibilityCounts, springs, groups, nextSpringIndex, groupIndex, groupCount);
					break;
				case '?':
					possibilityCount = recurseUnknown(possibilityCounts, springs, groups, nextSpringIndex, groupIndex, groupCount);
					break;
				case '!':
					possibilityCount = 0;
			}
			possibilityCounts[groupIndex][springIndex] = possibilityCount;
		}
		return possibilityCount;
	}

	private static long recurseBroken(long[][] possibilityCounts, char[] springs, int[] groups, int springIndex, int groupIndex, int groupCount) {
		int groupLength = groups[groupIndex++];
		for (int groupRelativeSpringIndex=1; groupRelativeSpringIndex<groupLength; groupRelativeSpringIndex++) {
			switch (springs[springIndex++]) {
				case '#':
				case '?':
					break;
				case '.':
				case '!':
					return 0;
			}
		}
		if (groupIndex >= groupCount) {
			while (true) {
				switch (springs[springIndex++]) {
					case '.':
					case '?':
						break;
					case '!':
						return 1;
					case '#':
						return 0;
				}
			}
		}
		switch (springs[springIndex++]) {
			case '.':
			case '?':
				break;
			case '#':
			case '!':
				return 0;
		}
		return recurseDecide(possibilityCounts, springs, groups, springIndex, groupIndex, groupCount);
	}

	private static long recurseWorking(long[][] possibilityCounts, char[] springs, int[] groups, int springIndex, int groupIndex, int groupCount) {
		while (true) {
			switch (springs[springIndex++]) {
				case '.':
					break;
				case '!':
					return 0;
				case '#':
				case '?':
					return recurseDecide(possibilityCounts, springs, groups, springIndex-1, groupIndex, groupCount);
			}
		}
	}

	private static long recurseUnknown(long[][] possibilityCounts, char[] springs, int[] groups, int springIndex, int groupIndex, int groupCount) {
		return
			recurseBroken(possibilityCounts, springs, groups, springIndex, groupIndex, groupCount)
			+
			recurseWorking(possibilityCounts, springs, groups, springIndex, groupIndex, groupCount);
	}
}
