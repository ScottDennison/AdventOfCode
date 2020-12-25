package uk.co.scottdennison.java.libs.grammar.chomsky.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ChomskyReducedFormRules<K, V extends K> {
	private final Class<V> valueClass;
	private final Set<K> validStartRuleKeys;
	private final Collection<ChomskyReducedFormTerminalRule<K, V>> terminalRules;
	private final Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules;

	public ChomskyReducedFormRules(Class<V> valueClass, Set<K> validStartRuleKeys, Collection<ChomskyReducedFormTerminalRule<K, V>> terminalRules, Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules) {
		this.valueClass = valueClass;
		this.validStartRuleKeys = new HashSet<>(validStartRuleKeys);
		this.terminalRules = new ArrayList<>(terminalRules);
		this.nonTerminalRules = new ArrayList<>(nonTerminalRules);
	}

	public Class<V> getValueClass() {
		return this.valueClass;
	}

	public Set<K> getValidStartRuleKeys() {
		return this.validStartRuleKeys;
	}

	public Collection<ChomskyReducedFormTerminalRule<K, V>> getTerminalRulesView() {
		return Collections.unmodifiableCollection(this.terminalRules);
	}

	public Collection<ChomskyReducedFormNonTerminalRule<K>> getNonTerminalRulesView() {
		return Collections.unmodifiableCollection(this.nonTerminalRules);
	}
}
