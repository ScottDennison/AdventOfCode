package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day07 {
	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		outputResult("TLS", fileLines, Pattern.compile("(?:(?!^.*\\[[a-z]*([a-z])((?!\\1)[a-z])\\2\\1[a-z]*].*$)^.*([a-z])((?!\\3)[a-z])\\4\\3.*$)"));
		outputResult("SSL", fileLines, Pattern.compile("(?:^.*][a-z]*([a-z])((?!\\1)[a-z])\\1[a-z]*\\[(?:.*\\[)?[a-z]*\\2\\1\\2[a-z]*].*$)|(?:^.*\\[[a-z]*([a-z])((?!\\3)[a-z])\\3[a-z]*](?:.*])?[a-z]*\\4\\3\\4[a-z]*\\[.*$)"));
	}

	private static void outputResult(String ipv7Requirement, List<String> fileLines, Pattern pattern) {
		System.out.format("Count of IPV7 addresses that support %s: %d%n", ipv7Requirement, fileLines.stream().map(String::trim).map(fileLine -> "]" + fileLine + "[").map(pattern::matcher).filter(Matcher::matches).count());
	}
}
