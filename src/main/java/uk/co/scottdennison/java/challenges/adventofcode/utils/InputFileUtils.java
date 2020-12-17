package uk.co.scottdennison.java.challenges.adventofcode.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class InputFileUtils {
	private static final int STACK_TRACE_ELEMENT_CALLEE = 2;

	public static Path getInputPath() {
		return getInputPath(Thread.currentThread().getStackTrace()[STACK_TRACE_ELEMENT_CALLEE].getClassName());
	}

	public static Path getInputPath(Class<?> clazz) {
		return getInputPath(clazz.getName());
	}

	private static Path getInputPath(String className) {
		char[] c = className.toCharArray();
		int p = c.length;
		int y1, y2, y3, y4, d1, d2;
		if (!(
			p >= 13 &&
				(d2 = c[--p]) >= '0' && d2 <= '9' &&
				(d1 = c[--p]) >= '0' && d1 <= '9' &&
				c[--p] == 'y' &&
				c[--p] == 'a' &&
				(c[--p] == 'D' || c[p] == 'd') &&
				c[--p] == '.' &&
				(y4 = c[--p]) >= '0' && y4 <= '9' &&
				(y3 = c[--p]) >= '0' && y3 <= '9' &&
				(y2 = c[--p]) >= '0' && y2 <= '9' &&
				(y1 = c[--p]) >= '0' && y1 <= '9' &&
				c[--p] == 'r' &&
				c[--p] == 'a' &&
				c[--p] == 'e' &&
				(c[--p] == 'Y' || c[p] == 'y') &&
				c[--p] == '.'
		)) {
			throw new IllegalStateException("Invalid class name");
		}
		return getInputPath(((y1 - '0') * 1000) + ((y2 - '0') * 100) + ((y3 - '0') * 10) + (y4 - '0'), ((d1 - '0') * 10) + (d2 - '0'));
	}

	public static Path getInputPath(int year, int day) {
		if (year < 0 || year > 9999) {
			throw new IllegalStateException("Invalid year.");
		}
		else if (day < 0 || day > 99) {
			throw new IllegalStateException("Invalid day.");
		}
		return Paths.get(
			new String(
				new char[]{
					'd', 'a', 't', 'a', '/', 'y', 'e', 'a', 'r',
					(char) (((year / 1000) % 10) + '0'),
					(char) (((year / 100) % 10) + '0'),
					(char) (((year / 10) % 10) + '0'),
					(char) ((year % 10) + '0'),
					'/', 'd', 'a', 'y',
					(char) (((day / 10) % 10) + '0'),
					(char) ((day % 10) + '0'),
					'/', 'i', 'n', 'p', 'u', 't', '.', 't', 'x', 't'
				}
			)
		);
	}
}
