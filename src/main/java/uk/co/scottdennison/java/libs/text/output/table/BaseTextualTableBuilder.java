package uk.co.scottdennison.java.libs.text.output.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BaseTextualTableBuilder<ValueType> {
	private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\u001B\\[[0-9=?;]*[A-Za-z]");

	private final List<String> knownHeadersList = new ArrayList<>();
	private final Set<String> knownHeadersSet = new HashSet<>();
	private final List<Map<String, ValueType>> rows = new ArrayList<>();
	private boolean headersLocked = false;

	public void addHeaders(String... headers) {
		if (this.headersLocked) {
			throw new IllegalStateException("The headers have been locked.");
		}
		else if (headers == null) {
			throw new IllegalArgumentException("The headers array must not be NULL.");
		}
		else {
			for (String header : headers) {
				if (header == null) {
					throw new IllegalArgumentException("The headers array must not contain NULL.");
				}
			}
			for (String header : headers) {
				if (!knownHeadersSet.contains(header)) {
					this.knownHeadersList.add(header);
					this.knownHeadersSet.add(header);
				}
			}
		}
	}

	public boolean isEmpty() {
		return this.knownHeadersList.isEmpty() && this.rows.isEmpty();
	}

	public void lockHeaders() {
		this.headersLocked = true;
	}

	public List<String> getKnownHeaders() {
		return Collections.unmodifiableList(this.knownHeadersList);
	}

	protected List<Map<String, ValueType>> getRows() {
		return Collections.unmodifiableList(this.rows.stream().map(Collections::unmodifiableMap).collect(Collectors.toList()));
	}

	public int getRowCount() {
		return this.rows.size();
	}

	private Map<String, ValueType> getLastRow() {
		if (this.getRowCount() < 1) {
			this.addRow();
		}
		return this.rows.get(this.getRowCount() - 1);
	}

	private void addRow() {
		this.rows.add(new HashMap<>());
	}

	public void addRow(boolean collapse) {
		if (!(collapse && this.getLastRow().isEmpty())) {
			this.addRow();
		}
	}

	protected void addEntry(String header, ValueType value) {
		if (header == null) {
			throw new IllegalArgumentException("The header must not be NULL.");
		}
		else {
			if (!this.knownHeadersSet.contains(header)) {
				if (this.headersLocked) {
					throw new IllegalStateException("A new header was provided, but could not be added as the headers have been locked.");
				}
				else {
					this.knownHeadersList.add(header);
					this.knownHeadersSet.add(header);
				}
			}
			this.getLastRow().put(header, value);
		}
	}

	protected static int calculateLength(String value) {
		int length = value.length();
		if (value.indexOf('\u001B') != -1) {
			Matcher matcher = ANSI_ESCAPE_PATTERN.matcher(value);
			while (matcher.find()) {
				length -= matcher.group().length();
			}
		}
		return length;
	}
}