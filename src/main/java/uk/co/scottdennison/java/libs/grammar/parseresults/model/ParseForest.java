package uk.co.scottdennison.java.libs.grammar.parseresults.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ParseForest<K> {
	private final K rule;
	private final Collection<ParseTree<K>> possibilities;

	public ParseForest(K rule, List<ParseTree<K>> possibilities) {
		this.rule = rule;
		this.possibilities = new ArrayList<>(possibilities);
	}

	public K getRule() {
		return this.rule;
	}

	public Collection<ParseTree<K>> getPossibilitiesView() {
		return Collections.unmodifiableCollection(this.possibilities);
	}
}
