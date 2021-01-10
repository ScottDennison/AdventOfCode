package uk.co.scottdennison.java.challenges.adventofcode.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class LineReader {
	private interface Processor<T> {
		T process(char[] chars, int offset, int count);
		boolean isEmpty(T t);
	}

	public static class StringProcessor implements Processor<String> {
		public static final StringProcessor INSTANCE = new StringProcessor();

		private StringProcessor() {}

		@Override
		public String process(char[] chars, int offset, int count) {
			return new String(chars, offset, count);
		}

		@Override
		public boolean isEmpty(String s) {
			return s.isEmpty();
		}
	}

	public static class CharArrayProcessor implements Processor<char[]> {
		public static final CharArrayProcessor INSTANCE = new CharArrayProcessor();

		private CharArrayProcessor() {}

		@Override
		public char[] process(char[] chars, int offset, int count) {
			char[] charsSelection = new char[count];
			System.arraycopy(chars, offset, charsSelection, 0, count);
			return charsSelection;
		}

		@Override
		public boolean isEmpty(char[] chars) {
			return chars.length < 1;
		}
	}

	private final char[] chars;

	private LineReader(char[] chars) {
		this.chars = chars;
	}

	public static LineReader create(char[] chars) {
		int length = chars.length;
		char[] charsCopy = new char[length];
		System.arraycopy(chars, 0, charsCopy, 0, length);
		return new LineReader(charsCopy);
	}

	public static LineReader createWithoutCopy(char[] chars) {
		return new LineReader(chars);
	}

	private static class Iterator<T> implements java.util.Iterator<T> {
		private final Processor<T> processor;
		private final char[] chars;
		private int index = 0;
		private boolean hasNextLine;
		private T next;

		private Iterator(char[] chars, Processor<T> processor) {
			this.processor = processor;
			this.chars = chars;
			this.update();
		}

		@Override
		public boolean hasNext() {
			return this.hasNextLine;
		}

		@Override
		public T next() {
			if (this.hasNextLine) {
				T value = this.next;
				this.update();
				return value;
			} else {
				throw new NoSuchElementException("No more lines.");
			}
		}

		private void update() {
			char[] chars = this.chars;
			int charCount = this.chars.length;
			int startIndex = this.index;
			if (startIndex >= charCount) {
				this.hasNextLine = false;
			} else {
				int stopIndex = startIndex;
				int nextStartIndex = charCount;
				while (stopIndex < charCount) {
					boolean shouldStop = false;
					switch (chars[stopIndex]) {
						case '\r':
							shouldStop = true;
							nextStartIndex = stopIndex + 1;
							break;
						case '\n':
							shouldStop = true;
							int potentialCRIndex = stopIndex + 1;
							if (potentialCRIndex < charCount && chars[potentialCRIndex] == '\n') {
								nextStartIndex = potentialCRIndex + 1;
							}
							else {
								nextStartIndex = potentialCRIndex;
							}
							break;
						default:
							// Continue the loop.
					}
					if (shouldStop) {
						break;
					}
					stopIndex++;
				}
				this.index = nextStartIndex;
				this.next = this.processor.process(chars, startIndex, stopIndex - startIndex);
				this.hasNextLine = true;
			}
		}
	}

	public java.util.Iterator<String> stringsIterator() {
		return this.iterator(StringProcessor.INSTANCE);
	}

	public java.util.Iterator<char[]> charArraysIterator() {
		return new Iterator<>(this.chars,CharArrayProcessor.INSTANCE);
	}

	private <T> Iterator<T> iterator(Processor<T> processor) {
		return new Iterator<>(this.chars,processor);
	}

	public List<String> stringsList(boolean trimEmpty) {
		return this.list(StringProcessor.INSTANCE,trimEmpty);
	}

	public List<char[]> charArraysList(boolean trimEmpty) {
		return this.list(CharArrayProcessor.INSTANCE,trimEmpty);
	}

	public String[] stringsArray(boolean trimEmpty) {
		return this.stringsList(trimEmpty).toArray(new String[0]);
	}

	public char[][] charArraysArray(boolean trimEmpty) {
		return this.charArraysList(trimEmpty).toArray(new char[0][]);
	}

	private <T> List<T> list(Processor<T> processor, boolean trimEmpty) {
		Iterator<T> iterator = this.iterator(processor);
		List<T> items = new ArrayList<>();
		while (iterator.hasNext()) {
			items.add(iterator.next());
		}
		if (trimEmpty) {
			int itemsToRemove = 0;
			for (int index=items.size()-1; index>=0 && processor.isEmpty(items.get(index)); index++) {
				itemsToRemove++;
			}
			if (itemsToRemove > 0) {
				items = new ArrayList<>(items.subList(0, items.size() - itemsToRemove));
			}
		}
		return items;
	}

	public Iterable<String> strings() {
		return LineReader.this::stringsIterator;
	}

	public Iterable<char[]> charArrays() {
		return LineReader.this::charArraysIterator;
	}

	public static java.util.Iterator<String> stringsIterator(char[] chars) {
		return LineReader.create(chars).stringsIterator();
	}

	public static java.util.Iterator<char[]> charArraysIterator(char[] chars) {
		return LineReader.create(chars).charArraysIterator();
	}

	public static List<String> stringsList(char[] chars, boolean trimEmpty) {
		return LineReader.createWithoutCopy(chars).stringsList(trimEmpty);
	}

	public static List<char[]> charArraysList(char[] chars, boolean trimEmpty) {
		return LineReader.createWithoutCopy(chars).charArraysList(trimEmpty);
	}

	public static String[] stringsArray(char[] chars, boolean trimEmpty) {
		return LineReader.createWithoutCopy(chars).stringsArray(trimEmpty);
	}

	public static char[][] charArraysArray(char[] chars, boolean trimEmpty) {
		return LineReader.createWithoutCopy(chars).charArraysArray(trimEmpty);
	}

	public static Iterable<String> strings(char[] chars) {
		return LineReader.create(chars).strings();
	}

	public static Iterable<char[]> charArrays(char[] chars) {
		return LineReader.create(chars).charArrays();
	}
}
