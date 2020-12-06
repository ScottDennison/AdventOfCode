package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class Day05 {
	private static final Pattern[][] RULESETS = {
		{
			Pattern.compile("[aeiou].*[aeiou].*[aeiou]"),
			Pattern.compile("(.)\\1"),
			Pattern.compile("^((?!(ab|cd|pq|xy)).)*$")
		},
		{
			Pattern.compile("(..).*\\1"),
			Pattern.compile("(.).\\1")
		}
	};

	public static void main(String[] args) throws IOException {
		int rulesetCount = RULESETS.length;
		int[] niceStringCounts = new int[rulesetCount];
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			for (int rulesetIndex = 0; rulesetIndex < rulesetCount; rulesetIndex++) {
				boolean isValid = true;
				for (Pattern rule : RULESETS[rulesetIndex]) {
					if (!rule.matcher(fileLine).find()) {
						isValid = false;
						break;
					}
				}
				if (isValid) {
					niceStringCounts[rulesetIndex]++;
				}
			}
		}
		for (int rulesetIndex = 0; rulesetIndex < rulesetCount; rulesetIndex++) {
			System.out.format("Nice string count for ruleset %d: %d%n", rulesetIndex + 1, niceStringCounts[rulesetIndex]);
		}
	}
}
