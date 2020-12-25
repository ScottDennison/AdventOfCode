package uk.co.scottdennison.java.libs.grammar.chomsky.transformation;

import java.util.HashMap;
import java.util.Map;

final class ChomskyReducedFormBuildingRuleSymbol {
	private final boolean terminal;
	private final int value;

	public ChomskyReducedFormBuildingRuleSymbol(boolean terminal, int value) {
		this.terminal = terminal;
		this.value = value;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public int getValue() {
		return value;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject) {
			return true;
		}
		if (otherObject == null || otherObject.getClass() != ChomskyReducedFormBuildingRuleSymbol.class) {
			return false;
		}
		ChomskyReducedFormBuildingRuleSymbol otherChomskyReducedFormBuildingRuleSymbol = (ChomskyReducedFormBuildingRuleSymbol) otherObject;
		return this.terminal == otherChomskyReducedFormBuildingRuleSymbol.terminal && this.value == otherChomskyReducedFormBuildingRuleSymbol.value;
	}

	public static ChomskyReducedFormBuildingRuleSymbol createUsingCache(int item, boolean terminal, Map<Boolean, Map<Integer, ChomskyReducedFormBuildingRuleSymbol>> symbolsCache) {
		return symbolsCache
			.computeIfAbsent(terminal, k -> new HashMap<>())
			.computeIfAbsent(item, k -> new ChomskyReducedFormBuildingRuleSymbol(terminal, k));
	}

	@Override
	public int hashCode() {
		return 31 * (this.terminal ? 1 : 0) + value;
	}
}
