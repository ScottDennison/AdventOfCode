package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Day12 implements IPuzzle {
    private static enum NodeType {
        START,
        BIG_CAVE,
        SMALL_CAVE,
        END
    }

    private static class Node {
        private final String name;
        private final NodeType nodeType;
        private final Set<Node> linkedNodes;

        public Node(String name, NodeType nodeType) {
            this.name = name;
            this.nodeType = nodeType;
            this.linkedNodes = new HashSet<>();
        }

        public String getName() {
            return this.name;
        }

        public NodeType getNodeType() {
            return this.nodeType;
        }

        public void addLink(Node otherNode) {
            this.linkedNodes.add(otherNode);
        }

        public Iterator<Node> getLinkedNodeIterator() {
            return this.linkedNodes.iterator();
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Node> namedNodes = new HashMap<>();
        for (String line : LineReader.strings(inputCharacters)) {
            int separatorIndex = line.indexOf('-');
            if (separatorIndex < 0 || line.indexOf('-',separatorIndex+1) >= 0) {
                throw new IllegalStateException("Invalid line");
            }
            Node node1 = getOrCreateNode(namedNodes,line.substring(0,separatorIndex));
            Node node2 = getOrCreateNode(namedNodes,line.substring(separatorIndex+1));
            if (node1.getNodeType() == NodeType.BIG_CAVE && node2.getNodeType() == NodeType.BIG_CAVE) {
                throw new IllegalStateException("Solution does not support two big caves being linked to each other");
            }
            node1.addLink(node2);
            node2.addLink(node1);
        }
        Collection<Node> allNodes = namedNodes.values();
        Node startNode = null;
        for (Node node : allNodes) {
            if (node.getNodeType() == NodeType.START) {
                if (startNode == null) {
                    startNode = node;
                } else {
                    throw new IllegalStateException("Multiple start nodes");
                }
            }
        }
        if (startNode == null) {
            throw new IllegalStateException("No start node");
        }
        return new BasicPuzzleResults<>(
            recurseCountPaths(startNode, allNodes, false),
            recurseCountPaths(startNode, allNodes, true)
        );
    }

    private static int recurseCountPaths(Node startNode, Collection<Node> allNodes, boolean canVisitSmallCaveTwice) {
        Map<Node,Integer> visitCounts = new HashMap<>();
        for (Node node : allNodes) {
            visitCounts.put(node, 0);
        }
        return recurseCountPaths(startNode, visitCounts, canVisitSmallCaveTwice);
    }

    private static int recurseCountPaths(Node currentNode, Map<Node,Integer> visitCounts, boolean canVisitSmallCaveTwice) {
        NodeType currentNodeType = currentNode.getNodeType();
        if (currentNodeType == NodeType.END) {
            return 1;
        }
        int oldCurrentNodeVisitCount = visitCounts.get(currentNode);
        visitCounts.put(currentNode,oldCurrentNodeVisitCount+1);
        Iterator<Node> linkedNodeIterator = currentNode.getLinkedNodeIterator();
        int totalPaths = 0;
        while (linkedNodeIterator.hasNext()) {
            Node linkedNode = linkedNodeIterator.next();
            NodeType linkedNodeType = linkedNode.getNodeType();
            boolean nextCanVisitSmallCaveTwice = canVisitSmallCaveTwice;
            final boolean canVisit;
            if (linkedNodeType == NodeType.SMALL_CAVE) {
                int linkedNodeVisitCount = visitCounts.get(linkedNode);
                if (linkedNodeVisitCount == 0) {
                    canVisit = true;
                } else if (linkedNodeVisitCount == 1 && canVisitSmallCaveTwice) {
                    nextCanVisitSmallCaveTwice = false;
                    canVisit = true;
                } else {
                    canVisit = false;
                }
            } else if (linkedNodeType == NodeType.START) {
                canVisit = false;
            } else {
                canVisit = true;
            }
            if (canVisit) {
                totalPaths += recurseCountPaths(linkedNode, visitCounts, nextCanVisitSmallCaveTwice);
            }
        }
        visitCounts.put(currentNode,oldCurrentNodeVisitCount);
        return totalPaths;
    }

    private static Node getOrCreateNode(Map<String,Node> namedNodes, String name) {
        Node existingNode = namedNodes.get(name);
        if (existingNode == null) {
            NodeType nodeType;
            if (name.equalsIgnoreCase("start")) {
                nodeType = NodeType.START;
            } else if (name.equalsIgnoreCase("end")) {
                nodeType = NodeType.END;
            } else if (name.toUpperCase(Locale.ENGLISH).equals(name)) {
                nodeType = NodeType.BIG_CAVE;
            } else if (name.toLowerCase(Locale.ENGLISH).equals(name)) {
                nodeType = NodeType.SMALL_CAVE;
            } else {
                throw new IllegalStateException("Could not determine node type");
            }
            Node newNode = new Node(name, nodeType);
            namedNodes.put(name, newNode);
            return newNode;
        } else {
            return existingNode;
        }
    }
}
