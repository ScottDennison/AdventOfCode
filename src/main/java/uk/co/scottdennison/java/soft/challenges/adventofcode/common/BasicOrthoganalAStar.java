package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016.Day22;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;

public class BasicOrthoganalAStar {
	private BasicOrthoganalAStar() {}

	public static class Point {
		private final int x;
		private final int y;
		private Point cameFrom = null;
		private int fScore = Integer.MAX_VALUE;
		private int gScore = Integer.MAX_VALUE;

		private Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		private Point getCameFrom() {
			return this.cameFrom;
		}

		private void setCameFrom(Point cameFrom) {
			this.cameFrom = cameFrom;
		}

		private int getFScore() {
			return this.fScore;
		}

		private void setFScore(int hScore) {
			this.fScore = hScore;
		}

		private int getGScore() {
			return this.gScore;
		}

		private void setGScore(int gScore) {
			this.gScore = gScore;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) return true;
			if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

			Point otherPoint = (Point) otherObject;

			if (this.x != otherPoint.x) return false;
			if (this.y != otherPoint.y) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = this.x;
			result = 31 * result + this.y;
			return result;
		}
	}

	public static interface ValidityCheck {
		boolean isValidPoint(int y, int x);
	}

	private static int heuristic(int fromY, int fromX, int toY, int toX) {
		return Math.abs(toY-fromY)+Math.abs(toX-fromX);
	}

	private static void checkNeighbour(ValidityCheck validityCheck, PriorityQueue<Point> openSet, Point[][] knownPoints, Point currentPoint, int neighbourY, int neighbourX, int targetY, int targetX) {
		if (!validityCheck.isValidPoint(neighbourY, neighbourX)) {
			return;
		}
		Point neighbourPoint = knownPoints[neighbourY][neighbourX];
		boolean newPoint = false;
		if (neighbourPoint == null) {
			newPoint = true;
			neighbourPoint = new Point(neighbourX, neighbourY);
			knownPoints[neighbourY][neighbourX] = neighbourPoint;
		}
		int potentialGScore = currentPoint.getGScore() + 1;
		if (potentialGScore < neighbourPoint.getGScore()) {
			if (!newPoint) {
				openSet.remove(neighbourPoint);
			}
			neighbourPoint.setCameFrom(currentPoint);
			neighbourPoint.setGScore(potentialGScore);
			neighbourPoint.setFScore(potentialGScore + heuristic(neighbourY, neighbourX, targetY, targetX));
			openSet.add(neighbourPoint);
		}
	}

	public static Optional<Deque<Point>> run(ValidityCheck validityCheck, int sourceY, int sourceX, int targetY, int targetX, int minY, int minX, int maxY, int maxX) {
		PriorityQueue<Point> openSet = new PriorityQueue<>(Comparator.comparing(Point::getFScore));
		Point[][] knownPoints = new Point[maxY+1][maxX+1];
		Point startPoint = new Point(sourceX, sourceY);
		startPoint.setGScore(0);
		startPoint.setFScore(heuristic(sourceY, sourceX, targetY, targetX));
		knownPoints[sourceY][sourceX] = startPoint;
		openSet.add(startPoint);
		while (true) {
			Point point = openSet.poll();
			if (point == null) {
				return Optional.empty();
			}
			if (point.getY() == targetY && point.getX() == targetX) {
				Deque<Point> steps = new LinkedList<>();
				steps.addFirst(point);
				while (true) {
					point = point.getCameFrom();
					if (point == null) {
						break;
					}
					steps.addFirst(point);
				}
				return Optional.of(steps);
			}
			int y = point.getY();
			int x = point.getX();
			for (int yDelta=-1; yDelta<=1; yDelta+=2) {
				int newY = y+yDelta;
				if (newY >= minY && newY <= maxY) {
					checkNeighbour(validityCheck, openSet, knownPoints, point, newY, x, targetY, targetX);
				}
			}
			for (int xDelta=-1; xDelta<=1; xDelta+=2) {
				int newX = x+xDelta;
				if (newX >= minX && newX <= maxX) {
					checkNeighbour(validityCheck, openSet, knownPoints, point, y, newX, targetY, targetX);
				}
			}
		}
	}
}
