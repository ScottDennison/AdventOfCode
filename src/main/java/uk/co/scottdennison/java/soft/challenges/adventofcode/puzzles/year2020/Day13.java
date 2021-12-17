package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.math.ChineseNumberTheorem;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day13 implements IPuzzle {
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
		List<ChineseNumberTheorem.Input> chineseNumberTheoremInputList = new ArrayList<>();
		int busIdCount = busIds.length;
		for (int busIdIndex = 0; busIdIndex < busIdCount; busIdIndex++) {
			Long busId = busIds[busIdIndex];
			if (busId != null) {
				chineseNumberTheoremInputList.add(new ChineseNumberTheorem.Input(busId, -busIdIndex));
			}
		}
		ChineseNumberTheorem.Input[] chineseNumberTheoremInputArray = chineseNumberTheoremInputList.toArray(new ChineseNumberTheorem.Input[0]);
		return ChineseNumberTheorem.solve(chineseNumberTheoremInputArray);
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String[] inputLine = Pattern.compile("\\s+").split(new String(inputCharacters).trim());
		if (inputLine.length != 2) {
			throw new IllegalStateException("Unexpected input line count");
		}
		long currentTime = Long.parseLong(inputLine[0]);
		String[] busIdStrings = Pattern.compile(",").split(inputLine[1]);
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
		return new BasicPuzzleResults<>(
			nextBusResult.getBusId() * nextBusResult.getMinutesToWait(),
			competitionTime
		);
	}
}
