package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 {
	private static final Pattern PATTERN_FILE = Pattern.compile("^\\s*(?<rules>(?:.+\\n)*.+)\\n{2,}your ticket *: *\\n(?<myTicket>.+)\\n{2,}nearby tickets *: *\\n(?<nearbyTickets>(?:.+\\n)*.+)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_NEWLINE = Pattern.compile("\\n");
	private static final Pattern PATTERN_SUB_RULE = Pattern.compile("(?<min>[0-9]+) *- *(?<max>[0-9]+)(?<continuation> *(?:(?: or )|,) *)?", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_RULE = Pattern.compile("^(?<name>[a-z ]+?) *: *(?<subRules>.*)$");
	private static final Pattern PATTERN_COMMA = Pattern.compile(",");

	private static final Pattern PATTERN_INTERESTED_RULE_NAME = Pattern.compile("^departure .*$", Pattern.CASE_INSENSITIVE);

	private static final class Rule {
		private final String name;
		private final SubRule[] subRules;

		public Rule(String name, SubRule... subRules) {
			this.name = name;
			this.subRules = Arrays.copyOf(subRules, subRules.length);
		}

		public String getName() {
			return this.name;
		}

		public boolean test(int value) {
			for (SubRule subRule : this.subRules) {
				if (subRule.test(value)) {
					return true;
				}
			}
			return false;
		}
	}

	private static final class SubRule {
		private final int min;
		private final int max;

		public SubRule(int min, int max) {
			this.min = min;
			this.max = max;
		}

		public boolean test(int value) {
			return value >= this.min && value <= this.max;
		}
	}

	public static void main(String[] args) throws IOException {
		Matcher fileMatcher = PATTERN_FILE.matcher(new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8));
		if (!fileMatcher.matches()) {
			throw new IllegalStateException("Unparseable file");
		}
		List<Rule> rulesList = new ArrayList<>();
		for (String ruleString : PATTERN_NEWLINE.split(fileMatcher.group("rules"))) {
			Matcher ruleMatcher = PATTERN_RULE.matcher(ruleString);
			if (!ruleMatcher.matches()) {
				throw new IllegalStateException("Unparseable rule");
			}
			String subRulesString = ruleMatcher.group("subRules");
			int subRulesStringLength = subRulesString.length();
			Matcher subRuleMatcher = PATTERN_SUB_RULE.matcher(subRulesString);
			List<SubRule> subRules = new ArrayList<>();
			while (true) {
				if (!subRuleMatcher.lookingAt()) {
					throw new IllegalStateException("Unparseable sub rule");
				}
				int min = Integer.parseInt(subRuleMatcher.group("min"));
				int max = Integer.parseInt(subRuleMatcher.group("max"));
				if (max < min) {
					throw new IllegalStateException("Wrong way around sub rule");
				}
				subRules.add(new SubRule(min, max));
				if (subRuleMatcher.group("continuation") == null) {
					if (subRuleMatcher.hitEnd()) {
						break;
					}
					else {
						throw new IllegalStateException("Expected to hit end if no sub rule continuation");
					}
				}
				else if (subRuleMatcher.hitEnd()) {
					throw new IllegalStateException("Did not expect to hit end of sub rule after a continuation");
				}
				subRuleMatcher.region(subRuleMatcher.end(), subRulesStringLength);
			}
			rulesList.add(new Rule(ruleMatcher.group("name"), subRules.toArray(new SubRule[0])));
		}
		Rule[] rulesArray = rulesList.toArray(new Rule[0]);
		int ruleCount = rulesArray.length;
		int totalErrorRate = 0;
		List<int[]> ticketsList = new ArrayList<>();
		int fieldCount = -1;
		for (String nearbyTicketString : PATTERN_NEWLINE.split(fileMatcher.group("nearbyTickets"))) {
			int[] nearbyTicket = parseTicket(nearbyTicketString);
			if (fieldCount < 0) {
				fieldCount = nearbyTicket.length;
			}
			Integer errorRate = validateTicket(rulesArray, fieldCount, nearbyTicket);
			if (errorRate == null) {
				ticketsList.add(nearbyTicket);
			}
			else {
				totalErrorRate += errorRate;
			}
		}
		int[] myTicket = parseTicket(fileMatcher.group("myTicket"));
		if (validateTicket(rulesArray, fieldCount, myTicket) != null) {
			throw new IllegalStateException("My ticket must be valid");
		}
		ticketsList.add(myTicket);
		int[][] ticketsArray = ticketsList.toArray(new int[0][]);
		int[][] possibleFieldRuleIds = new int[fieldCount][];
		Set<Integer> allRuleIds = new HashSet<>();
		for (int ruleId = 0; ruleId < ruleCount; ruleId++) {
			allRuleIds.add(ruleId);
		}
		for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
			Set<Integer> possibleRuleIds = new HashSet<>(allRuleIds);
			for (int[] ticket : ticketsArray) {
				int field = ticket[fieldIndex];
				possibleRuleIds.removeIf(integer -> !rulesArray[integer].test(field));
				if (possibleRuleIds.isEmpty()) {
					throw new IllegalStateException("No possible rules match field " + fieldIndex);
				}
			}
			int[] possibleRuleIdsArray = new int[possibleRuleIds.size()];
			int possibleRuleIdIndex = 0;
			for (int possibleRuleId : possibleRuleIds) {
				possibleRuleIdsArray[possibleRuleIdIndex++] = possibleRuleId;
			}
			possibleFieldRuleIds[fieldIndex] = possibleRuleIdsArray;
		}
		int[] assignedFieldRuleIds = assignFieldRuleIds(possibleFieldRuleIds, new boolean[ruleCount], new int[fieldCount], 0);
		if (assignedFieldRuleIds == null) {
			throw new IllegalStateException("No valid field rule ids");
		}
		long myTicketInterestedFieldProduct = 1;
		for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
			if (PATTERN_INTERESTED_RULE_NAME.matcher(rulesArray[assignedFieldRuleIds[fieldIndex]].getName()).matches()) {
				myTicketInterestedFieldProduct *= myTicket[fieldIndex];
			}
		}
		System.out.format("Error rate is %d%n", totalErrorRate);
		System.out.format("The product of all interesting fields on my ticket is %d%n", myTicketInterestedFieldProduct);
	}

	private static int[] assignFieldRuleIds(int[][] possibleFieldRuleIds, boolean[] rulesAssigned, int[] currentFieldRuleIds, int fieldIndex) {
		if (fieldIndex >= possibleFieldRuleIds.length) {
			return Arrays.copyOf(currentFieldRuleIds, currentFieldRuleIds.length);
		}
		int[] possibleRuleIds = possibleFieldRuleIds[fieldIndex];
		int nextFieldIndex = fieldIndex + 1;
		int[] assignedFieldRuleIds = null;
		for (int ruleId : possibleRuleIds) {
			if (!rulesAssigned[ruleId]) {
				currentFieldRuleIds[fieldIndex] = ruleId;
				rulesAssigned[ruleId] = true;
				int[] possibleAssignedFieldRuleIds = assignFieldRuleIds(possibleFieldRuleIds, rulesAssigned, currentFieldRuleIds, nextFieldIndex);
				if (possibleAssignedFieldRuleIds != null) {
					if (assignedFieldRuleIds == null) {
						assignedFieldRuleIds = possibleAssignedFieldRuleIds;
					}
					else {
						throw new IllegalStateException("Multiple possible rule assignments: " + Arrays.toString(assignedFieldRuleIds) + " and " + Arrays.toString(possibleAssignedFieldRuleIds));
					}
				}
				rulesAssigned[ruleId] = false;
			}
		}
		return assignedFieldRuleIds;
	}

	private static int[] parseTicket(String ticketString) {
		String[] fieldStrings = PATTERN_COMMA.split(ticketString);
		int fieldCount = fieldStrings.length;
		int[] fields = new int[fieldCount];
		for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
			fields[fieldIndex] = Integer.parseInt(fieldStrings[fieldIndex]);
		}
		return fields;
	}

	private static Integer validateTicket(Rule[] rules, int expectedFieldCount, int[] fields) {
		if (fields.length != expectedFieldCount) {
			throw new IllegalStateException("Unexpected field count");
		}
		int errorRate = 0;
		boolean hadError = false;
		for (int field : fields) {
			boolean ruleMatched = false;
			for (Rule rule : rules) {
				if (rule.test(field)) {
					ruleMatched = true;
					break;
				}
			}
			if (!ruleMatched) {
				hadError = true;
				errorRate += field;
			}
		}
		return hadError ? errorRate : null;
	}
}
