package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?:mask = (?<mask>[01X]+))|(?:mem\\[(?<address>[0-9]+)] = (?<value>[0-9]+))$");

	private static final int BITS = 36;

	private static void recursePutMemoryV2(Map<Long, Long> memoryV2, int recursionIndex, long maskFloating, long currentAddress, long newValue) {
		if (recursionIndex == BITS) {
			memoryV2.put(currentAddress, newValue);
		}
		else {
			long maskMask = 1L << recursionIndex;
			int nextRecursionIndex = recursionIndex + 1;
			if ((maskFloating & maskMask) != 0) {
				recursePutMemoryV2(memoryV2, nextRecursionIndex, maskFloating, currentAddress | maskMask, newValue);
				recursePutMemoryV2(memoryV2, nextRecursionIndex, maskFloating, currentAddress & ~maskMask, newValue);
			}
			else {
				recursePutMemoryV2(memoryV2, nextRecursionIndex, maskFloating, currentAddress, newValue);
			}
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		long maskAnd = 0xFFFFFFFFFFFFFFFFL;
		long maskOr = 0x0000000000000000L;
		long maskFloating = 0x0000000000000000L;
		Map<Long, Long> memoryV1 = new HashMap<>();
		Map<Long, Long> memoryV2 = new HashMap<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to parse line");
			}
			String maskString = matcher.group("mask");
			if (maskString == null) {
				long address = Long.parseLong(matcher.group("address"));
				long newValue = Long.parseLong(matcher.group("value"));
				memoryV1.put(address, (newValue & maskAnd) | maskOr);
				if (!partBPotentiallyUnsolvable) {
					recursePutMemoryV2(memoryV2, 0, maskFloating, address | maskOr, newValue);
				}
			}
			else {
				int maskStringLength = maskString.length();
				long maskMask = 1L << maskStringLength;
				maskAnd = 0xFFFFFFFFFFFFFFFFL;
				maskOr = 0x0000000000000000L;
				maskFloating = 0x0000000000000000L;
				for (int maskStringIndex = 0; maskStringIndex < maskStringLength; maskStringIndex++) {
					maskMask >>>= 1;
					switch (maskString.charAt(maskStringIndex)) {
						case '0':
							maskAnd &= ~maskMask;
							break;
						case '1':
							maskOr |= maskMask;
							break;
						case 'X':
							maskFloating |= maskMask;
							break;
					}
				}
			}
		}
		return new BasicPuzzleResults<>(
			sumMemory(memoryV1),
			partBPotentiallyUnsolvable ? null : sumMemory(memoryV2)
		);
	}

	private static long sumMemory(Map<Long, Long> memory) {
		long sum = 0;
		for (long memoryValue : memory.values()) {
			sum = Math.addExact(sum, memoryValue);
		}
		return sum;
	}
}
