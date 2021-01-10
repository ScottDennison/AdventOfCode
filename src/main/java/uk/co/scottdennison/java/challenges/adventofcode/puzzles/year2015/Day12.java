package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzlePartResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzlePartResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.MultiPartPuzzleResults;

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;

public class Day12 implements IPuzzle {
	private static long readObject(PushbackReader pushbackReader, boolean skipRed) throws IOException {
		require(pushbackReader, '{');
		consumeWhitespace(pushbackReader);
		long sum = 0;
		boolean hadRedValue = false;
		if (!optionallyRead(pushbackReader, '}')) {
			for (; ; ) {
				readAndReturnString(pushbackReader);
				consumeWhitespace(pushbackReader);
				require(pushbackReader, ':');
				if (peek(pushbackReader) == '"') {
					hadRedValue |= "red".equalsIgnoreCase(readAndReturnString(pushbackReader));
				}
				else {
					sum = Math.addExact(sum, readValue(pushbackReader, skipRed));
				}
				if (!optionallyRead(pushbackReader, ',')) {
					break;
				}
				consumeWhitespace(pushbackReader);
			}
			require(pushbackReader, '}');
		}
		if (skipRed && hadRedValue) {
			return 0;
		}
		else {
			return sum;
		}
	}

	private static long readArray(PushbackReader pushbackReader, boolean skipRed) throws IOException {
		require(pushbackReader, '[');
		consumeWhitespace(pushbackReader);
		long sum = 0;
		if (!optionallyRead(pushbackReader, ']')) {
			do {
				sum = Math.addExact(sum, readValue(pushbackReader, skipRed));
			} while (optionallyRead(pushbackReader, ','));
			require(pushbackReader, ']');
		}
		return sum;
	}

	private static long readAndReturnNumber(PushbackReader pushbackReader) throws IOException {
		boolean negative = optionallyRead(pushbackReader, '-');
		long value;
		if (optionallyRead(pushbackReader, '0')) {
			value = 0;
		}
		else {
			value = readDigit(pushbackReader, false, false, true);
			long digitValue;
			while ((digitValue = readDigit(pushbackReader, false, true, false)) != -1) {
				value = Math.addExact(Math.multiplyExact(value, 10), digitValue);
			}
		}
		if (optionallyRead(pushbackReader, '.')) {
			throw new IllegalStateException("Decimal numbers are not allowed as puzzle input.");
		}
		int c1 = pushbackReader.read();
		switch (c1) {
			case 'E':
			case 'e':
				char c2;
				switch (c2 = read(pushbackReader)) {
					case '+':
						long exponent = readDigit(pushbackReader, false, true, true);
						long digitValue;
						while ((digitValue = readDigit(pushbackReader, true, true, false)) != -1) {
							exponent = Math.addExact(Math.multiplyExact(exponent, 10), digitValue);
						}
						double valueMultiplierDouble = Math.pow(10, exponent);
						long valueMultiplier = (long) valueMultiplierDouble;
						if (valueMultiplierDouble != valueMultiplier) {
							throw new IllegalStateException("Exponent too large to convert to long.");
						}
						value = Math.multiplyExact(value, valueMultiplier);
						break;
					case '-':
						throw new IllegalStateException("Decimal numbers are not allowed as puzzle input, and a negative exponent would result in this.");
					default:
						throw new IllegalStateException("Unexpected character: " + (int) c2);
				}
			default:
				pushbackReader.unread(c1);
		}
		if (negative) {
			value = Math.multiplyExact(value, -1);
		}
		return value;
	}

	private static String readAndReturnString(PushbackReader pushbackReader) throws IOException {
		require(pushbackReader, '\"');
		StringBuilder stringBuilder = new StringBuilder();
		boolean shouldContinue = true;
		while (shouldContinue) {
			char c1;
			switch (c1 = read(pushbackReader)) {
				case '"':
					shouldContinue = false;
					break;
				case '\\':
					char c2;
					switch (c2 = read(pushbackReader)) {
						case '"':
						case '\\':
						case '/':
							stringBuilder.append(c2);
							break;
						case 'b':
							stringBuilder.append('\b');
							break;
						case 'f':
							stringBuilder.append('\f');
							break;
						case 'n':
							stringBuilder.append('\n');
							break;
						case 'r':
							stringBuilder.append('\r');
							break;
						case 't':
							stringBuilder.append('\t');
							break;
						case 'u':
							int characterCode = 0;
							for (int index = 0; index < 4; index++) {
								char c3;
								characterCode <<= 4;
								switch (c3 = read(pushbackReader)) {
									case '0':
									case '1':
									case '2':
									case '3':
									case '4':
									case '5':
									case '6':
									case '7':
									case '8':
									case '9':
										characterCode |= (c3 - 'A');
										break;
									case 'A':
									case 'B':
									case 'C':
									case 'D':
									case 'E':
									case 'F':
										characterCode |= (c3 - 'A') + 10;
										break;
									case 'a':
									case 'b':
									case 'c':
									case 'd':
									case 'e':
									case 'f':
										characterCode |= (c3 - 'a') + 10;
										break;
									default:
										throw new IllegalStateException("Unexpected character: " + (int) c3);
								}
							}
							stringBuilder.append((char) characterCode);
						default:
							throw new IllegalStateException("Unexpected escape: " + (int) c2);
					}
					break;
				default:
					stringBuilder.append(c1);
					break;
			}
		}
		return stringBuilder.toString();
	}

