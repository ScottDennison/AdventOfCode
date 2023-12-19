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

public class AStarSolver {
	private AStarSolver() {}

	public static interface CostAdapter<CostType> {
		CostType addCosts(CostType cost1, CostType cost2);
		boolean isCostBetter(CostType newCost, CostType existingCost);
		boolean areCostsEqual(CostType cost1, CostType cost2);
		Comparator<CostType> getComparator();
		CostType getZeroCost();
	}

	public static abstract class IntegerCostAdapter implements CostAdapter<Integer> {
		@Override
		public Integer addCosts(Integer cost1, Integer cost2) {
			return cost1 + cost2;
		}

		@Override
		public boolean areCostsEqual(Integer cost1, Integer cost2) {
			return Objects.equals(cost1, cost2);
		}

		@Override
		public Comparator<Integer> getComparator() {
			return Comparator.naturalOrder();
		}

		@Override
		public Integer getZeroCost() {
			return 0;
		}
	}

	public static class MinimizeIntegerCostAdapter extends IntegerCostAdapter {
		public static final MinimizeIntegerCostAdapter INSTANCE = new MinimizeIntegerCostAdapter();

		private MinimizeIntegerCostAdapter() {}

		@Override
		public boolean isCostBetter(Integer newCost, Integer existingCost) {
			return newCost < existingCost;
		}
	}

	public static interface NodeAdapter<NodeKeyType,CostType> {
		void getLinkedNodeKeys(NodeKeyType fromNodeKey, Consumer<NodeKeyType> linkedNodeKeyConsumer);
		CostType getCostOfMovingBetweenLinkedNodes(NodeKeyType linkedFromNodeKey, NodeKeyType linkedToNodeKey);
		CostType getCostEstimateOfMovingBetweenNodes(NodeKeyType fromNodeKey, NodeKeyType toNodeKey);
		boolean isValidEndingNode(NodeKeyType nodeKey);
		Class<NodeKeyType> getClazz();
	}

	public final static class PointNodeAdapter<CostType> implements NodeAdapter<PointNodeAdapter.Point,CostType> {
		public static interface PointAdapter<CostType> {
			boolean canMoveBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
			CostType getCostOfMovingBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
			CostType getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint);
		}

		public static abstract class OrthagonalEstimatingPointAdapter implements PointAdapter<Integer> {
			@Override
			public final Integer getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
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

		private final PointAdapter<CostType> pointAdapter;
		private final int minY;
		private final int maxY;
		private final int minX;
		private final int maxX;

		public PointNodeAdapter(PointAdapter<CostType> pointAdapter, int minY, int maxY, int minX, int maxX) {
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
		public CostType getCostOfMovingBetweenLinkedNodes(Point linkedFromPoint, Point linkedToPoint) {
			return pointAdapter.getCostOfMovingBetweenLinkedPoints(linkedFromPoint, linkedToPoint);
		}

		@Override
		public CostType getCostEstimateOfMovingBetweenNodes(Point fromPoint, Point toPoint) {
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

	private static class Node<NodeKeyType,CostType> {
		private final NodeKeyType nodeKey;
		private Node<NodeKeyType,CostType> cameFrom;
		private CostType fScore;
		private CostType gScore;

		private Node(NodeKeyType nodeKey, Node<NodeKeyType,CostType> cameFrom, CostType fScore, CostType gScore) {
			this.nodeKey = nodeKey;
			update(cameFrom, fScore, gScore);
		}

		public NodeKeyType getNodeKey() {
			return this.nodeKey;
		}

		public Node<NodeKeyType,CostType> getCameFrom() {
			return this.cameFrom;
		}

		public CostType getFScore() {
			return this.fScore;
		}

		public CostType getGScore() {
			return this.gScore;
		}

		public void update(Node<NodeKeyType,CostType> cameFrom, CostType fScore, CostType gScore) {
			this.cameFrom = cameFrom;
			this.fScore = fScore;
			this.gScore = gScore;
		}
	}

	public static class ResultingRoute<NodeKeyType,CostType> {
		private final CostType cost;
		private final NodeKeyType[] steps;

		private ResultingRoute(CostType cost, NodeKeyType[] steps) {
			this.cost = cost;
			this.steps = steps;
		}

		public CostType getCost() {
			return this.cost;
		}

		public NodeKeyType[] getSteps() {
			return Arrays.copyOf(this.steps,this.steps.length);
		}
	}

	public static <NodeKeyType,CostType> Optional<ResultingRoute<NodeKeyType,CostType>> run(NodeAdapter<NodeKeyType,CostType> nodeAdapter, CostAdapter<CostType> costAdapter, NodeKeyType fromNodeKey, NodeKeyType toNodeKey) {
		Comparator<CostType> costTypeComparator = costAdapter.getComparator();
		PriorityQueue<Node<NodeKeyType,CostType>> openSet = new PriorityQueue<>((node1,node2) -> costTypeComparator.compare(node1.getFScore(),node2.getFScore()));
		Node<NodeKeyType,CostType> fromNode = new Node<>(fromNodeKey,null,nodeAdapter.getCostEstimateOfMovingBetweenNodes(fromNodeKey, toNodeKey),costAdapter.getZeroCost());
		Map<NodeKeyType, Node<NodeKeyType,CostType>> knownNodes = new HashMap<>();
		knownNodes.put(fromNodeKey, fromNode);
		openSet.add(fromNode);
		while (true) {
			Node<NodeKeyType,CostType> currentNode = openSet.poll();
			if (currentNode == null) {
				return Optional.empty();
			}
			NodeKeyType currentNodeKey = currentNode.getNodeKey();
			CostType currentGScore = currentNode.getGScore();
			if (costAdapter.areCostsEqual(currentGScore,currentNode.getFScore()) && nodeAdapter.isValidEndingNode(currentNodeKey)) {
				CostType cost = currentNode.getFScore();
				Deque<NodeKeyType> steps = new LinkedList<>();
				steps.addFirst(currentNodeKey);
				Node<NodeKeyType,CostType> traversalNode = currentNode;
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
					new ResultingRoute<NodeKeyType,CostType>(
						cost,
						stepsArray
					)
				);
			}
			nodeAdapter.getLinkedNodeKeys(
				currentNodeKey,
				linkedNodeKey -> {
					Node<NodeKeyType,CostType> linkedNode = knownNodes.get(linkedNodeKey);
					CostType potentialGScore = costAdapter.addCosts(currentGScore,nodeAdapter.getCostOfMovingBetweenLinkedNodes(currentNodeKey, linkedNodeKey));
					if (linkedNode == null) {
						linkedNode = new Node<>(linkedNodeKey, currentNode, costAdapter.addCosts(potentialGScore,nodeAdapter.getCostEstimateOfMovingBetweenNodes(linkedNodeKey, toNodeKey)), potentialGScore);
						knownNodes.put(linkedNodeKey, linkedNode);
						openSet.add(linkedNode);
					}
					else if (costAdapter.isCostBetter(potentialGScore,linkedNode.getGScore())) {
						openSet.remove(linkedNode);
						linkedNode.update(
							currentNode,
							costAdapter.addCosts(potentialGScore,nodeAdapter.getCostEstimateOfMovingBetweenNodes(linkedNodeKey, toNodeKey)),
							potentialGScore
						);
						openSet.add(linkedNode);
					}
				}
			);
		}
	}
}
