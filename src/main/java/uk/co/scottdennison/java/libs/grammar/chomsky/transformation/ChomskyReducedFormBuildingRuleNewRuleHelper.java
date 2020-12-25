package uk.co.scottdennison.java.libs.grammar.chomsky.transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class ChomskyReducedFormBuildingRuleNewRuleHelper {
	private final Set<Integer> reservedRuleNumbers;
	private final Set<Integer> generatedRuleNumbers = new HashSet<>();
	private final List<ChomskyReducedFormBuildingRule> rules = new ArrayList<>();
	private final Map<Boolean, Map<Integer, ChomskyReducedFormBuildingRuleSymbol>> symbolCache = new HashMap<>();
	private int nextRuleNumber = 0;

	public ChomskyReducedFormBuildingRuleNewRuleHelper(Set<Integer> reservedRuleNumbers) {
		this.reservedRuleNumbers = new HashSet<>(reservedRuleNumbers);
	}

	public static ChomskyReducedFormBuildingRuleNewRuleHelper createWithReservedRuleNumbersFromExistingRules(Collection<ChomskyReducedFormBuildingRule> existingRules) {
		return new ChomskyReducedFormBuildingRuleNewRuleHelper(
			existingRules
				.stream()
				.map(ChomskyReducedFormBuildingRule::getLeftSide)
				.collect(Collectors.toSet())
		);
	}

	public void addRule(ChomskyReducedFormBuildingRule rule) {
		int leftSide = rule.getLeftSide();
		if (generatedRuleNumbers.contains(leftSide)) {
			throw new IllegalStateException("Rule left side was already used for a generated rule.");
		}
		this.reservedRuleNumbers.add(leftSide);
		this.rules.add(rule);
	}

	public void addRuleWithNewRightSide(ChomskyReducedFormBuildingRule existingRule, List<ChomskyReducedFormBuildingRuleSymbol> newRightSide) {
		this.addRule(
			new ChomskyReducedFormBuildingRule(
				existingRule.getLeftSide(),
				newRightSide,
				existingRule.isTemporary()
			)
		);
	}

	public ChomskyReducedFormBuildingRuleSymbol createTemporaryRuleAndReturnReferencingSymbol(List<ChomskyReducedFormBuildingRuleSymbol> rightSide) {
		int newRuleNumber = this.nextRuleNumber;
		while (this.reservedRuleNumbers.contains(newRuleNumber)) {
			newRuleNumber++;
		}
		this.nextRuleNumber = newRuleNumber + 1;
		this.generatedRuleNumbers.add(newRuleNumber);
		this.rules.add(
			new ChomskyReducedFormBuildingRule(
				newRuleNumber,
				rightSide,
				true
			)
		);
		return ChomskyReducedFormBuildingRuleSymbol.createUsingCache(newRuleNumber, false, this.symbolCache);
	}

	public List<ChomskyReducedFormBuildingRule> getRulesCopy() {
		return new ArrayList<>(this.rules);
	}
}
