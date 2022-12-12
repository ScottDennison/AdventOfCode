package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class Day12 implements IPuzzle {
	private static final class Cell implements Comparable<Cell>, Iterable<Cell> {
		private final int x;
		private final int y;
		private final char height;
		private final Set<Cell> neighbours = new HashSet<>();
		private boolean locked = false;
		private int distance = Integer.MAX_VALUE;

		private Cell(int x, int y, char height) {
			this.x = x;
			this.y = y;
			this.height = height;
		}

		@Override
		public int compareTo(Cell otherCell) {
			return Integer.compare(this.distance, otherCell.distance);
		}

		public void addNeighbour(Cell cell) {
			if (this.locked) {
				throw new IllegalStateException("Cannot add new neighbours");
			}
			if (this.neighbours.add(cell)) {
				cell.addNeighbour(this);
			}
		}

		public void lock() {
			this.locked = true;
		}

		@Override
		public Iterator<Cell> iterator() {
			return this.neighbours.iterator();
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public char getHeight() {
			return this.height;
		}

		public int getDistance() {
			return this.distance;
		}

		public void setDistance(int distance) {
			this.distance = distance;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
		int height = inputGrid.length;
		int width = inputGrid[0].length;
		Cell[][] cells = new Cell[height][width];
		boolean partAStartFound = false;
		int partAStartX = -1;
		int partAStartY = -1;
		boolean endFound = false;
		int endX = -1;
		int endY = -1;
		for (int y = 0; y < height; y++) {
			char[] inputGridRow = inputGrid[y];
			if (inputGridRow.length != width) {
				throw new IllegalStateException("Input characters grid is not square");
			}
			for (int x = 0; x < width; x++) {
				char inputGridCharacter = inputGridRow[x];
				if (inputGridCharacter == 'S') {
					if (partAStartFound) {
						throw new IllegalStateException("Duplicate part A start cell");
					}
					else {
						partAStartFound = true;
						partAStartX = x;
						partAStartY = y;
					}
					inputGridCharacter = 'a';
				}
				else if (inputGridCharacter == 'E') {
					if (endFound) {
						throw new IllegalStateException("Duplicate end cell");
					}
					else {
						endFound = true;
						endX = x;
						endY = y;
					}
					inputGridCharacter = 'z';
				}
				else if (inputGridCharacter < 'a' || inputGridCharacter > 'z') {
					throw new IllegalStateException("Invalid cell height");
				}
				Cell thisCell = new Cell(x, y, inputGridCharacter);
				if (x > 0) {
					thisCell.addNeighbour(cells[y][x - 1]);
				}
				if (y > 0) {
					thisCell.addNeighbour(cells[y - 1][x]);
				}
				cells[y][x] = thisCell;
			}
		}
		if (!partAStartFound) {
			throw new IllegalStateException("No part A start cell");
		}
		if (!endFound) {
			throw new IllegalStateException("No end cell");
		}
		Cell partAStartCell = cells[partAStartY][partAStartX];
		Cell endCell = cells[endY][endX];
		Integer partASolution = null;
		Integer partBSolution = null;
		endCell.setDistance(0);
		PriorityQueue<Cell> openSet = new PriorityQueue<>();
		openSet.add(endCell);
		Cell thisCell;
		while ((thisCell = openSet.poll()) != null) { // If partA is solved, part B is guaranteed to be solved.
			char thisCellHeight = thisCell.getHeight();
			int thisCellDistance = thisCell.getDistance();
			if (thisCell == partAStartCell) {
				partASolution = thisCellDistance;
				break;
			}
			if (partBSolution == null && thisCellHeight == 'a') {
				partBSolution = thisCellDistance;
			}
			thisCell.lock();
			int possibleNeighbourDistance = thisCellDistance + 1;
			for (Cell neighbourCell : thisCell) {
				int heightDecrease = thisCellHeight-neighbourCell.getHeight();
				if (heightDecrease <= 1 && possibleNeighbourDistance < neighbourCell.getDistance()) {
					openSet.remove(neighbourCell);
					neighbourCell.setDistance(possibleNeighbourDistance);
					openSet.add(neighbourCell);
				}
			}
		}
		return new BasicPuzzleResults<>(
			partASolution,
			partBSolution
		);
	}
}
