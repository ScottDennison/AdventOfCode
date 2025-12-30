package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Day08 implements IPuzzle {
    private static class Circuit implements Iterable<JunctionBox> {
        private final int number;
        private final Set<JunctionBox> junctionBoxes;

        private Circuit(int number, Set<JunctionBox> junctionBoxes) {
            this.number = number;
            this.junctionBoxes = new TreeSet<>(Comparator.comparing(JunctionBox::getNumber));
            this.junctionBoxes.addAll(junctionBoxes);
        }

        public int getNumber() {
            return this.number;
        }

        public int getConnectionCount() {
            return junctionBoxes.size();
        }

        public void merge(Circuit otherCircuit) {
            if (otherCircuit != this) {
                this.junctionBoxes.addAll(otherCircuit.junctionBoxes);
                for (JunctionBox junctionBox : otherCircuit.junctionBoxes) {
                    junctionBox.setCurrentCircuit(this);
                }
                otherCircuit.junctionBoxes.clear();
            }
        }

        @Override
        public Iterator<JunctionBox> iterator() {
            return junctionBoxes.iterator();
        }

        @Override
        public String toString() {
            return "Circuit{" +
                "number=" + this.number + ", " +
                "junctionBoxes=" + this.junctionBoxes.stream().map(JunctionBox::getNumber).map(junctionBoxNumber -> Integer.toString(junctionBoxNumber)).collect(Collectors.joining(",","[","]")) + "}";
        }
    }

    private static class JunctionBox {
        private final int number;
        private final int x;
        private final int y;
        private final int z;
        private Circuit currentCircuit;

        public JunctionBox(int number, int x, int y, int z) {
            this.number = number;
            this.x = x;
            this.y = y;
            this.z = z;
            this.currentCircuit = null;
        }

        public int getNumber() {
            return this.number;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public Circuit getCurrentCircuit() {
            return this.currentCircuit;
        }

        public void setCurrentCircuit(Circuit currentCircuit) {
            this.currentCircuit = currentCircuit;
        }

        @Override
        public String toString() {
            return "JunctionBox{" +
                "number=" + this.number + ", " +
                "x=" + this.x + ", " +
                "y=" + this.y + ", " +
                "z=" + this.z + ", " +
                "currentCircuit=" + this.currentCircuit + "}";
        }
    }

    private static class Pairing {
        private final JunctionBox source;
        private final JunctionBox dest;
        private final double distance;

        public Pairing(JunctionBox source, JunctionBox dest) {
            this.source = source;
            this.dest = dest;
            long xDistance = source.getX() - dest.getX();
            long yDistance = source.getY() - dest.getY();
            long zDistance = source.getZ() - dest.getZ();
            this.distance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance) + (zDistance * zDistance));
        }

        public JunctionBox getSource() {
            return this.source;
        }

        public JunctionBox getDest() {
            return this.dest;
        }

        public double getDistance() {
            return this.distance;
        }

        @Override
        public String toString() {
            return "Pairing{" +
                "source=" + this.source + ", " +
                "dest=" + this.dest + ", " +
                "distance=" + this.distance + "}";
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int pairingsToMake = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("pairings_to_make")).trim());
        int junctionBoxCount = inputLines.length;
        JunctionBox[] junctionBoxes = new JunctionBox[junctionBoxCount];
        Circuit[] circuits = new Circuit[junctionBoxCount];
        for (int junctionBoxIndex = 0; junctionBoxIndex < junctionBoxCount; junctionBoxIndex++) {
            String[] junctionBoxCoordinateParts = inputLines[junctionBoxIndex].split(",");
            JunctionBox junctionBox = new JunctionBox(
                junctionBoxIndex,
                Integer.parseInt(junctionBoxCoordinateParts[0]),
                Integer.parseInt(junctionBoxCoordinateParts[1]),
                Integer.parseInt(junctionBoxCoordinateParts[2])
            );
            junctionBoxes[junctionBoxIndex] = junctionBox;
            Circuit circuit = new Circuit(junctionBoxIndex, Collections.singleton(junctionBox));
            circuits[junctionBoxIndex] = circuit;
            junctionBox.setCurrentCircuit(circuit);
        }
        PriorityQueue<Pairing> pairingsPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Pairing::getDistance).reversed());
        for (int junctionBoxIndex1 = 0; junctionBoxIndex1 < junctionBoxCount; junctionBoxIndex1++) {
            JunctionBox source = junctionBoxes[junctionBoxIndex1];
            for (int junctionBoxIndex2 = junctionBoxIndex1 + 1; junctionBoxIndex2 < junctionBoxCount; junctionBoxIndex2++) {
                JunctionBox dest = junctionBoxes[junctionBoxIndex2];
                pairingsPriorityQueue.add(new Pairing(source, dest));
                if (pairingsPriorityQueue.size() > pairingsToMake) {
                    pairingsPriorityQueue.poll();
                }
            }
        }
        Pairing[] pairings = pairingsPriorityQueue.toArray(new Pairing[0]);
        Arrays.sort(pairings, Comparator.comparing(Pairing::getDistance));
        for (int pairingIndex=0; pairingIndex<pairingsToMake; pairingIndex++) {
            Pairing pairing = pairings[pairingIndex];
            pairing.getSource().getCurrentCircuit().merge(pairing.getDest().getCurrentCircuit());
        }
        Arrays.sort(circuits, Comparator.comparing(Circuit::getConnectionCount).reversed());
        int partATotal = circuits[0].getConnectionCount() * circuits[1].getConnectionCount() * circuits[2].getConnectionCount();
        return new BasicPuzzleResults<>(
            partATotal,
            null
        );
    }
}
