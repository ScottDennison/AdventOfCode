package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static class IntermediateState {
        private final int currentValveIndex;
        private final int openedValveBitset;
        private final int timeElapsed;
        private final transient int pressureReliefRate;
        private final int pressureRelieved;

        private IntermediateState(int currentValveIndex, int openedValveBitset, int timeElapsed, int pressureReliefRate, int pressureRelieved) {
            this.currentValveIndex = currentValveIndex;
            this.openedValveBitset = openedValveBitset;
            this.timeElapsed = timeElapsed;
            this.pressureReliefRate = pressureReliefRate;
            this.pressureRelieved = pressureRelieved;
        }

        public int getCurrentValveIndex() {
            return this.currentValveIndex;
        }

        public int getOpenedValveBitset() {
            return this.openedValveBitset;
        }

        public int getTimeElapsed() {
            return this.timeElapsed;
        }

        public int getPressureReliefRate() {
            return this.pressureReliefRate;
        }

        public int getPressureRelieved() {
            return this.pressureRelieved;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            IntermediateState otherState = (IntermediateState) otherObject;

            if (this.currentValveIndex != otherState.currentValveIndex) return false;
            if (this.openedValveBitset != otherState.openedValveBitset) return false;
            if (this.timeElapsed != otherState.timeElapsed) return false;
            if (this.pressureRelieved != otherState.pressureRelieved) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.currentValveIndex;
            result = 31 * result + this.openedValveBitset;
            result = 31 * result + this.timeElapsed;
            result = 31 * result + this.pressureRelieved;
            return result;
        }
    }

    private static final class FinalState {
        private final int openedValveBitset;
        private final int totalPressureRelieved;

        public FinalState(int openedValveBitset, int totalPressureRelieved) {
            this.openedValveBitset = openedValveBitset;
            this.totalPressureRelieved = totalPressureRelieved;
        }

        public int getOpenedValveBitset() {
            return this.openedValveBitset;
        }

        public int getTotalPressureRelieved() {
            return this.totalPressureRelieved;
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^Valve (?<name>[A-Z]+) has flow rate=(?<flowRate>[0-9]+); (?:(?:tunnels lead to valves)|(?:tunnel leads to valve)) (?<connectedTunnels>[A-Z]+(?: *, *[A-Z]+)*)$");
    private static final String STARTING_VALVE_NAME = "AA";

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String, Valve> allValves = parseValves(inputCharacters);
        Map<String, Map<String, Integer>> allValvesDistancesMap = createDistancesMap(allValves);
        Valve startingValve = allValves.get(STARTING_VALVE_NAME);
        if (startingValve == null) {
            throw new IllegalStateException("Starting valve does not exist");
        }
        Valve[] valvesOfInterest = Stream.concat(Stream.of(startingValve),allValves.values().stream().filter(valve -> valve.getFlowRate() > 0 && !valve.getName().equals(STARTING_VALVE_NAME)).sorted(Comparator.comparing(Valve::getName))).toArray(Valve[]::new);
        int valvesOfInterestCount = valvesOfInterest.length;
        if (valvesOfInterestCount > Integer.SIZE) {
            throw new IllegalStateException("Too many valves of interest");
        }
        int[][] distanceBetweenValves = new int[valvesOfInterestCount][valvesOfInterestCount];
        int[] flowRates = new int[valvesOfInterestCount];
        for (int valveIndex1=0; valveIndex1<valvesOfInterestCount; valveIndex1++) {
            Valve valve1 = valvesOfInterest[valveIndex1];
            flowRates[valveIndex1] = valve1.getFlowRate();
            for (int valveIndex2=0; valveIndex2<valvesOfInterestCount; valveIndex2++) {
                Valve valve2 = valvesOfInterest[valveIndex2];
                distanceBetweenValves[valveIndex1][valveIndex2] = allValvesDistancesMap.get(valve1.getName()).get(valve2.getName());
            }
        }
        boolean considerStartingValve = startingValve.getFlowRate()>0;
        return new BasicPuzzleResults<>(
            solve(distanceBetweenValves,flowRates,0,considerStartingValve,30,1),
            solve(distanceBetweenValves,flowRates,0,considerStartingValve,26,2)
        );
    }

    private static Map<String, Valve> parseValves(char[] inputCharacters) {
        Map<String, Valve> valves = new HashMap<>();
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
        return valves;
    }

    private static Map<String, Map<String, Integer>> createDistancesMap(Map<String, Valve> valves) {
        Map<String,Map<String,Integer>> allDistances = new HashMap<>();
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
            allDistances.put(valveName,distancesForThisValve);
        }
        return allDistances;
    }

    private static int solve(int[][] distanceBetweenValves, int[] flowRates, int startIndex, boolean considerStartingValve, int availableTime, int actorCount) {
        Set<IntermediateState> knownStates = new HashSet<>();
        Deque<IntermediateState> pendingStates = new LinkedList<>();
        IntermediateState initialState = new IntermediateState(
            startIndex,
            0,
            0,
            0,
            0
        );
        Map<Integer,Integer> bestPressureRelievedPerOpenedValveBitsetMap = new HashMap<>();
        knownStates.add(initialState);
        pendingStates.add(initialState);
        IntermediateState oldState;
        int valveCount = flowRates.length;
        while ((oldState = pendingStates.pollFirst()) != null) {
            int currentValveIndex = oldState.getCurrentValveIndex();
            int openedValveBitset = oldState.getOpenedValveBitset();
            int timeElapsed = oldState.getTimeElapsed();
            int pressureReliefRate = oldState.getPressureReliefRate();
            int pressureRelieved = oldState.getPressureRelieved();
            bestPressureRelievedPerOpenedValveBitsetMap.merge(openedValveBitset,pressureRelieved+((availableTime-timeElapsed)*pressureReliefRate),Math::max);
            for (int targetValveIndex=0, toValveBit=1; targetValveIndex<valveCount; targetValveIndex++, toValveBit<<=1) {
                if (!(targetValveIndex == startIndex && considerStartingValve) && (openedValveBitset & toValveBit) == 0) {
                    int timeTakenForThisValve = distanceBetweenValves[currentValveIndex][targetValveIndex] + 1;
                    int newTimeElapsed = timeElapsed + timeTakenForThisValve;
                    if (newTimeElapsed <= availableTime) {
                        IntermediateState newState = new IntermediateState(
                            targetValveIndex,
                            openedValveBitset | toValveBit,
                            newTimeElapsed,
                            pressureReliefRate + flowRates[targetValveIndex],
                            pressureRelieved + (pressureReliefRate*timeTakenForThisValve)
                        );
                        if (!knownStates.contains(newState)) {
                            pendingStates.addLast(newState);
                        }
                    }
                }
            }
        }
        FinalState[] finalStates = bestPressureRelievedPerOpenedValveBitsetMap.entrySet().stream().map(entry -> new FinalState(entry.getKey(),entry.getValue())).sorted(Comparator.comparing(FinalState::getTotalPressureRelieved)).toArray(FinalState[]::new);
        return recurseActorAssignment(finalStates,finalStates.length, 0, actorCount,0,0,0);
    }

    private static int recurseActorAssignment(FinalState[] finalStates, int finalStateCount, int finalStateStartIndex, int actorCount, int actorIndex, int openedValveBitset, int pressureRelieved) {
        if (actorIndex == actorCount) {
            return pressureRelieved;
        } else {
            int nextActorIndex = actorIndex+1;
            int maximumPressureRelieved = 0;
            for (int finalStateIndex=finalStateStartIndex; finalStateIndex<finalStateCount; finalStateIndex++) {
                FinalState finalState = finalStates[finalStateIndex];
                int finalStateOpenedValveBitset = finalState.getOpenedValveBitset();
                if ((openedValveBitset & finalStateOpenedValveBitset) == 0) {
                    maximumPressureRelieved = Math.max(maximumPressureRelieved, recurseActorAssignment(finalStates,finalStateCount,finalStateIndex+1,actorCount,nextActorIndex,openedValveBitset|finalStateOpenedValveBitset,pressureRelieved+finalState.getTotalPressureRelieved()));
                }
            }
            return maximumPressureRelieved;
        }
    }
}
