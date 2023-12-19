package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public class AStar {
	// Future Enhancements:
	// * Allow different types of cost
	// * Allow either min or max for the goal.

	private AStar() {}

	public static interface NodeAdapter<NodeKeyType> {
		void getLinkedNodeKeys(NodeKeyType fromNodeKey, Consumer<NodeKeyType> linkedNodeKeyConsumer);
		int getCostOfMovingBetweenLinkedNodes(NodeKeyType linkedFromNodeKey, NodeKeyType linkedToNodeKey);
		int getCostEstimateOfMovingBetweenNodes(NodeKeyType fromNodeKey, NodeKeyType toNodeKey);
		boolean isValidEndingNode(NodeKeyType nodeKey);
		Class<NodeKeyType> getClazz();
	}

	public final static class PointNodeAdapter implements NodeAdapter<PointNodeAdapter.Point> {
		public static interface PointAdapter {
			boolean canMoveBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
			int getCostOfMovingBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
			int getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint);
		}

		public static abstract class OrthagonalEstimatingPointAdapter implements PointAdapter {
			@Override
			public final int getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
				return Math.abs(toPoint.getY()-fromPoint.getY())+Math.abs(toPoint.getX()-fromPoint.getX());
			}
		}

		public static class Point {
			private final int y;
			private final int x;

			public Point(int y, int x) {
				this.y = y;
				this.x = x;
			}

			public int getY() {
				return this.y;
			}

			public int getX() {
				return this.x;
			}

			@Override
			public boolean equals(Object otherObject) {
				if (this == otherObject) return true;
				if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

				Point otherPoint = (Point) otherObject;

				if (this.y != otherPoint.y) return false;
				if (this.x != otherPoint.x) return false;

				return true;
			}

			@Override
			public int hashCode() {
				int result = this.y;
				result = 31 * result + this.x;
				return result;
			}
		}

		private final PointAdapter pointAdapter;
		private final int minY;
		private final int maxY;
		private final int minX;
		private final int maxX;

		public PointNodeAdapter(PointAdapter pointAdapter, int minY, int maxY, int minX, int maxX) {
			this.pointAdapter = pointAdapter;
			this.minY = minY;
			this.maxY = maxY;
			this.minX = minX;
			this.maxX = maxX;
		}

		private void checkAndConsumeLink(Point fromPoint, Consumer<Point> linkedPointConsumer, int toY, int toX) {
			Point toPoint = new Point(toY, toX);
			if (pointAdapter.canMoveBetweenLinkedPoints(fromPoint, toPoint)) {
				linkedPointConsumer.accept(toPoint);
			}
		}

		@Override
		public void getLinkedNodeKeys(Point fromPoint, Consumer<Point> linkedPointConsumer) {
			int y = fromPoint.getY();
			int x = fromPoint.getX();
			if (y != minY) {
				checkAndConsumeLink(fromPoint, linkedPointConsumer, y - 1, x);
			}
			if (y != maxY) {
				checkAndConsumeLink(fromPoint, linkedPointConsumer, y + 1, x);
			}
			if (x != minX) {
				checkAndConsumeLink(fromPoint, linkedPointConsumer, y, x - 1);
			}
			if (x != maxX) {
				checkAndConsumeLink(fromPoint, linkedPointConsumer, y, x + 1);
			}
		}

		@Override
		public int getCostOfMovingBetweenLinkedNodes(Point linkedFromPoint, Point linkedToPoint) {
			return pointAdapter.getCostOfMovingBetweenLinkedPoints(linkedFromPoint, linkedToPoint);
		}

		@Override
		public int getCostEstimateOfMovingBetweenNodes(Point fromPoint, Point toPoint) {
			return pointAdapter.getCostEstimateOfMovingBetweenPoints(fromPoint, toPoint);
		}

		@Override
		public boolean isValidEndingNode(Point nodeKey) {
			return true;
		}

		@Override
		public Class<Point> getClazz() {
			return Point.class;
		}
	}

	private static class Node<NodeKeyType> {
		private final NodeKeyType nodeKey;
		private Node<NodeKeyType> cameFrom = null;
		private int fScore = Integer.MAX_VALUE;
		private int gScore = Integer.MAX_VALUE;

		private Node(NodeKeyType nodeKey) {
			this.nodeKey = nodeKey;
		}

		public NodeKeyType getNodeKey() {
			return this.nodeKey;
		}

		private Node<NodeKeyType> getCameFrom() {
			return this.cameFrom;
		}

		private void setCameFrom(Node<NodeKeyType> cameFrom) {
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
	}

	public static class ResultingRoute<NodeKeyType> {
		private final int cost;
		private final NodeKeyType[] steps;

		private ResultingRoute(int cost, NodeKeyType[] steps) {
			this.cost = cost;
			this.steps = steps;
		}

		public int getCost() {
			return this.cost;
		}

		public NodeKeyType[] getSteps() {
			return Arrays.copyOf(this.steps,this.steps.length);
		}
	}

	public static <NodeKeyType> Optional<ResultingRoute<NodeKeyType>> run(NodeAdapter<NodeKeyType> nodeAdapter, NodeKeyType fromNodeKey, NodeKeyType toNodeKey) {
		PriorityQueue<Node<NodeKeyType>> openSet = new PriorityQueue<>(Comparator.comparing(Node::getFScore));
		Node<NodeKeyType> fromNode = new Node<>(fromNodeKey);
		fromNode.setGScore(0);
		fromNode.setFScore(nodeAdapter.getCostEstimateOfMovingBetweenNodes(fromNodeKey, toNodeKey));
		Map<NodeKeyType, Node<NodeKeyType>> knownNodes = new HashMap<>();
		knownNodes.put(fromNodeKey, fromNode);
		openSet.add(fromNode);
		while (true) {
			Node<NodeKeyType> currentNode = openSet.poll();
			if (currentNode == null) {
				return Optional.empty();
			}
			NodeKeyType currentNodeKey = currentNode.getNodeKey();
			int currentGScore = currentNode.getGScore();
			if (currentGScore == currentNode.getFScore() && nodeAdapter.isValidEndingNode(currentNodeKey)) {
				int cost = currentNode.getFScore();
				Deque<NodeKeyType> steps = new LinkedList<>();
				steps.addFirst(currentNodeKey);
				Node<NodeKeyType> traversalNode = currentNode;
				while (true) {
					traversalNode = traversalNode.getCameFrom();
					if (traversalNode == null) {
						break;
					}
					steps.addFirst(traversalNode.getNodeKey());
				}
				@SuppressWarnings("unchecked")
				NodeKeyType[] stepsArray = steps.toArray((NodeKeyType[])Array.newInstance(nodeAdapter.getClazz(),steps.size()));
				return Optional.of(
					new ResultingRoute<NodeKeyType>(
						cost,
						stepsArray
					)
				);
			}
			nodeAdapter.getLinkedNodeKeys(
				currentNodeKey,
				linkedNodeKey -> {
					Node<NodeKeyType> linkedNode = knownNodes.get(linkedNodeKey);
					boolean newPoint = false;
					if (linkedNode == null) {
						newPoint = true;
						linkedNode = new Node<>(linkedNodeKey);
						knownNodes.put(linkedNodeKey, linkedNode);
					}
					int potentialGScore = currentGScore + nodeAdapter.getCostOfMovingBetweenLinkedNodes(currentNodeKey, linkedNodeKey);
					if (potentialGScore < linkedNode.getGScore()) {
						if (!newPoint) {
							openSet.remove(linkedNode);
						}
						linkedNode.setCameFrom(currentNode);
						linkedNode.setGScore(potentialGScore);
						linkedNode.setFScore(potentialGScore + nodeAdapter.getCostEstimateOfMovingBetweenNodes(linkedNodeKey, toNodeKey));
						openSet.add(linkedNode);
					}
				}
			);
		}
	}
}