	private static long readDigit(PushbackReader pushbackReader, boolean allowEnd, boolean allowZero, boolean failIfNonDigit) throws IOException {
		int c;
		switch ((c = pushbackReader.read())) {
			case -1:
				if (allowEnd) {
					return -1;
				}
				else {
					throw new EOFException();
				}
			case '0':
				if (allowZero) {
					return 0;
				}
				else {
					throw new IllegalStateException("0 not allowed here.");
				}
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			default:
				if (failIfNonDigit) {
					throw new IllegalStateException("Unexpected character: " + c);
				}
				else {
					pushbackReader.unread(c);
					return -1;
				}
		}
	}

	private static long readValue(PushbackReader pushbackReader, boolean skipRed) throws IOException {
		consumeWhitespace(pushbackReader);
		long sum;
		char c;
		switch (c = peek(pushbackReader)) {
			case '{':
				sum = readObject(pushbackReader, skipRed);
				break;
			case '[':
				sum = readArray(pushbackReader, skipRed);
				break;
			case '\"':
				readAndReturnString(pushbackReader);
				sum = 0;
				break;
			case 't':
				require(pushbackReader, 't');
				require(pushbackReader, 'r');
				require(pushbackReader, 'u');
				require(pushbackReader, 'e');
				sum = 0;
				break;
			case 'f':
				require(pushbackReader, 'f');
				require(pushbackReader, 'a');
				require(pushbackReader, 'l');
				require(pushbackReader, 's');
				require(pushbackReader, 'e');
				sum = 0;
				break;
			case 'n':
				require(pushbackReader, 'n');
				require(pushbackReader, 'u');
				require(pushbackReader, 'l');
				require(pushbackReader, 'l');
				sum = 0;
				break;
			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				sum = readAndReturnNumber(pushbackReader);
				break;
			default:
				throw new IllegalStateException("Unexpected character: " + (int) c);
		}
		consumeWhitespace(pushbackReader);
		return sum;
	}

	private static void consumeWhitespace(PushbackReader pushbackReader) throws IOException {
		int c;
		boolean shouldContinue = true;
		do {
			c = pushbackReader.read();
			if (c == -1) {
				return;
			}
			switch (c) {
				case ' ':
				case '\r':
				case '\n':
				case '\t':
					break;
				default:
					shouldContinue = false;
					pushbackReader.unread(c);
					break;
			}
		} while (shouldContinue);
	}

	private static boolean optionallyRead(PushbackReader pushbackReader, char c) throws IOException {
		char readC;
		if ((readC = read(pushbackReader)) == c) {
			return true;
		}
		else {
			pushbackReader.unread(readC);
			return false;
		}
	}

	private static void require(PushbackReader pushbackReader, char c) throws IOException {
		char readC;
		if ((readC = read(pushbackReader)) != c) {
			throw new IllegalStateException("Unexpected character: " + (int) readC);
		}
	}

	private static char read(PushbackReader pushbackReader) throws IOException {
		int c = pushbackReader.read();
		if (c == -1) {
			throw new EOFException();
		}
		return (char) c;
	}

	private static char peek(PushbackReader pushbackReader) throws IOException {
		char c = read(pushbackReader);
		pushbackReader.unread(c);
		return c;
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		return new MultiPartPuzzleResults<>(
			runPuzzlePart(inputCharacters, false),
			runPuzzlePart(inputCharacters, true)
		);
	}

	private static IPuzzlePartResults runPuzzlePart(char[] inputCharacters, boolean skipRed) {
		PushbackReader pushbackReader = new PushbackReader(new CharArrayReader(inputCharacters), 1);
		try {
			long sum = readValue(pushbackReader, skipRed);
			consumeWhitespace(pushbackReader);
			if (pushbackReader.read() != -1) {
				throw new IllegalStateException("Not at end of input.");
			}
			return new BasicPuzzlePartResults<>(sum);
		} catch (IOException ex) {
			throw new IllegalStateException("IOException when parsing JSON", ex);
		}
	}
}
