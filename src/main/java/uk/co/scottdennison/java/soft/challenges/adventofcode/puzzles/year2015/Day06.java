package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day06 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<action>[a-z ]+) (?<x1>[0-9]+),(?<y1>[0-9]+) through (?<x2>[0-9]+),(?<y2>[0-9]+)$");

	private enum Action {
		TURN_OFF("turn off") {
			@Override
			public boolean getNewState1(boolean oldState) {
				return false;
			}

			@Override
			public int getNewState2(int oldState) {
				return Math.max(0, oldState - 1);
			}
		},
		TURN_ON("turn on") {
			@Override
			public boolean getNewState1(boolean oldState) {
				return true;
			}

			@Override
			public int getNewState2(int oldState) {
				return oldState + 1;
			}
		},
		TOGGLE("toggle") {
			@Override
			public boolean getNewState1(boolean oldState) {
				return !oldState;
			}

			@Override
			public int getNewState2(int oldState) {
				return oldState + 2;
			}
		};

		private static final Map<String, Action> lookupMap = Stream.of(Action.values()).collect(Collectors.toMap(Action::getActionDescription, Function.identity()));

		private final String actionDescription;

		Action(String actionDescription) {
			this.actionDescription = actionDescription;
		}

		public String getActionDescription() {
			return this.actionDescription;
		}

		public abstract boolean getNewState1(boolean oldState);

		public abstract int getNewState2(int oldState);

		public static Optional<Action> lookup(String actionDescription) {
			return Optional.ofNullable(lookupMap.get(actionDescription));
		}
	}

	private static final int GRID_WIDTH = 1000;
	private static final int GRID_HEIGHT = 1000;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		boolean[][] grid1On = new boolean[GRID_HEIGHT][GRID_WIDTH];
		int[][] grid2Brightness = new int[GRID_HEIGHT][GRID_WIDTH];
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line.");
			}
			Optional<Action> optionalAction = Action.lookup(matcher.group("action"));
			if (!optionalAction.isPresent()) {
				throw new IllegalStateException("Invalid action.");
			}
			Action action = optionalAction.get();
			int x1 = Integer.parseInt(matcher.group("x1"));
			int y1 = Integer.parseInt(matcher.group("y1"));
			int x2 = Integer.parseInt(matcher.group("x2"));
			int y2 = Integer.parseInt(matcher.group("y2"));
			int xa = Math.min(x1, x2);
			int ya = Math.min(y1, y2);
			int xb = Math.max(x1, x2);
			int yb = Math.max(y1, y2);
			for (int y = ya; y <= yb; y++) {
				for (int x = xa; x <= xb; x++) {
					grid1On[y][x] = action.getNewState1(grid1On[y][x]);
					grid2Brightness[y][x] = action.getNewState2(grid2Brightness[y][x]);
				}
			}
		}
		int lightsOnInGrid1 = 0;
		int lightBrightnessInGrid2 = 0;
		for (int y = 0; y < GRID_HEIGHT; y++) {
			for (int x = 0; x < GRID_WIDTH; x++) {
				if (grid1On[y][x]) {
					lightsOnInGrid1++;
				}
				lightBrightnessInGrid2 += grid2Brightness[y][x];
			}
		}
		return new BasicPuzzleResults<>(
			lightsOnInGrid1,
			lightBrightnessInGrid2
		);
	}
}
