package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<name>[a-z]+) can fly (?<distance>[0-9]+) km/s for (?<activeSeconds>[0-9]+) seconds, but then must rest for (?<restSeconds>[0-9]+) seconds\\.$", Pattern.CASE_INSENSITIVE);

	private static class Reindeer {
		private final String name;
		private final int speed;
		private final int activeSeconds;
		private final int restSeconds;

		private int distanceTravelled;
		private int remainingSecondsUntilChange;
		private boolean active;
		private int points;

		public Reindeer(String name, int speed, int activeSeconds, int restSeconds) {
			this.name = name;
			this.speed = speed;
			this.activeSeconds = activeSeconds;
			this.restSeconds = restSeconds;
			this.distanceTravelled = 0;
			this.remainingSecondsUntilChange = activeSeconds;
			this.active = true;
			this.points = 0;
		}

		public void tick() {
			if (this.active) {
				this.distanceTravelled += this.speed;
			}
			if (--this.remainingSecondsUntilChange <= 0) {
				if (this.active) {
					this.active = false;
					this.remainingSecondsUntilChange = this.restSeconds;
				}
				else {
					this.active = true;
					this.remainingSecondsUntilChange = this.activeSeconds;
				}
			}
		}

		public void awardPoint() {
			this.points++;
		}

		public int getDistanceTravelled() {
			return this.distanceTravelled;
		}

		public int getPoints() {
			return this.points;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<Reindeer> reindeerList = new ArrayList<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparsable line");
			}
			reindeerList.add(new Reindeer(matcher.group("name"), Integer.parseInt(matcher.group("distance")), Integer.parseInt(matcher.group("activeSeconds")), Integer.parseInt(matcher.group("restSeconds"))));
		}
		Reindeer[] reindeerArray = reindeerList.toArray(new Reindeer[0]);
		int targetSeconds = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("seconds")));
		for (int second = 1; second <= targetSeconds; second++) {
			for (Reindeer reindeer : reindeerArray) {
				reindeer.tick();
			}
			int furthestDistance = Arrays.stream(reindeerArray).mapToInt(Reindeer::getDistanceTravelled).max().orElse(0);
			for (Reindeer reindeer : reindeerArray) {
				if (reindeer.getDistanceTravelled() == furthestDistance) {
					reindeer.awardPoint();
				}
			}
		}
		return new BasicPuzzleResults<>(
			calculateAnswer(reindeerArray, Reindeer::getDistanceTravelled),
			calculateAnswer(reindeerArray, Reindeer::getPoints)
		);
	}

	private static int calculateAnswer(Reindeer[] reindeerArray, ToIntFunction<Reindeer> measurementGetter) {
		return measurementGetter.applyAsInt(Arrays.stream(reindeerArray).max(Comparator.comparingInt(measurementGetter)).orElseThrow(() -> new IllegalStateException("No reindeer")));
	}
}
