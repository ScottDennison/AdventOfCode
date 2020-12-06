package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 {
	private static final Pattern PATTERN = Pattern.compile("^([0-9]+)x([0-9]+)x([0-9]+)");

	public static void main(String[] args) throws IOException {
		int requiredPaper = 0;
		int requiredRibbon = 0;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(fileLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparseable line");
			}
			int length = Integer.parseInt(matcher.group(1));
			int width = Integer.parseInt(matcher.group(2));
			int height = Integer.parseInt(matcher.group(3));
			int area1 = length * width;
			int area2 = width * height;
			int area3 = height * length;
			requiredPaper += (2 * area1) + (2 * area2) + (2 * area3) + Math.min(Math.min(area1, area2), area3);
			requiredRibbon += (length * width * height) + (2 * length) + (2 * width) + (2 * height) - (2 * Math.max(length, Math.max(width, height)));
		}
		System.out.format("Square feet of paper  to be ordered: %d%n", requiredPaper);
		System.out.format("       Feet of ribbon to be ordered: %d%n", requiredRibbon);
	}
}
