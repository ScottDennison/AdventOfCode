package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 {
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

	public static void main(String[] args) throws IOException {
		long maskAnd = 0xFFFFFFFFFFFFFFFFL;
		long maskOr = 0x0000000000000000L;
		long maskFloating = 0x0000000000000000L;
		Map<Long, Long> memoryV1 = new HashMap<>();
		Map<Long, Long> memoryV2 = new HashMap<>();
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(fileLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to parse line");
			}
			String maskString = matcher.group("mask");
			if (maskString == null) {
				long address = Long.parseLong(matcher.group("address"));
				long newValue = Long.parseLong(matcher.group("value"));
				memoryV1.put(address, (newValue & maskAnd) | maskOr);
				recursePutMemoryV2(memoryV2, 0, maskFloating, address | maskOr, newValue);
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
		outputSummary(1, memoryV1);
		outputSummary(2, memoryV2);
	}

	private static void outputSummary(int version, Map<Long, Long> memory) {
		System.out.format("Memory sum for version %d: %d%n", version, sumMemory(memory));
	}

	private static long sumMemory(Map<Long, Long> memory) {
		long sum = 0;
		for (long memoryValue : memory.values()) {
			sum = Math.addExact(sum, memoryValue);
		}
		return sum;
	}
}
