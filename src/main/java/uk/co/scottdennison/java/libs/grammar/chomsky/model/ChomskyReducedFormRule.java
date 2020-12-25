package uk.co.scottdennison.java.libs.grammar.chomsky.model;

public interface ChomskyReducedFormRule<K> {
	K getRuleKey();

	boolean isTemporary();
}
