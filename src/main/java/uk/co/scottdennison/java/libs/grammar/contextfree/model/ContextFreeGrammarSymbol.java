package uk.co.scottdennison.java.libs.grammar.contextfree.model;

public final class ContextFreeGrammarSymbol<T> {
	private final boolean terminal;
	private final T symbol;

	public ContextFreeGrammarSymbol(boolean terminal, T symbol) {
		this.terminal = terminal;
		this.symbol = symbol;
	}

	public boolean isTerminal() {
		return this.terminal;
	}

	public T getSymbol() {
		return this.symbol;
	}
}
