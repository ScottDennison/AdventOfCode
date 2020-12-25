package uk.co.scottdennison.java.libs.grammar.chomsky.model;

public final class ChomskyReducedFormNonTerminalRule<K> extends AbstractChomskyReducedFormRule<K> {
	private final K leftChildRuleKey;
	private final K rightChildRuleKey;

	public ChomskyReducedFormNonTerminalRule(K ruleKey, boolean temporary, K leftChildRuleKey, K rightChildRuleKey) {
		super(ruleKey, temporary);
		this.leftChildRuleKey = leftChildRuleKey;
		this.rightChildRuleKey = rightChildRuleKey;
	}

	public K getLeftChildRuleKey() {
		return this.leftChildRuleKey;
	}

	public K getRightChildRuleKey() {
		return this.rightChildRuleKey;
	}
}
