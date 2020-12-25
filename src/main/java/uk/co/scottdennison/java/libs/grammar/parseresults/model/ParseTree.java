package uk.co.scottdennison.java.libs.grammar.parseresults.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParseTree<K> {
	private final List<ParseForest<K>> children;

	public ParseTree(List<ParseForest<K>> children) {
		this.children = new ArrayList<>(children);
	}

	public List<ParseForest<K>> getChildrenView() {
		return Collections.unmodifiableList(this.children);
	}
}
