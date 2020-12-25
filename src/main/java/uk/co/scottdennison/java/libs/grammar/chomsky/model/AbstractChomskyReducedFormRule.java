package uk.co.scottdennison.java.libs.grammar.chomsky.model;

public class AbstractChomskyReducedFormRule<K> implements ChomskyReducedFormRule<K> {
	private final K ruleKey;
	private final boolean temporary;

	protected AbstractChomskyReducedFormRule(K ruleKey, boolean temporary) {
		this.ruleKey = ruleKey;
		this.temporary = temporary;
	}

	@Override
	public K getRuleKey() {
		return this.ruleKey;
	}

	@Override
	public boolean isTemporary() {
		return this.temporary;
	}
}
