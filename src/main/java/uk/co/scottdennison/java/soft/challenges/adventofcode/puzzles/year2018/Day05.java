package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Day05 implements IPuzzle {
    private static class Node {
        private final char lowPolairtyUnit;
        private final boolean polarized;
        private Node originalPrevious;
        private Node originalNext;
        private Node previous;
        private Node next;

        public Node(char lowPolairtyUnit, boolean polarized) {
            this.lowPolairtyUnit = lowPolairtyUnit;
            this.polarized = polarized;
        }

        public char getLowPolairtyUnit() {
            return this.lowPolairtyUnit;
        }

        public boolean isPolarized() {
            return this.polarized;
        }

        public Node getOriginalPrevious() {
            return this.originalPrevious;
        }

        public void setOriginalPrevious(Node originalPrevious) {
            this.originalPrevious = originalPrevious;
        }

        public Node getOriginalNext() {
            return this.originalNext;
        }

        public void setOriginalNext(Node originalNext) {
            this.originalNext = originalNext;
        }

        public Node getPrevious() {
            return this.previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public Node getNext() {
            return this.next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    private static void restore(Node startNode) {
        Node currentNode = startNode;
        while (currentNode != null) {
            currentNode.setPrevious(currentNode.getOriginalPrevious());
            currentNode.setNext(currentNode.getOriginalNext());
            currentNode = currentNode.getNext();
        }
    }

    private static Node remove(Node startNode, char lowPolarityUnitToRemove) {
        Node currentNode = startNode;
        while (true) {
            if (currentNode.getLowPolairtyUnit() == lowPolarityUnitToRemove) {
                Node nodeBeforeCurrentNode = currentNode.getPrevious();
                Node nodeAfterCurrentNode = currentNode.getNext();
                if (nodeBeforeCurrentNode != null) {
                    nodeBeforeCurrentNode.setNext(nodeAfterCurrentNode);
                }
                if (nodeAfterCurrentNode != null) {
                    nodeAfterCurrentNode.setPrevious(nodeBeforeCurrentNode);
                    currentNode = nodeAfterCurrentNode;
                }
                else {
                    break;
                }
            }
            else {
                Node nextNode = currentNode.getNext();
                if (nextNode == null) {
                    break;
                }
                currentNode = nextNode;
            }
        }
        while (true) {
            Node previousNode = currentNode.getPrevious();
            if (previousNode == null) {
                break;
            }
            currentNode = previousNode;
        }
        return currentNode;
    }

    private static int react(Node startNode) {
        Node currentNode = startNode;
        while (true) {
            Node nextNode = currentNode.getNext();
            if (nextNode == null) {
                break;
            }
            if (currentNode.getLowPolairtyUnit() == nextNode.getLowPolairtyUnit() && currentNode.isPolarized() != nextNode.isPolarized()) {
                Node nodeBeforeCurrentNode = currentNode.getPrevious();
                Node nodeAfterNextNode = nextNode.getNext();
                if (nodeBeforeCurrentNode != null) {
                    nodeBeforeCurrentNode.setNext(nodeAfterNextNode);
                    currentNode = nodeBeforeCurrentNode;
                }
                else {
                    currentNode = nodeAfterNextNode;
                }
                if (nodeAfterNextNode != null) {
                    nodeAfterNextNode.setPrevious(nodeBeforeCurrentNode);
                }
            } else {
                currentNode = nextNode;
            }
        }
        int count = 0;
        while (currentNode != null) {
            count++;
            currentNode = currentNode.getPrevious();
        }
        return count;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Node previousNode = null;
        Node startNode = null;
        Set<Character> knownLowPolarityUnits = new HashSet<>();
        for (char rawUnit : new String(inputCharacters).trim().toCharArray()) {
            char lowPolarityUnit = Character.toLowerCase(rawUnit);
            knownLowPolarityUnits.add(lowPolarityUnit);
            Node newNode = new Node(lowPolarityUnit, rawUnit!=lowPolarityUnit);
            if (startNode == null) {
                startNode = newNode;
            }
            else {
                previousNode.setOriginalNext(newNode);
            }
            newNode.setOriginalPrevious(previousNode);
            previousNode = newNode;
        }

        restore(startNode);
        int partAResult = react(startNode);

        int partBResult = Integer.MAX_VALUE;
        for (char lowPolarityUnitToRemove : knownLowPolarityUnits) {
            restore(startNode);
            partBResult = Math.min(partBResult,react(remove(startNode,lowPolarityUnitToRemove)));
        }

        return new BasicPuzzleResults<>(
            partAResult,
            partBResult
        );
    }
}
