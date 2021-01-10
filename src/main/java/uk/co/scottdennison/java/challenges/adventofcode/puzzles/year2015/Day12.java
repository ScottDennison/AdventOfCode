package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzlePartResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.MultiPartPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.ResultGetter;

import java.io.IOException;
import java.io.PrintWriter;

public class Day12 implements IPuzzle {
	private static class PartResults implements IPuzzlePartResults {
		private final boolean redSkipped;
		private final long sum;

		public PartResults(boolean redSkipped, long sum) {
			this.redSkipped = redSkipped;
			this.sum = sum;
		}

		@ResultGetter
		public long getSum() {
			return this.sum;
		}

		@Override
		public String getAnswerString() {
			return Long.toString(this.sum);
		}

		@Override
		public String getSummary() {
			return String.format("Result when skipRed=%b: %d", this.redSkipped, this.sum);
		}
	}

	private static long recurse(JsonNode jsonNode, boolean skipRed) {
		boolean isObject;
		if (jsonNode.isArray() || ((isObject = jsonNode.isObject()) && !skipRed)) {
			int sum = 0;
			for (JsonNode childJsonNode : jsonNode) {
				sum += recurse(childJsonNode, skipRed);
			}
			return sum;
		}
		else if (isObject) {
			int sum = 0;
			for (JsonNode childJsonNode : jsonNode) {
				if (childJsonNode.isTextual() && "red".equals(childJsonNode.asText())) {
					sum = 0;
					break;
				}
				sum += recurse(childJsonNode, true);
			}
			return sum;
		}
		else if (jsonNode.isInt() || jsonNode.isLong()) {
			return jsonNode.asLong();
		}
		else {
			return 0;
		}
	}

	@Override
	public MultiPartPuzzleResults<Day12.PartResults> runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter progressWriter) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(new String(inputCharacters));
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to parse JSON");
		}
		return new MultiPartPuzzleResults<>(
			runPuzzlePart(jsonNode, false),
			runPuzzlePart(jsonNode, true)
		);
	}

	private static Day12.PartResults runPuzzlePart(JsonNode jsonNode, boolean skipRed) {
		return new Day12.PartResults(
			skipRed,
			recurse(jsonNode, skipRed)
		);
	}
}
