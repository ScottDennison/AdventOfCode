package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;

public class Day12 {
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

	public static void main(String[] args) throws IOException {
		JsonNode jsonNode = new ObjectMapper().readTree(Files.newBufferedReader(InputFileUtils.getInputPath()));
		outputSummary(jsonNode, false);
		outputSummary(jsonNode, true);
	}

	private static void outputSummary(JsonNode jsonNode, boolean skipRed) {
		System.out.format("Result when skipRed=%b: %d%n", skipRed, recurse(jsonNode, skipRed));
	}
}
