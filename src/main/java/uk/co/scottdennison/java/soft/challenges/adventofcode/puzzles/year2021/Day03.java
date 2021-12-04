package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day03 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(
		char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		char[][] lines = LineReader.charArraysArray(inputCharacters,true);
		int lineCount = lines.length;
		int bitCountPerLine = lines[0].length;
		if (bitCountPerLine > Integer.SIZE) {
			throw new IllegalStateException("Solution cannot handle that many bits");
		}
		int startOrValue = 1<<(bitCountPerLine-1);
		int[] values = new int[lineCount];
		for (int lineIndex=0; lineIndex<lineCount; lineIndex++) {
			char[] line = lines[lineIndex];
			int value = 0;
			int orValue = startOrValue;
			for (int index=0; index<bitCountPerLine; index++) {
				switch (line[index]) {
					case '0':
						break;
					case '1':
						value |= orValue;
						break;
					default:
						throw new IllegalStateException("Unexpected character");
				}
				orValue >>=1;
			}
			values[lineIndex] = value;
		}
		int gammaRate = 0;
		int epsilonRate = 0;
		for (int bitIndex=0; bitIndex<bitCountPerLine; bitIndex++) {
			int bitCountDifference = calculateBitCountDifference(values, lineCount, bitIndex);
			int orValue = 1<<bitIndex;
			if (bitCountDifference > 0) {
				gammaRate |= orValue;
			} else if (bitCountDifference < 0) {
				epsilonRate |= orValue;
			} else {
				throw new IllegalStateException("No most common bit in index " + bitIndex);
			}
		}
		int oxygenGeneratorRating = calculateRating(values,bitCountPerLine,true,true);
		int co2ScrubberRating = calculateRating(values,bitCountPerLine,false,false);
		return new BasicPuzzleResults<>(
			gammaRate*epsilonRate,
			oxygenGeneratorRating*co2ScrubberRating
		);
	}

	private int calculateRating(int[] values, int bitCount, boolean mostCommonPreferred, boolean keepOneBitsIfEqual) {
		int valuesCount = values.length;
		for (int bitIndex=bitCount-1; bitIndex>=0; bitIndex--) {
			if (valuesCount == 1) {
				break;
			}
			int[] newValues = new int[valuesCount];
			int newValuesCount = 0;
			int bitDifference = calculateBitCountDifference(values, valuesCount, bitIndex);
			boolean shouldKeepOneBits;
			if (bitDifference == 0) {
				shouldKeepOneBits = keepOneBitsIfEqual;
			} else {
				shouldKeepOneBits = (bitDifference < 0) ^ mostCommonPreferred;
			}
			int bitMask = 1<<bitIndex;
			for (int valueIndex=0; valueIndex<valuesCount; valueIndex++) {
				int value = values[valueIndex];
				if (((value&bitMask)!=0) ^ shouldKeepOneBits) {
					newValues[newValuesCount++] = value;
				}
			}
			values = newValues;
			valuesCount = newValuesCount;
		}
		if (valuesCount != 1) {
			throw new IllegalStateException("Failed to reduce to a single value. Reduced to " + valuesCount);
		}
		return values[0];
	}

	private int calculateBitCountDifference(int[] values, int valuesCount, int bitIndex) {
		int zeroCount = 0;
		int oneCount = 0;
		int bitMask = 1<<bitIndex;
		for (int valueIndex=0; valueIndex<valuesCount; valueIndex++) {
			if ((values[valueIndex]&bitMask) != 0) {
				oneCount++;
			} else {
				zeroCount++;
			}
		}
		return oneCount-zeroCount;
	}
}
