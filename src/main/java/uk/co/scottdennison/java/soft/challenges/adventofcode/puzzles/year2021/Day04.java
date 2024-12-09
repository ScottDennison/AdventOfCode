package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class Day04 implements IPuzzle {
	private static final Pattern PATTERN_SPACE = Pattern.compile(" +");

	private static final int BOARD_WIDTH = 5;
	private static final int BOARD_HEIGHT = 5;

	private static class Board {
		private int[][] numbers;
		private boolean[][] called;

		public Board(int[][] numbers) {
			this.numbers = new int[BOARD_HEIGHT][BOARD_WIDTH];
			this.called = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
			if (numbers.length != BOARD_HEIGHT) {
				throw new IllegalStateException("Incorrectly sized array");
			}
			for (int y=0; y<BOARD_HEIGHT; y++) {
				int[] originalRow = numbers[y];
				if (originalRow.length != BOARD_WIDTH) {
					throw new IllegalStateException("Incorrectly sized array");
				}
				System.arraycopy(originalRow,0,this.numbers[y],0,BOARD_WIDTH);
			}
		}

		public OptionalInt mark(int number) {
			for (int y=0; y<BOARD_HEIGHT; y++) {
				for (int x=0; x<BOARD_WIDTH; x++) {
					if (numbers[y][x] == number) {
						called[y][x] = true;
					}
				}
			}
			boolean won = false;
			for (int y=0; y<BOARD_HEIGHT; y++) {
				boolean allCalled = true;
				for (int x=0; x<BOARD_WIDTH; x++) {
					if (!called[y][x]) {
						allCalled = false;
					}
				}
				if (allCalled) {
					won = true;
					break;
				}
			}
			if (!won) {
				for (int x=0; x<BOARD_WIDTH; x++) {
					boolean allCalled = true;
					for (int y=0; y<BOARD_HEIGHT; y++) {
						if (!called[y][x]) {
							allCalled = false;
						}
					}
					if (allCalled) {
						won = true;
						break;
					}
				}
			}
			if (won) {
				int score = 0;
				for (int y=0; y<BOARD_HEIGHT; y++) {
					for (int x=0; x<BOARD_WIDTH; x++) {
						if (!called[y][x]) {
							score += numbers[y][x];
						}
					}
				}
				score *= number;
				return OptionalInt.of(score);
			} else {
				return OptionalInt.empty();
			}
		}
	}

	@Override
	public IPuzzleResults runPuzzle(
		char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		String[] lines = LineReader.stringsArray(inputCharacters, true);
		String callingLine = lines[0];
		int currentBoardY = BOARD_HEIGHT;
		int[][] currentBoard = new int[BOARD_HEIGHT][];
		List<Board> boards = new ArrayList<>();
		int lineCount = lines.length;
		for (int y=1; y<lines.length; y++) {
			String line = lines[y];
			if (currentBoardY == BOARD_HEIGHT) {
				if (!line.isEmpty()) {
					throw new IllegalStateException("Expected an empty line");
				}
				if (y > 1) {
					boards.add(new Board(currentBoard));
				}
				currentBoardY = 0;
			} else {
				int[] entries = PATTERN_SPACE.splitAsStream(line.trim()).mapToInt(Integer::parseInt).toArray();
				if (entries.length != BOARD_WIDTH) {
					throw new IllegalStateException("Too many entries per line");
				}
				currentBoard[currentBoardY++] = entries;
			}
		}
		if (currentBoardY == BOARD_HEIGHT) {
			if (lineCount > 2) {
				boards.add(new Board(currentBoard));
			} else {
				throw new IllegalStateException("No boards");
			}
		} else {
			throw new IllegalStateException("Incomplete board");
		}
		OptionalInt firstBoardWinningScore = OptionalInt.empty();
		OptionalInt lastBoardWinningScore = OptionalInt.empty();
		List<Board> boardsRemaining = new LinkedList<>(boards);
		for (String callingNumberString : callingLine.split(",")) {
			int callingNumber = Integer.parseInt(callingNumberString);
			Iterator<Board> boardIterator = boardsRemaining.listIterator();
			OptionalInt boardScoreThisTurn = OptionalInt.empty();
			boolean multipleBoardsWonThisTurn = false;
			while (boardIterator.hasNext()) {
				Board board = boardIterator.next();
				OptionalInt boardResult = board.mark(callingNumber);
				if (boardResult.isPresent()) {
					if (boardScoreThisTurn.isPresent()) {
						multipleBoardsWonThisTurn = true;
					} else {
						boardScoreThisTurn = boardResult;
					}
					boardIterator.remove();
				}
			}
			if (boardScoreThisTurn.isPresent()) {
				if (!firstBoardWinningScore.isPresent()) {
					if (multipleBoardsWonThisTurn) {
						throw new IllegalStateException("There is no 'first' winning board. Multiple boards won on this turn.");
					}
					else {
						firstBoardWinningScore = boardScoreThisTurn;
					}
				}
				if (boardsRemaining.isEmpty()) {
					if (multipleBoardsWonThisTurn) {
						throw new IllegalStateException("There is no 'last' winning board. Multiple boards won on this turn.");
					}
					else {
						lastBoardWinningScore = boardScoreThisTurn;
						break;
					}
				}
			}
		}
		if (!firstBoardWinningScore.isPresent() || !lastBoardWinningScore.isPresent()) {
			throw new IllegalStateException("Answers not calculatable");
		}
		return new BasicPuzzleResults<>(
			firstBoardWinningScore.getAsInt(),
			lastBoardWinningScore.getAsInt()
		);
	}
}
