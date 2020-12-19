package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day19 {
	private static final Pattern PATTERN_RULE = Pattern.compile("^(?<ruleNumber>[0-9]+): (?<ruleComponents>.+)$");
	private static final Pattern PATTERN_RULE_COMPONENT_SPLIT = Pattern.compile(" ");
	private static final Pattern PATTERN_RULE_COMPONENT = Pattern.compile("(?<otherRuleNumber>[0-9]+)|(?<or>\\|)|(?:\"(?<literal>.+?)\")");

	private static final class OrRuleComponent implements RuleComponent {
		@Override
		public String buildRegularExpression(RuleManager ruleManager) {
			return "|";
		}
	}

	private static final class LiteralRuleComponent implements RuleComponent {
		private final String literalText;


		private LiteralRuleComponent(String literalText) {
			this.literalText = literalText;
		}

		@Override
		public String buildRegularExpression(RuleManager ruleManager) {
			return this.literalText;
		}
	}

	private static final class OtherRuleRuleComponent implements RuleComponent {
		private final int ruleNumber;

		public OtherRuleRuleComponent(int ruleNumber) {
			this.ruleNumber = ruleNumber;
		}

		@Override
		public String buildRegularExpression(RuleManager ruleManager) {
			return ruleManager.getRule(ruleNumber).buildRegularExpression(ruleManager);
		}
	}

	private interface RuleComponent {
		String buildRegularExpression(RuleManager ruleManager);
	}

	private static final class Rule {
		private final List<RuleComponent> ruleComponents;

		private Rule(List<RuleComponent> ruleComponents) {
			this.ruleComponents = new ArrayList<>(ruleComponents);
		}

		public String buildRegularExpression(RuleManager ruleManager) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append('(');
			for (RuleComponent ruleComponent : this.ruleComponents) {
				stringBuilder.append(ruleComponent.buildRegularExpression(ruleManager));
			}
			stringBuilder.append(')');
			return stringBuilder.toString();
		}
	}

	private static final class RuleManager {
		private final Map<Integer, Rule> rules = new HashMap<>();

		public void addRule(int ruleNumber, Rule rule) {
			if (this.rules.put(ruleNumber, rule) != null) {
				throw new IllegalStateException("Duplicate rule");
			}
		}

		public Rule getRule(int ruleNumber) {
			Rule rule = this.rules.get(ruleNumber);
			if (rule == null) {
				throw new IllegalStateException("No such rule");
			}
			return rule;
		}
	}

	public static void main(String[] args) throws IOException {
		boolean parsingRules = true;
		RuleManager ruleManager = new RuleManager();
		Pattern rule0Pattern = null;
		int matchCount = 0;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			if (parsingRules) {
				if (fileLine.isEmpty()) {
					parsingRules = false;
					rule0Pattern = Pattern.compile(ruleManager.getRule(0).buildRegularExpression(ruleManager));
				}
				else {
					Matcher ruleMatcher = PATTERN_RULE.matcher(fileLine);
					if (!ruleMatcher.matches()) {
						throw new IllegalStateException("Could not parse rule");
					}
					List<RuleComponent> ruleComponents = new ArrayList<>();
					for (String ruleComponentString : PATTERN_RULE_COMPONENT_SPLIT.split(ruleMatcher.group("ruleComponents"))) {
						Matcher ruleComponentMatcher = PATTERN_RULE_COMPONENT.matcher(ruleComponentString);
						if (!ruleComponentMatcher.matches()) {
							throw new IllegalStateException("Could not parse rule component");
						}
						String otherRuleNumberString = ruleComponentMatcher.group("otherRuleNumber");
						RuleComponent ruleComponent;
						if (otherRuleNumberString != null) {
							ruleComponent = new OtherRuleRuleComponent(Integer.parseInt(otherRuleNumberString));
						}
						else {
							String orString = ruleComponentMatcher.group("or");
							if (orString != null) {
								ruleComponent = new OrRuleComponent();
							}
							else {
								String literalString = ruleComponentMatcher.group("literal");
								if (literalString != null) {
									ruleComponent = new LiteralRuleComponent(literalString);
								}
								else {
									throw new IllegalStateException("Could not extract rule component data");
								}
							}
						}
						ruleComponents.add(ruleComponent);
					}
					ruleManager.addRule(Integer.parseInt(ruleMatcher.group("ruleNumber")), new Rule(ruleComponents));
				}
			}
			else {
				System.out.println(fileLine);
				if (!fileLine.isEmpty() && rule0Pattern.matcher(fileLine).matches()) {
					System.out.println("\tMatches");
					matchCount++;
				}
			}
		}
		System.out.format("Match count: %d%n", matchCount);
	}
}
