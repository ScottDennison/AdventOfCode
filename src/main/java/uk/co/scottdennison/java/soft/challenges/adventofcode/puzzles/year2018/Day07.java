package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day07 implements IPuzzle {
    private static final class Node {
        private final char name;
        private Set<Node> parents = new HashSet<>();
        private Set<Node> children = new HashSet<>();
        private boolean complete = false;

        public Node(char name) {
            this.name = name;
        }

        public char getName() {
            return this.name;
        }

        public void addParent(Node parent) {
            this.parents.add(parent);
        }

        public Iterator<Node> iterateParents() {
            return Collections.unmodifiableSet(this.parents).iterator();
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

        public Iterator<Node> iterateChildren() {
            return Collections.unmodifiableSet(this.children).iterator();
        }

        public boolean isComplete() {
            return complete;
        }

        public void markComplete() {
            this.complete = true;
        }
    }

    private static final class Worker {
        private Node currentlyAssignedNode = null;
        private int secondsUntilCompletion = -1;

        public Node getCurrentlyAssignedNode() {
            return currentlyAssignedNode;
        }

        public void setCurrentlyAssignedNode(Node currentlyAssignedNode) {
            this.currentlyAssignedNode = currentlyAssignedNode;
        }

        public int getSecondsUntilCompletion() {
            return secondsUntilCompletion;
        }

        public void setSecondsUntilCompletion(int secondsUntilCompletion) {
            this.secondsUntilCompletion = secondsUntilCompletion;
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^Step (?<node1>[A-Z0-9]) must be finished before step (?<node2>[A-Z0-9]) can begin.");

    private static Map<Character, Node> parseNodes(char[] inputCharacters) {
        Map<Character, Node> nodes = new HashMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_LINE.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse input line");
            }
            Node node1 = nodes.computeIfAbsent(matcher.group("node1").charAt(0),Node::new);
            Node node2 = nodes.computeIfAbsent(matcher.group("node2").charAt(0),Node::new);
            node1.addChild(node2);
            node2.addParent(node1);
        }
        return nodes;
    }

    private static PriorityQueue<Node> buildInitialAvailableNodesPriorityQueue(Map<Character, Node> nodes) {
        PriorityQueue<Node> availableNodes = new PriorityQueue<>(Comparator.comparing(Node::getName));
        for (Node node : nodes.values()) {
            if (!node.iterateParents().hasNext()) {
                availableNodes.add(node);
            }
        }
        return availableNodes;
    }

    private static void completeNode(PriorityQueue<Node> availableNodes, Node node) {
        node.markComplete();
        Iterator<Node> childNodeIterator = node.iterateChildren();
        while (childNodeIterator.hasNext()) {
            Node childNode = childNodeIterator.next();
            boolean allParentsComplete = true;
            Iterator<Node> parentNodeIterator = childNode.iterateParents();
            while (parentNodeIterator.hasNext()) {
                Node parentNode = parentNodeIterator.next();
                if (!parentNode.isComplete()) {
                    allParentsComplete = false;
                    break;
                }
            }
            if (allParentsComplete) {
                availableNodes.add(childNode);
            }
        }
    }

    private static String solvePartA(char[] inputCharacters) {
        Map<Character, Node> nodes = parseNodes(inputCharacters);
        PriorityQueue<Node> availableNodes = buildInitialAvailableNodesPriorityQueue(nodes);
        int nodeCount = nodes.size();
        char[] result = new char[nodeCount];
        int resultIndex = 0;
        Node node;
        while ((node = availableNodes.poll()) != null) {
            result[resultIndex++] = node.getName();
            completeNode(availableNodes, node);
        }
        if (resultIndex != nodeCount) {
            throw new IllegalStateException("Could not fill part A result.");
        }
        return new String(result);
    }

    private static int solvePartB(char[] inputCharacters, int numWorkers, int stepTimeAddition) {
        Map<Character, Node> nodes = parseNodes(inputCharacters);
        PriorityQueue<Node> availableNodes = buildInitialAvailableNodesPriorityQueue(nodes);
        int nodeCount = nodes.size();
        int completeNodeCount = 0;
        int secondsElapsed = 0;
        Deque<Worker> unassignedWorkers = new LinkedList<>();
        Deque<Worker> assignedWorkers = new LinkedList<>();
        for (int workerNumber=0; workerNumber<numWorkers; workerNumber++) {
            unassignedWorkers.add(new Worker());
        }
        while (completeNodeCount < nodeCount) {
            while (!unassignedWorkers.isEmpty() && !availableNodes.isEmpty()) {
                Worker unassignedworker = unassignedWorkers.removeFirst();
                Node availableNode = availableNodes.remove();
                unassignedworker.setCurrentlyAssignedNode(availableNode);
                unassignedworker.setSecondsUntilCompletion(stepTimeAddition + availableNode.getName() - 'A' + 1);
                assignedWorkers.addLast(unassignedworker);
            }
            if (!assignedWorkers.isEmpty()) {
                int minimumSecondsUntilCompletion = Integer.MAX_VALUE;
                Iterator<Worker> assignedWorkersIterator = assignedWorkers.iterator();
                while (assignedWorkersIterator.hasNext()) {
                    int secondsUntilCompletion = assignedWorkersIterator.next().getSecondsUntilCompletion();
                    if (secondsUntilCompletion < minimumSecondsUntilCompletion) {
                        minimumSecondsUntilCompletion = secondsUntilCompletion;
                    }
                }
                secondsElapsed += minimumSecondsUntilCompletion;
                assignedWorkersIterator = assignedWorkers.iterator();
                while (assignedWorkersIterator.hasNext()) {
                    Worker assignedWorker = assignedWorkersIterator.next();
                    int newSecondsUntilCompletion = assignedWorker.getSecondsUntilCompletion() - minimumSecondsUntilCompletion;
                    assignedWorker.setSecondsUntilCompletion(newSecondsUntilCompletion);
                    if (newSecondsUntilCompletion <= 0) {
                        assignedWorkersIterator.remove();
                        completeNode(availableNodes, assignedWorker.getCurrentlyAssignedNode());
                        completeNodeCount++;
                        assignedWorker.setCurrentlyAssignedNode(null);
                        unassignedWorkers.add(assignedWorker);
                    }
                }
            }
        }
        return secondsElapsed;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        return new BasicPuzzleResults<>(
            solvePartA(
                inputCharacters
            ),
            solvePartB(
                inputCharacters,
                Integer.parseInt(new String(configProvider.getPuzzleConfigChars("num_workers"))),
                Integer.parseInt(new String(configProvider.getPuzzleConfigChars("step_time_addition")))
            )
        );
    }
}
