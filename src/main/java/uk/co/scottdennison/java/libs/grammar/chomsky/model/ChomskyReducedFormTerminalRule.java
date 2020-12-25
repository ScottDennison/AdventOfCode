package uk.co.scottdennison.java.libs.grammar.chomsky.model;

public final class ChomskyReducedFormTerminalRule<K, V> extends AbstractChomskyReducedFormRule<K> {
	private final V output;

	public ChomskyReducedFormTerminalRule(K ruleKey, boolean temporary, V output) {
		super(ruleKey, temporary);
		this.output = output;
	}

	public V getOutput() {
		return this.output;
	}
}
