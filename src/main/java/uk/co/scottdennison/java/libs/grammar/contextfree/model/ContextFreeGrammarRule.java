package uk.co.scottdennison.java.libs.grammar.contextfree.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class ContextFreeGrammarRule<T> {
	private final T leftSide;
	private final Collection<ContextFreeGrammarSubRule<T>> subRules;

	public ContextFreeGrammarRule(T leftSide, Collection<ContextFreeGrammarSubRule<T>> subRules) {
		this.leftSide = leftSide;
		this.subRules = new ArrayList<>(subRules);
	}

	public T getLeftSide() {
		return this.leftSide;
	}

	public Collection<ContextFreeGrammarSubRule<T>> getSubRules() {
		return Collections.unmodifiableCollection(this.subRules);
	}
}
