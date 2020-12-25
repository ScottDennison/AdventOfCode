package uk.co.scottdennison.java.libs.grammar.parseresults.model;

import java.util.OptionalLong;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ParseForestStats {
	private final int minNodes;
	private final int maxNodes;
	private final int minDepth;
	private final int maxDepth;
	private final OptionalLong ways;

	public ParseForestStats(int minNodes, int maxNodes, int minDepth, int maxDepth, OptionalLong ways) {
		this.minNodes = minNodes;
		this.maxNodes = maxNodes;
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
		this.ways = ways;
	}

	public int getMinNodes() {
		return this.minNodes;
	}

	public int getMaxNodes() {
		return this.maxNodes;
	}

	public int getMinDepth() {
		return this.minDepth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public OptionalLong getWays() {
		return this.ways;
	}
}
