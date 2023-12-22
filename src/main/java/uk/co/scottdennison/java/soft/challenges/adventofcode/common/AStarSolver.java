package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public class AStarSolver {
	private AStarSolver() {}

	public static interface CostAdapter<CostType> {
		CostType addCosts(CostType cost1, CostType cost2);
		Comparator<CostType> getComparator();
		CostType getZeroCost();

		public static final class CommonTypes {
			public static final class Of {
				private static abstract class AbstractComparableCostAdapter<T extends Comparable<T>> implements CostAdapter<T> {
					@Override
					public Comparator<T> getComparator() {
						return Comparator.naturalOrder();
					}
				}

				public static final class Integer extends AbstractComparableCostAdapter<java.lang.Integer> {
					public static final Integer INSTANCE = new Integer();

					private Integer() {}

					@Override
					public java.lang.Integer addCosts(java.lang.Integer cost1, java.lang.Integer cost2) {
						return cost1 + cost2;
					}

					@Override
					public java.lang.Integer getZeroCost() {
						return 0;
					}
				}

				public static final class Long extends AbstractComparableCostAdapter<java.lang.Long> {
					public static final Long INSTANCE = new Long();

					private Long() {}

					@Override
					public java.lang.Long addCosts(java.lang.Long cost1, java.lang.Long cost2) {
						return cost1 + cost2;
					}

					@Override
					public java.lang.Long getZeroCost() {
						return 0L;
					}
				}

				public static final class Float extends AbstractComparableCostAdapter<java.lang.Float> {
					public static final Float INSTANCE = new Float();

					private Float() {}

					@Override
					public java.lang.Float addCosts(java.lang.Float cost1, java.lang.Float cost2) {
						return cost1 + cost2;
					}

					@Override
					public java.lang.Float getZeroCost() {
						return 0F;
					}
				}

				public static final class Double extends AbstractComparableCostAdapter<java.lang.Double> {
					public static final Double INSTANCE = new Double();

					private Double() {}

					@Override
					public java.lang.Double addCosts(java.lang.Double cost1, java.lang.Double cost2) {
						return cost1 + cost2;
					}

					@Override
					public java.lang.Double getZeroCost() {
						return 0D;
					}
				}

				public static final class BigInteger extends AbstractComparableCostAdapter<java.math.BigInteger> {
					public static final BigInteger INSTANCE = new BigInteger();

					private BigInteger() {}

					@Override
					public java.math.BigInteger addCosts(java.math.BigInteger cost1, java.math.BigInteger cost2) {
						return cost1.add(cost2);
					}

					@Override
					public java.math.BigInteger getZeroCost() {
						return java.math.BigInteger.ZERO;
					}
				}

				public static final class BigDecimal extends AbstractComparableCostAdapter<java.math.BigDecimal> {
					public static final BigDecimal INSTANCE = new BigDecimal();

					private BigDecimal() {}

					@Override
					public java.math.BigDecimal addCosts(java.math.BigDecimal cost1, java.math.BigDecimal cost2) {
						return cost1.add(cost2);
					}

					@Override
					public java.math.BigDecimal getZeroCost() {
						return java.math.BigDecimal.ZERO;
					}
				}

				private Of() {}
			}

			private CommonTypes() {}
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
		@FunctionalInterface
		public static interface CanMoveAdapter {
			boolean canMoveBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
		}

		public static final class AlwaysTrueCanMoveAdapter implements CanMoveAdapter {
			public static final AlwaysTrueCanMoveAdapter INSTANCE = new AlwaysTrueCanMoveAdapter();

			private AlwaysTrueCanMoveAdapter() {}

			@Override
			public boolean canMoveBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint) {
				return true;
			}
		}

		@FunctionalInterface
		public static interface ActualMoveCostAdapter<CostType> {
			CostType getCostOfMovingBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint);
		}

		public static final class UnchangingActualMoveCostAdapter<CostType> implements ActualMoveCostAdapter<CostType> {
			public static final class One {
				public static final class Of {
					public static final UnchangingActualMoveCostAdapter<Integer> INTEGER = new UnchangingActualMoveCostAdapter<>(1);
					public static final UnchangingActualMoveCostAdapter<Long> LONG = new UnchangingActualMoveCostAdapter<>(1L);
					public static final UnchangingActualMoveCostAdapter<Float> FLOAT = new UnchangingActualMoveCostAdapter<>(1F);
					public static final UnchangingActualMoveCostAdapter<Double> DOUBLE = new UnchangingActualMoveCostAdapter<>(1D);
					public static final UnchangingActualMoveCostAdapter<BigInteger> BIGINTEGER = new UnchangingActualMoveCostAdapter<>(BigInteger.ONE);
					public static final UnchangingActualMoveCostAdapter<BigDecimal> BIGDECIMAL = new UnchangingActualMoveCostAdapter<>(BigDecimal.ONE);

					private Of() {}
				}

				private One() {}
			}

			private final CostType cost;

			public UnchangingActualMoveCostAdapter(CostType cost) {
				this.cost = cost;
			}

			@Override
			public CostType getCostOfMovingBetweenLinkedPoints(Point linkedFromPoint, Point linkedToPoint) {
				return cost;
			}
		}

		@FunctionalInterface
		public static interface EstimatedMoveCostAdapter<CostType> {
			CostType getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint);

			public static final class CommonAlgorithms {
				public static final class Manhattan {
					public static final class Of {
						public static final class Integer implements EstimatedMoveCostAdapter<java.lang.Integer> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Integer INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Integer();

							private Integer() {}

							@Override
							public final java.lang.Integer getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return Math.abs(toPoint.getY()-fromPoint.getY())+Math.abs(toPoint.getX()-fromPoint.getX());
							}
						}

						public static final class Long implements EstimatedMoveCostAdapter<java.lang.Long> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Long INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Long();

							private Long() {}

							@Override
							public final java.lang.Long getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return Math.abs((long)toPoint.getY()-(long)fromPoint.getY())+Math.abs((long)toPoint.getX()-(long)fromPoint.getX());
							}
						}

						public static final class Float implements EstimatedMoveCostAdapter<java.lang.Float> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Float INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Float();

							private Float() {}

							@Override
							public final java.lang.Float getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return (float)Math.abs((long)toPoint.getY()-(long)fromPoint.getY())+Math.abs((long)toPoint.getX()-(long)fromPoint.getX());
							}
						}

						public static final class Double implements EstimatedMoveCostAdapter<java.lang.Double> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Double INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Double();

							private Double() {}

							@Override
							public final java.lang.Double getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return (double)Math.abs((long)toPoint.getY()-(long)fromPoint.getY())+Math.abs((long)toPoint.getX()-(long)fromPoint.getX());
							}
						}

						public static final class BigInteger implements EstimatedMoveCostAdapter<java.math.BigInteger> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.BigInteger INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.BigInteger();

							private BigInteger() {}

							@Override
							public final java.math.BigInteger getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return java.math.BigInteger.valueOf(Math.abs((long)toPoint.getY()-(long)fromPoint.getY())+Math.abs((long)toPoint.getX()-(long)fromPoint.getX()));
							}
						}

						public static final class BigDecimal implements EstimatedMoveCostAdapter<java.math.BigDecimal> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.BigDecimal INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.BigDecimal();

							private BigDecimal() {}

							@Override
							public final java.math.BigDecimal getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return java.math.BigDecimal.valueOf(Math.abs((long)toPoint.getY()-(long)fromPoint.getY())+Math.abs((long)toPoint.getX()-(long)fromPoint.getX()));
							}
						}

						private Of() {}
					}

					private Manhattan() {}
				}

				public static final class Diagonal {
					public static final class Of {
						public static final class Integer implements EstimatedMoveCostAdapter<java.lang.Integer> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Integer INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Integer();

							private Integer() {}

							@Override
							public final java.lang.Integer getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return Math.max(Math.abs(toPoint.getY()-fromPoint.getY()),Math.abs(toPoint.getX()-fromPoint.getX()));
							}
						}

						public static final class Long implements EstimatedMoveCostAdapter<java.lang.Long> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Long INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Long();

							private Long() {}

							@Override
							public final java.lang.Long getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return Math.max(Math.abs((long)toPoint.getY()-(long)fromPoint.getY()),Math.abs((long)toPoint.getX()-(long)fromPoint.getX()));
							}
						}

						public static final class Float implements EstimatedMoveCostAdapter<java.lang.Float> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Float INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Float();

							private Float() {}

							@Override
							public final java.lang.Float getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return (float)Math.max(Math.abs((long)toPoint.getY()-(long)fromPoint.getY()),Math.abs((long)toPoint.getX()-(long)fromPoint.getX()));
							}
						}

						public static final class Double implements EstimatedMoveCostAdapter<java.lang.Double> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Double INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.Double();

							private Double() {}

							@Override
							public final java.lang.Double getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return (double)Math.max(Math.abs((long)toPoint.getY()-(long)fromPoint.getY()),Math.abs((long)toPoint.getX()-(long)fromPoint.getX()));
							}
						}

						public static final class BigInteger implements EstimatedMoveCostAdapter<java.math.BigInteger> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.BigInteger INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.BigInteger();

							private BigInteger() {}

							@Override
							public final java.math.BigInteger getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return java.math.BigInteger.valueOf(Math.max(Math.abs((long)toPoint.getY()-(long)fromPoint.getY()),Math.abs((long)toPoint.getX()-(long)fromPoint.getX())));
							}
						}

						public static final class BigDecimal implements EstimatedMoveCostAdapter<java.math.BigDecimal> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.BigDecimal INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Diagonal.Of.BigDecimal();

							private BigDecimal() {}

							@Override
							public final java.math.BigDecimal getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								return java.math.BigDecimal.valueOf(Math.max(Math.abs((long)toPoint.getY()-(long)fromPoint.getY()),Math.abs((long)toPoint.getX()-(long)fromPoint.getX())));
							}
						}

						private Of() {}
					}

					private Diagonal() {}
				}

				public static final class Euclidean {
					public static final class Of {
						public static final class Integer implements EstimatedMoveCostAdapter<java.lang.Integer> {
							private final double scale;

							private Integer(double scale) {
								this.scale = scale;
							}

							@Override
							public final java.lang.Integer getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								int yTheta = (toPoint.getY() - fromPoint.getY());
								int xTheta = (toPoint.getX() - fromPoint.getX());
								return (int)(scale*Math.sqrt((yTheta*yTheta)+(xTheta*xTheta)));
							}

							public static final Integer createWithScale(int scale) {
								return new Integer(scale);
							}
						}

						public static final class Long implements EstimatedMoveCostAdapter<java.lang.Long> {
							private final double scale;

							private Long(double scale) {
								this.scale = scale;
							}

							@Override
							public final java.lang.Long getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								long yTheta = ((long)toPoint.getY() - (long)fromPoint.getY());
								long xTheta = ((long)toPoint.getX() - (long)fromPoint.getX());
								return (long)(scale*Math.sqrt((yTheta*yTheta)+(xTheta*xTheta)));
							}

							public static final Long createWithScale(long scale) {
								return new Long(scale);
							}
						}

						public static final class Float implements EstimatedMoveCostAdapter<java.lang.Float> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.Float INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.Float();

							private Float() {}

							@Override
							public final java.lang.Float getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								float yTheta = ((long)toPoint.getY() - (long)fromPoint.getY());
								float xTheta = ((long)toPoint.getX() - (long)fromPoint.getX());
								return (float)Math.sqrt((yTheta*yTheta)+(xTheta*xTheta));
							}
						}

						public static final class Double implements EstimatedMoveCostAdapter<java.lang.Double> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.Double INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.Double();

							private Double() {}

							@Override
							public final java.lang.Double getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								double yTheta = ((long)toPoint.getY() - (long)fromPoint.getY());
								double xTheta = ((long)toPoint.getX() - (long)fromPoint.getX());
								return Math.sqrt((yTheta*yTheta)+(xTheta*xTheta));
							}
						}

						public static final class BigInteger implements EstimatedMoveCostAdapter<java.math.BigInteger> {
							private final double scale;

							private BigInteger(double scale) {
								this.scale = scale;
							}

							@Override
							public final java.math.BigInteger getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								// Unfortunately java 8 does not have a sqrt() method on BigInteger. This is however introduced in java 9. For now, just use the same approach as .Of.Long
								long yTheta = ((long)toPoint.getY() - (long)fromPoint.getY());
								long xTheta = ((long)toPoint.getX() - (long)fromPoint.getX());
								return java.math.BigInteger.valueOf((long)(scale*Math.sqrt((yTheta*yTheta)+(xTheta*xTheta))));
							}

							public static final BigInteger createWithScale(long scale) {
								return new BigInteger(scale);
							}
						}

						public static final class BigDecimal implements EstimatedMoveCostAdapter<java.math.BigDecimal> {
							public static final EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.BigDecimal INSTANCE = new EstimatedMoveCostAdapter.CommonAlgorithms.Euclidean.Of.BigDecimal();

							private BigDecimal() {}

							@Override
							public final java.math.BigDecimal getCostEstimateOfMovingBetweenPoints(Point fromPoint, Point toPoint) {
								// Unfortunately java 8 does not have a sqrt() method on BigDecimal. This is however introduced in java 9. For now, just use the same approach as .Of.Double
								double yTheta = ((long)toPoint.getY() - (long)fromPoint.getY());
								double xTheta = ((long)toPoint.getX() - (long)fromPoint.getX());
								return java.math.BigDecimal.valueOf(Math.sqrt((yTheta*yTheta)+(xTheta*xTheta)));
							}
						}

						private Of() {}
					}

					private Euclidean() {}
				}

				private CommonAlgorithms() {}
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

		private final CanMoveAdapter canMoveAdapter;
		private final ActualMoveCostAdapter<CostType> actualMoveCostAdapter;
		private final EstimatedMoveCostAdapter<CostType> estimatedMoveCostAdapter;
		private final int minY;
		private final int maxY;
		private final int minX;
		private final int maxX;

		public PointNodeAdapter(CanMoveAdapter canMoveAdapter, ActualMoveCostAdapter<CostType> actualMoveCostAdapter, EstimatedMoveCostAdapter<CostType> estimatedMoveCostAdapter, int minY, int maxY, int minX, int maxX) {
			this.canMoveAdapter = canMoveAdapter;
			this.actualMoveCostAdapter = actualMoveCostAdapter;
			this.estimatedMoveCostAdapter = estimatedMoveCostAdapter;
			this.minY = minY;
			this.maxY = maxY;
			this.minX = minX;
			this.maxX = maxX;
		}

		private void checkAndConsumeLink(Point fromPoint, Consumer<Point> linkedPointConsumer, int toY, int toX) {
			Point toPoint = new Point(toY, toX);
			if (canMoveAdapter.canMoveBetweenLinkedPoints(fromPoint, toPoint)) {
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
			return actualMoveCostAdapter.getCostOfMovingBetweenLinkedPoints(linkedFromPoint, linkedToPoint);
		}

		@Override
		public CostType getCostEstimateOfMovingBetweenNodes(Point fromPoint, Point toPoint) {
			return estimatedMoveCostAdapter.getCostEstimateOfMovingBetweenPoints(fromPoint, toPoint);
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
			if (costTypeComparator.compare(currentGScore,currentNode.getFScore()) == 0 && nodeAdapter.isValidEndingNode(currentNodeKey)) {
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
					else if (costTypeComparator.compare(potentialGScore,linkedNode.getGScore()) < 0) {
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
