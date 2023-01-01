package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 implements IPuzzle {
    private static final class Valve {
        private final String name;
        private final int flowRate;
        private final Set<String> connectedTunnels;

        public Valve(String name, int flowRate, Set<String> connectedTunnels) {
            this.name = name;
            this.flowRate = flowRate;
            this.connectedTunnels = connectedTunnels;
        }

        public String getName() {
            return this.name;
        }

        public int getFlowRate() {
            return this.flowRate;
        }

        public Set<String> getConnectedTunnels() {
            return Collections.unmodifiableSet(this.connectedTunnels);
        }
    }

    private static final boolean DEBUG = true;
    private static final Pattern PATTERN_LINE = Pattern.compile("^Valve (?<name>[A-Z]+) has flow rate=(?<flowRate>[0-9]+); (?:(?:tunnels lead to valves)|(?:tunnel leads to valve)) (?<connectedTunnels>[A-Z]+(?: *, *[A-Z]+)*)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Valve> valves = new HashMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_LINE.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line.");
            }
            Set<String> connectedTunnels = new HashSet<>();
            for (String connectedTunnel : matcher.group("connectedTunnels").split(",")) {
                if (!connectedTunnels.add(connectedTunnel.trim())) {
                    throw new IllegalStateException("Duplicate connected tunnel");
                }
            }
            Valve valve = new Valve(
                matcher.group("name"),
                Integer.parseInt(matcher.group("flowRate")),
                connectedTunnels
            );
            valves.put(valve.getName(),valve);
        }
        Map<String,Map<String,Integer>> distances = new HashMap<>();
        for (String valveName : valves.keySet()) {
            Set<String> pendingVisitValveNames = new HashSet<>();
            Map<String,Integer> distancesForThisValve = new HashMap<>();
            int distance = 0;
            pendingVisitValveNames.add(valveName);
            while (!pendingVisitValveNames.isEmpty()) {
                Set<String> newPendingVisitValveNames = new HashSet<>();
                for (String pendingVisitValveName : pendingVisitValveNames) {
                    distancesForThisValve.put(pendingVisitValveName,distance);
                    for (String linkedValveName : valves.get(pendingVisitValveName).getConnectedTunnels()) {
                        if (!distancesForThisValve.containsKey(linkedValveName) && !pendingVisitValveNames.contains(linkedValveName)) {
                            newPendingVisitValveNames.add(linkedValveName);
                        }
                    }
                }
                pendingVisitValveNames = newPendingVisitValveNames;
                distance++;
            }
            distances.put(valveName,distancesForThisValve);
        }
        String[] valveNamesOfInterest = valves.entrySet().stream().filter(entry -> entry.getValue().getFlowRate() > 0).map(Map.Entry::getKey).sorted().toArray(String[]::new);
        int valvesOfInterestCount = valveNamesOfInterest.length;
        Map<String,Integer> valveNameToIndexMap = new HashMap<>();
        for (String valveNameOfInterest : valveNamesOfInterest) {
            valveNameToIndexMap.put(valveNameOfInterest,valveNameToIndexMap.size());
        }
        int[] flatValveFlowRates = new int[valvesOfInterestCount];
        int[][] flatBetweenTimes = new int[valvesOfInterestCount][valvesOfInterestCount];
        int[] flatInitialTimes = new int[valvesOfInterestCount];
        Map<String,Integer> initialDistances = distances.get("AA");
        for (String valveNameOfInterest1 : valveNamesOfInterest) {
            int valveIndex1 = valveNameToIndexMap.get(valveNameOfInterest1);
            flatValveFlowRates[valveIndex1] = valves.get(valveNameOfInterest1).getFlowRate();
            flatInitialTimes[valveIndex1] = initialDistances.get(valveNameOfInterest1)+1;
            Map<String,Integer> distancesFromValve1 = distances.get(valveNameOfInterest1);
            for (String valveNameOfInterest2 : valveNamesOfInterest) {
                flatBetweenTimes[valveIndex1][valveNameToIndexMap.get(valveNameOfInterest2)] = distancesFromValve1.get(valveNameOfInterest2)+1;
            }
        }
        return new BasicPuzzleResults<>(
            solve(printWriter,"A",valveNamesOfInterest,valvesOfInterestCount,flatInitialTimes,flatBetweenTimes,flatValveFlowRates,1,30),
            solve(printWriter,"B",valveNamesOfInterest,valvesOfInterestCount,flatInitialTimes,flatBetweenTimes,flatValveFlowRates,2,26)
        );
    }

    private static int solve(PrintWriter printWriter, String part, String[] valveNames, int valveCount, int[] initialTimes, int[][] betweenTimes, int[] flowRates, int actorCount, int time) {
        Solver solver = new Solver(valveCount, valveNames, initialTimes, betweenTimes, flowRates, actorCount, time);
        solver.solve();
        if (DEBUG) {
            printWriter.format("==== Part %s ====%n",part);
            solver.summarize(printWriter);
        }
        return solver.getBestPressureRelieved();
    }

    private static class Solver {
        private final int valveCount;
        private final String[] valveNames;
        private final int[] initialTimes;
        private final int[][] betweenTimes;
        private final int[] flowRates;
        private final boolean[] assignedValves;
        private final int actorCount;
        private final int[] actorTargetLocations;
        private final int[] actorTimesUntilUnbusy;
        private final int[] actorValveOpenHistoryCounts;
        private final int[][] actorValveOpenHistory;
        private final int maxHistoryCount;
        private final int time;

        private boolean previouslySolved = false;
        private int bestPressureRelieved = -1;
        private final int[] resultActorValveOpenHistoryCounts;
        private final int[][] resultActorValveOpenHistory;

        public Solver(int valveCount, String[] valveNames, int[] initialTimes, int[][] betweenTimes, int[] flowRates, int actorCount, int time) {
            this.valveCount = valveCount;
            this.valveNames = valveNames;
            this.initialTimes = initialTimes;
            this.betweenTimes = betweenTimes;
            this.flowRates = flowRates;
            this.assignedValves = new boolean[valveCount];
            this.actorCount = actorCount;
            this.actorTargetLocations = new int[actorCount];
            this.actorTimesUntilUnbusy = new int[actorCount];
            this.maxHistoryCount = Math.min(time,valveCount);
            this.actorValveOpenHistoryCounts = new int[actorCount];
            this.actorValveOpenHistory = new int[actorCount][this.maxHistoryCount];
            this.resultActorValveOpenHistoryCounts = new int[actorCount];
            this.resultActorValveOpenHistory = new int[actorCount][this.maxHistoryCount];
            this.time = time;
        }

        public synchronized void solve() {
            Arrays.fill(this.assignedValves, false);
            Arrays.fill(this.actorTargetLocations, -1);
            Arrays.fill(this.actorTimesUntilUnbusy, -1);
            Arrays.fill(this.actorValveOpenHistoryCounts, 0);
            for (int actorIndex=0; actorIndex>actorCount; actorIndex++) {
                Arrays.fill(this.actorValveOpenHistory[actorIndex], this.maxHistoryCount);
            }
            this.bestPressureRelieved = -1;
            this.solveInitialAssignment(0);
            this.previouslySolved = true;
        }

        private void solveInitialAssignment(int actorIndex) {
            if (actorIndex == this.actorCount) {
                this.solveMain(this.time, 0, 0);
            } else {
                int nextActorIndex = actorIndex+1;
                int bestPressureRelieved = 0;
                for (int valveIndex=0; valveIndex<this.valveCount; valveIndex++) {
                    if (this.initialTimes[valveIndex] < this.time && !this.assignedValves[valveIndex]) {
                        this.actorTargetLocations[actorIndex] = valveIndex;
                        this.actorTimesUntilUnbusy[actorIndex] = this.initialTimes[valveIndex];
                        this.assignedValves[valveIndex] = true;
                        this.solveInitialAssignment(nextActorIndex);
                        this.assignedValves[valveIndex] = false;
                    }
                }
            }
        }

        private final void solveMain(int remainingTime, int currentPressureReliefRate, int currentPressureRelieved) {
            int minTimeUntilUnbusy = Integer.MAX_VALUE;
            int actorFinishingWorkIndex = -1;
            for (int actorIndex=0; actorIndex<this.actorCount; actorIndex++) {
                int actorTimeUntilUnbusy = this.actorTimesUntilUnbusy[actorIndex];
                if (actorTimeUntilUnbusy < minTimeUntilUnbusy) {
                    actorFinishingWorkIndex = actorIndex;
                    minTimeUntilUnbusy = actorTimeUntilUnbusy;
                }
            }
            if (remainingTime < minTimeUntilUnbusy) {
                currentPressureRelieved += remainingTime*currentPressureReliefRate;
                if (currentPressureRelieved > this.bestPressureRelieved) {
                    this.bestPressureRelieved = currentPressureRelieved;
                    for (int actorIndex=0; actorIndex<this.actorCount; actorIndex++) {
                        int thisActorValveOpenHistoryCount = this.actorValveOpenHistoryCounts[actorIndex];
                        int[] thisActorValveOpenHistory = this.actorValveOpenHistory[actorIndex];
                        int[] resultThisActorValveOpenHistory = this.resultActorValveOpenHistory[actorIndex];
                        this.resultActorValveOpenHistoryCounts[actorIndex] = thisActorValveOpenHistoryCount;
                        for (int historyIndex=0; historyIndex<thisActorValveOpenHistoryCount; historyIndex++) {
                            resultThisActorValveOpenHistory[historyIndex] = thisActorValveOpenHistory[historyIndex];
                        }
                    }
                }
            } else {
                for (int actorIndex=0; actorIndex<this.actorCount; actorIndex++) {
                    this.actorTimesUntilUnbusy[actorIndex] -= minTimeUntilUnbusy;
                }
                int currentValveIndex = this.actorTargetLocations[actorFinishingWorkIndex];
                this.actorValveOpenHistory[actorFinishingWorkIndex][this.actorValveOpenHistoryCounts[actorFinishingWorkIndex]++] = currentValveIndex;
                currentPressureRelieved += minTimeUntilUnbusy * currentPressureReliefRate;
                currentPressureReliefRate += this.flowRates[currentValveIndex];
                remainingTime -= minTimeUntilUnbusy;
                boolean newWorkAvaialble = false;
                int[] betweenTimesFromCurrentValve = this.betweenTimes[currentValveIndex];
                for (int newTargetValveIndex = 0; newTargetValveIndex < this.valveCount; newTargetValveIndex++) {
                    if (!this.assignedValves[newTargetValveIndex]) {
                        newWorkAvaialble = true;
                        this.actorTargetLocations[actorFinishingWorkIndex] = newTargetValveIndex;
                        this.actorTimesUntilUnbusy[actorFinishingWorkIndex] = betweenTimesFromCurrentValve[newTargetValveIndex];
                        this.assignedValves[newTargetValveIndex] = true;
                        this.solveMain(remainingTime, currentPressureReliefRate, currentPressureRelieved);
                        this.assignedValves[newTargetValveIndex] = false;
                    }
                }
                if (newWorkAvaialble) {
                    this.actorTargetLocations[actorFinishingWorkIndex] = currentValveIndex;
                } else {
                    this.actorTimesUntilUnbusy[actorFinishingWorkIndex] = Integer.MAX_VALUE;
                    this.solveMain(remainingTime, currentPressureReliefRate, currentPressureRelieved);
                }
                this.actorValveOpenHistoryCounts[actorFinishingWorkIndex]--;
                this.actorTimesUntilUnbusy[actorFinishingWorkIndex] = 0;
                for (int actorIndex=0; actorIndex<this.actorCount; actorIndex++) {
                    this.actorTimesUntilUnbusy[actorIndex] += minTimeUntilUnbusy;
                }
            }
        }

        public synchronized void summarize(PrintWriter printWriter) {
            for (int actorIndex=0; actorIndex<this.actorCount; actorIndex++) {
                printWriter.format("Actor #%d%n",actorIndex+1);
                int thisActorValveOpenHistoryCount = this.resultActorValveOpenHistoryCounts[actorIndex];
                int[] thisActorValveOpenHistory = this.resultActorValveOpenHistory[actorIndex];
                int timeElapsed = 0;
                int lastValveIndex = -1;
                for (int historyIndex=0; historyIndex<thisActorValveOpenHistoryCount; historyIndex++) {
                    int valveIndex = thisActorValveOpenHistory[historyIndex];
                    if (historyIndex == 0) {
                        timeElapsed += this.initialTimes[valveIndex];
                    } else {
                        timeElapsed += this.betweenTimes[lastValveIndex][valveIndex];
                    }
                    lastValveIndex = valveIndex;
                    printWriter.format("  Valve %s opened at time %d%n",this.valveNames[valveIndex],timeElapsed);
                }
            }
        }

        public synchronized int getBestPressureRelieved() {
            if (!this.previouslySolved) {
                throw new IllegalStateException("No solution.");
            }
            return this.bestPressureRelieved;
        }
    }

    /*
    private static int recurse(Map<String,Valve> valves, Map<String,Map<String,Integer>> distances, Set<String> valveNamesOfInterest, Set<String> onValves, String currentValve, int remainingTime, int currentPressureReliefRate, int currentPressureRelieved) {
        int bestPressureRelieved = currentPressureRelieved + (remainingTime*currentPressureReliefRate);
        Map<String, Integer> distancesFromCurrentValve = distances.get(currentValve);
        for (String targetValveName : valveNamesOfInterest) {
            if (!onValves.contains(targetValveName)) {
                int timeToTurnOnValve = distancesFromCurrentValve.get(targetValveName)+1;
                int remainingTimeAfterTurningOnValve = remainingTime-timeToTurnOnValve;
                if (remainingTime >= 0) {
                    onValves.add(targetValveName);
                    bestPressureRelieved = Math.max(bestPressureRelieved, recurse(valves, distances, valveNamesOfInterest, onValves, targetValveName, remainingTimeAfterTurningOnValve, currentPressureReliefRate + valves.get(targetValveName).getFlowRate(), currentPressureRelieved+(timeToTurnOnValve*currentPressureReliefRate)));
                    onValves.remove(targetValveName);
                }
            }
        }
        return bestPressureRelieved;
    }
    */
}
