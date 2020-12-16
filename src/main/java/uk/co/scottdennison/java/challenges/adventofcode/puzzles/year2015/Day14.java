package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 {
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

		public String getName() {
			return this.name;
		}

		public int getDistanceTravelled() {
			return this.distanceTravelled;
		}

		public int getPoints() {
			return this.points;
		}
	}

	private static final int TARGET_SECONDS = 2503;

	public static void main(String[] args) throws IOException {
		List<Reindeer> reindeerList = new ArrayList<>();
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(fileLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparsable line");
			}
			reindeerList.add(new Reindeer(matcher.group("name"), Integer.parseInt(matcher.group("distance")), Integer.parseInt(matcher.group("activeSeconds")), Integer.parseInt(matcher.group("restSeconds"))));
		}
		Reindeer[] reindeerArray = reindeerList.toArray(new Reindeer[0]);
		for (int second = 1; second <= TARGET_SECONDS; second++) {
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
		outputSummary(reindeerArray, "distance", Reindeer::getDistanceTravelled);
		outputSummary(reindeerArray, "points", Reindeer::getPoints);
	}

	private static void outputSummary(Reindeer[] reindeerArray, String measurement, ToIntFunction<Reindeer> measurementGetter) {
		Reindeer winningReindeer = Arrays.stream(reindeerArray).max(Comparator.comparingInt(measurementGetter)).orElseThrow(() -> new IllegalStateException("No reindeer"));
		System.out.format("The winning reindeer when measured by %s is %s with %d %s%n", measurement, winningReindeer.getName(), measurementGetter.applyAsInt(winningReindeer), measurement);
	}
}
