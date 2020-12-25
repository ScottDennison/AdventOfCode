package uk.co.scottdennison.java.libs.grammar.contextfree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContextFreeGrammarSubRule<T> {
	private final List<ContextFreeGrammarSymbol<T>> symbols;

	public ContextFreeGrammarSubRule(List<ContextFreeGrammarSymbol<T>> symbols) {
		this.symbols = new ArrayList<>(symbols);
	}

	public List<ContextFreeGrammarSymbol<T>> getSymbols() {
		return Collections.unmodifiableList(this.symbols);
	}
}
