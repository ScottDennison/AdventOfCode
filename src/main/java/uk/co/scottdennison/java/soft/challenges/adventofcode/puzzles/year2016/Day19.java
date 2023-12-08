package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.function.IntUnaryOperator;

public class Day19 implements IPuzzle {
    private static class SkipNode {
        public SkipNode childSkipNode;
        public SkipNode parentSkipNode;
        public SkipNode nextSkipNode;
        public SkipNode prevSkipNode;
        public int prevSkipNodeDistance;
        public int nextSkipNodeDistance;
    }

    private static class ElfSkipNode extends SkipNode {
        public int elfNumber;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int totalElves = Integer.parseInt(new String(inputCharacters).trim());
        return new BasicPuzzleResults<>(
            solve(totalElves,remainingElves -> 1),
            solve(totalElves,remainingElves -> remainingElves/2)
        );
    }

    private static int solve(int totalElves, IntUnaryOperator elvesToProgressFunction) {
        int layersRequired = (int)(Math.log(totalElves) / Math.log(2))+1;
        int[] nodesPerLayer = new int[layersRequired+2];

        ElfSkipNode firstElfSkipNode = new ElfSkipNode();
        firstElfSkipNode.elfNumber = 1;
        nodesPerLayer[0]++;
        ElfSkipNode currentElfSkipNode = firstElfSkipNode;
        for (int number=2; number<=totalElves; number++) {
            nodesPerLayer[0]++;
            ElfSkipNode newElfSkipNode = new ElfSkipNode();
            newElfSkipNode.elfNumber = number;
            currentElfSkipNode.nextSkipNode = newElfSkipNode;
            currentElfSkipNode.nextSkipNodeDistance = 1;
            newElfSkipNode.prevSkipNode = currentElfSkipNode;
            newElfSkipNode.prevSkipNodeDistance = 1;
            currentElfSkipNode = newElfSkipNode;
        }
        currentElfSkipNode.nextSkipNode = firstElfSkipNode;
        currentElfSkipNode.nextSkipNodeDistance = 1;
        firstElfSkipNode.prevSkipNode = currentElfSkipNode;
        firstElfSkipNode.prevSkipNodeDistance = 1;

        SkipNode firstSkipNodeInPreviousLayer = firstElfSkipNode;
        for (int layer=1; layer<=layersRequired; layer++) {
            SkipNode firstSkipNodeInThisLayer = new SkipNode();
            nodesPerLayer[layer]++;
            firstSkipNodeInPreviousLayer.parentSkipNode = firstSkipNodeInThisLayer;
            firstSkipNodeInThisLayer.childSkipNode = firstSkipNodeInPreviousLayer;
            SkipNode currentSkipNodeInPreviousLayer = firstSkipNodeInPreviousLayer;
            SkipNode currentSkipNodeInThisLayer = firstSkipNodeInThisLayer;
            int distance;
            while (true) {
                distance = 0;
                SkipNode skippedSkipNodeInPreviousLayer = currentSkipNodeInPreviousLayer.nextSkipNode;
                distance += currentSkipNodeInPreviousLayer.nextSkipNodeDistance;
                if (skippedSkipNodeInPreviousLayer == firstSkipNodeInPreviousLayer) {
                    break;
                }
                SkipNode nextSkipNodeInPreviousLayer = skippedSkipNodeInPreviousLayer.nextSkipNode;
                distance += skippedSkipNodeInPreviousLayer.nextSkipNodeDistance;
                if (nextSkipNodeInPreviousLayer == firstSkipNodeInPreviousLayer) {
                    break;
                }
                SkipNode newSkipNodeInThisLayer = new SkipNode();
                nodesPerLayer[layer]++;
                currentSkipNodeInThisLayer.nextSkipNode = newSkipNodeInThisLayer;
                currentSkipNodeInThisLayer.nextSkipNodeDistance = distance;
                newSkipNodeInThisLayer.prevSkipNode = currentSkipNodeInThisLayer;
                newSkipNodeInThisLayer.prevSkipNodeDistance = distance;
                nextSkipNodeInPreviousLayer.parentSkipNode = newSkipNodeInThisLayer;
                newSkipNodeInThisLayer.childSkipNode = nextSkipNodeInPreviousLayer;
                currentSkipNodeInPreviousLayer = nextSkipNodeInPreviousLayer;
                currentSkipNodeInThisLayer = newSkipNodeInThisLayer;
            }
            currentSkipNodeInThisLayer.nextSkipNode = firstSkipNodeInThisLayer;
            currentSkipNodeInThisLayer.nextSkipNodeDistance = distance;
            firstSkipNodeInThisLayer.prevSkipNode = currentSkipNodeInThisLayer;
            firstSkipNodeInThisLayer.prevSkipNodeDistance = distance;
            firstSkipNodeInPreviousLayer = firstSkipNodeInThisLayer;
        }

        int elvesRemaining = totalElves;
        ElfSkipNode recievingElfSkipNode = firstElfSkipNode;
        while (true) {
            SkipNode searchNode = recievingElfSkipNode;
            int elvesToProgress = elvesToProgressFunction.applyAsInt(elvesRemaining);
            while (elvesToProgress > 0) {
                if (searchNode.nextSkipNodeDistance < elvesToProgress) {
                    while (true) {
                        SkipNode parentNode = searchNode.parentSkipNode;
                        if (parentNode == null || parentNode.nextSkipNodeDistance > elvesToProgress) {
                            break;
                        }
                        searchNode = parentNode;
                    }
                }
                else if (searchNode.nextSkipNodeDistance > elvesToProgress) {
                    while (true) {
                        searchNode = searchNode.childSkipNode;
                        if (searchNode.nextSkipNodeDistance <= elvesToProgress) {
                            break;
                        }
                    }
                }
                elvesToProgress -= searchNode.nextSkipNodeDistance;
                searchNode = searchNode.nextSkipNode;
            }
            while (searchNode.childSkipNode != null) {
                searchNode = searchNode.childSkipNode;
            }
            // SearchNode will be an ElfSearchNode of the elf being removed.
            SkipNode deleteNode = searchNode;
            int layer = 0;
            while (true) {
                SkipNode searchNodeNext = deleteNode.nextSkipNode;
                SkipNode searchNodePrev = deleteNode.prevSkipNode;
                searchNodeNext.prevSkipNode = searchNodePrev;
                searchNodeNext.prevSkipNodeDistance += deleteNode.prevSkipNodeDistance - 1;
                searchNodePrev.nextSkipNode = searchNodeNext;
                searchNodePrev.nextSkipNodeDistance += deleteNode.nextSkipNodeDistance - 1;
                nodesPerLayer[layer]--;
                SkipNode parentNode = deleteNode.parentSkipNode;
                if (parentNode == null) {
                    SkipNode shorterChildCurrentNode = deleteNode;
                    while (nodesPerLayer[layer+1] > 0) {
                        parentNode = shorterChildCurrentNode.parentSkipNode;
                        if (parentNode != null) {
                            parentNode.nextSkipNodeDistance--;
                            parentNode.nextSkipNode.prevSkipNodeDistance--;
                            layer++;
                            shorterChildCurrentNode = parentNode;
                        }
                        else {
                            shorterChildCurrentNode = shorterChildCurrentNode.prevSkipNode;
                        }
                    }
                    break;
                }
                else {
                    layer++;
                    deleteNode = parentNode;
                }
            }
            if (--elvesRemaining == 1) {
                return recievingElfSkipNode.elfNumber;
            }
            recievingElfSkipNode = (ElfSkipNode)recievingElfSkipNode.nextSkipNode;
        }
    }

    private static int getElfNumber(SkipNode skipNode) {
        while (skipNode.childSkipNode != null) {
            skipNode = skipNode.childSkipNode;
        }
        return ((ElfSkipNode)skipNode).elfNumber;
    }
}
