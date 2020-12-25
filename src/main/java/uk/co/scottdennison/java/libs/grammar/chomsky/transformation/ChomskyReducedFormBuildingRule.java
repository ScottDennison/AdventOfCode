package uk.co.scottdennison.java.libs.grammar.chomsky.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ChomskyReducedFormBuildingRule {
	private final int leftSide;
	private final List<ChomskyReducedFormBuildingRuleSymbol> rightSide;
	private final boolean temporary;

	public ChomskyReducedFormBuildingRule(int leftSide, List<ChomskyReducedFormBuildingRuleSymbol> rightSide, boolean temporary) {
		this.leftSide = leftSide;
		this.rightSide = new ArrayList<>(rightSide);
		this.temporary = temporary;
	}

	public int getLeftSide() {
		return this.leftSide;
	}

	public List<ChomskyReducedFormBuildingRuleSymbol> getRightSide() {
		return Collections.unmodifiableList(this.rightSide);
	}

	public boolean isTemporary() {
		return this.temporary;
	}
}
