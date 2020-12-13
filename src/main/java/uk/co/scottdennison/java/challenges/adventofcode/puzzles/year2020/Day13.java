package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day13 {
	private static class NextBusResult {
		private final long busId;
		private final long minutesToWait;

		public NextBusResult(long busId, long minutesToWait) {
			this.busId = busId;
			this.minutesToWait = minutesToWait;
		}

		public long getBusId() {
			return this.busId;
		}

		public long getMinutesToWait() {
			return this.minutesToWait;
		}
	}

	private static class ChineseNumberTheoremInput {
		private final long n;
		private final long a;

		public ChineseNumberTheoremInput(long n, long a) {
			this.n = n;
			this.a = a;
		}

		public long getN() {
			return this.n;
		}

		public long getA() {
			return this.a;
		}
	}

	private static class ExtendedEuclideanAlgorithmResult {
		private final long gcd;
		private final long bezoutCoefficientS;
		private final long bezoutCoefficientT;

		public ExtendedEuclideanAlgorithmResult(long gcd, long bezoutCoefficientS, long bezoutCoefficientT) {
			this.gcd = gcd;
			this.bezoutCoefficientS = bezoutCoefficientS;
			this.bezoutCoefficientT = bezoutCoefficientT;
		}

		public long getGcd() {
			return this.gcd;
		}

		public long getBezoutCoefficientS() {
			return this.bezoutCoefficientS;
		}

		public long getBezoutCoefficientT() {
			return this.bezoutCoefficientT;
		}
	}

	// Based on https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode
	private static ExtendedEuclideanAlgorithmResult solveExtendedEuclideanAlgorithm(long a, long b) {
		long s = 0;
		long oldS = 1;
		long r = b;
		long oldR = a;
		while (r != 0) {
			long quotient = oldR / r;
			long temp;
			temp = oldR;
			oldR = r;
			r = temp - quotient * r;
			temp = oldS;
			oldS = s;
			s = temp - quotient * s;
		}
		long bezoutT;
		if (b != 0) {
			bezoutT = (oldR - oldS * a) / b;
		}
		else {
			bezoutT = 0;
		}
		return new ExtendedEuclideanAlgorithmResult(oldR, oldS, bezoutT);
	}

	// Based on https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Existence_(direct_construction) with sanity checks mentioned in https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Statement
	private static long solveChineseNumberTheorem(ChineseNumberTheoremInput[] inputs) {
		int inputCount = inputs.length;
		long upperN = 1;
		for (int leftIndex = 0; leftIndex < inputCount; leftIndex++) {
			long nLeft = inputs[leftIndex].getN();
			upperN = Math.multiplyExact(upperN, nLeft);
			for (int rightIndex = leftIndex + 1; rightIndex < inputCount; rightIndex++) {
				long nRight = inputs[rightIndex].getN();
				long gcd = solveExtendedEuclideanAlgorithm(nLeft, nRight).getGcd();
				if (gcd != 1) {
					throw new IllegalStateException("All n values must be pairwise coprime. " + nLeft + " and " + nRight + " have a gcd of " + gcd + ", and are therefore not pairwise coprime.");
				}
			}
		}
		long sum = 0;
		for (int i = 1; i <= inputCount; i++) {
			ChineseNumberTheoremInput input = inputs[i - 1];
			long lowerNi = input.getN();
			if (lowerNi <= 1) {
				throw new IllegalArgumentException("All n values must be greater than 1. n value " + i + " is " + lowerNi);
			}
			long lowerAi = input.getA();
			while (lowerAi < 0) {
				lowerAi += lowerNi;
			}
			if (lowerAi >= lowerNi) {
				throw new IllegalArgumentException("All a values must be less than their equivalent n value. a value " + i + " is " + lowerAi);
			}
			long upperNi = upperN / lowerNi;
			ExtendedEuclideanAlgorithmResult extendedEuclideanAlgorithmResult = solveExtendedEuclideanAlgorithm(upperNi, lowerNi);
			long s = extendedEuclideanAlgorithmResult.getBezoutCoefficientS();
			long t = extendedEuclideanAlgorithmResult.getBezoutCoefficientT();
			long mi;
			if (Math.abs(s) > Math.abs(t)) {
				mi = t;
			}
			else {
				mi = s;
			}
			sum = Math.addExact(sum, Math.multiplyExact(Math.multiplyExact(lowerAi, mi), upperNi));
		}
		while (sum < 0) {
			sum += upperN;
		}
		return sum % upperN;
	}

	private static NextBusResult calculateNextBus(long currentTime, Long[] busIds) {
		long bestBusId = Long.MAX_VALUE;
		long smallestWaitTime = Long.MAX_VALUE;
		for (Long busId : busIds) {
			if (busId != null) {
				long remainder = currentTime % busId;
				long waitTime;
				if (remainder == 0) {
					waitTime = 0;
				}
				else {
					waitTime = busId - remainder;
				}
				if (waitTime < smallestWaitTime) {
					smallestWaitTime = waitTime;
					bestBusId = busId;
				}
				else if (waitTime == smallestWaitTime) {
					throw new IllegalStateException("Duplicate smallest wait time");
				}
			}
		}
		return new NextBusResult(bestBusId, smallestWaitTime);
	}

	private static long calculateCompetitionTime(Long[] busIds) {
		List<ChineseNumberTheoremInput> chineseNumberTheoremInputList = new ArrayList<>();
		int busIdCount = busIds.length;
		for (int busIdIndex = 0; busIdIndex < busIdCount; busIdIndex++) {
			Long busId = busIds[busIdIndex];
			if (busId != null) {
				chineseNumberTheoremInputList.add(new ChineseNumberTheoremInput(busId, -busIdIndex));
			}
		}
		ChineseNumberTheoremInput[] chineseNumberTheoremInputArray = chineseNumberTheoremInputList.toArray(new ChineseNumberTheoremInput[0]);
		return solveChineseNumberTheorem(chineseNumberTheoremInputArray);
	}

	public static void main(String[] args) throws IOException {
		String[] fileLines = Pattern.compile("\\s+").split(new String(Files.readAllBytes(InputFileUtils.getInputPath())).trim());
		if (fileLines.length != 2) {
			throw new IllegalStateException("Unexpected input line count");
		}
		long currentTime = Long.parseLong(fileLines[0]);
		String[] busIdStrings = Pattern.compile(",").split(fileLines[1]);
		int busIdCount = busIdStrings.length;
		Long[] busIds = new Long[busIdStrings.length];
		for (int busIdIndex = 0; busIdIndex < busIdCount; busIdIndex++) {
			String busIdString = busIdStrings[busIdIndex];
			Long busId;
			if ("x".equalsIgnoreCase(busIdString)) {
				busId = null;
			}
			else {
				busId = Long.parseLong(busIdString);
			}
			busIds[busIdIndex] = busId;
		}
		NextBusResult nextBusResult = calculateNextBus(currentTime, busIds);
		long competitionTime = calculateCompetitionTime(busIds);
		System.out.format("Best bus ID is %d, with wait time %d, so result is %d%n", nextBusResult.getBusId(), nextBusResult.getMinutesToWait(), nextBusResult.getBusId() * nextBusResult.getMinutesToWait());
		System.out.format("The competition result is %d%n", competitionTime);
	}
}
