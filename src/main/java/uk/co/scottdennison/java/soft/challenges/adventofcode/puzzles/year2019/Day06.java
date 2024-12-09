package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Day06 implements IPuzzle {
    private static class Planet {
        private final String name;
        private Planet parent = null;
        private final Set<Planet> children = new HashSet<>();

        public Planet(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Planet getParent() {
            return this.parent;
        }

        public void setParent(Planet parent) {
            if (this.parent != null) {
                throw new IllegalStateException("This planet already has a parent.");
            }
            this.parent = parent;
        }

        public void addChild(Planet child) {
            this.children.add(child);
        }

        public Set<Planet> getChildren() {
            return Collections.unmodifiableSet(this.children);
        }

        public int countChildren() {
            return this.children.size();
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String, Planet> planetMap = new HashMap<>();
        Set<Planet> possibleTopLevelParentPlanets = new HashSet<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            int separatorIndex = inputLine.indexOf(')');
            if (separatorIndex == -1) {
                throw new IllegalStateException("Missing separator");
            }
            Planet centerOfOrbitPlanet = planetMap.computeIfAbsent(inputLine.substring(0, separatorIndex), Planet::new);
            Planet satellitePlanet = planetMap.computeIfAbsent(inputLine.substring(separatorIndex+ 1), Planet::new);
            satellitePlanet.setParent(centerOfOrbitPlanet);
            centerOfOrbitPlanet.addChild(satellitePlanet);
            if (centerOfOrbitPlanet.getParent() == null) {
                possibleTopLevelParentPlanets.add(centerOfOrbitPlanet);
            }
            else {
                possibleTopLevelParentPlanets.remove(centerOfOrbitPlanet);
            }
            possibleTopLevelParentPlanets.remove(satellitePlanet);
        };
        if (possibleTopLevelParentPlanets.size() != 1) {
            throw new IllegalStateException("Could not work out universal center of mass planet");
        }
        Planet universalCenterOfMassPlanet = possibleTopLevelParentPlanets.iterator().next();
        Map<Planet,Integer> planetOrbitCounts = new HashMap<>();
        planetOrbitCounts.put(universalCenterOfMassPlanet, 0);
        Deque<Planet> pendingParentPlanets = new LinkedList<>();
        pendingParentPlanets.addFirst(universalCenterOfMassPlanet);
        Planet parentPlanet;
        int totalOrbits = 0;
        while ((parentPlanet = pendingParentPlanets.pollFirst()) != null) {
            int orbitCountForChildPlanets = planetOrbitCounts.get(parentPlanet)+1;
            for (Planet childPlanet : parentPlanet.getChildren()) {
                planetOrbitCounts.put(childPlanet, orbitCountForChildPlanets);
                totalOrbits += orbitCountForChildPlanets;
                pendingParentPlanets.addLast(childPlanet);
            }
        }
        Planet sanPlanet = planetMap.get("SAN");
        Planet youPlanet = planetMap.get("YOU");
        Integer partBAnswer;
        if (sanPlanet == null || youPlanet == null) {
            if (partBPotentiallyUnsolvable) {
                partBAnswer = null;
            } else {
                throw new IllegalStateException("Could not find needed planets");
            }
        }
        else {
            Map<Planet,Integer> distancesFromYou = new HashMap<>();
            distancesFromYou.put(youPlanet, 0);
            Deque<Planet> planetsToCheck = new LinkedList<>();
            planetsToCheck.addFirst(youPlanet);
            Planet sourcePlanet;
            while ((sourcePlanet = planetsToCheck.pollFirst()) != null) {
                Set<Planet> linkedPlanets = new HashSet<>();
                linkedPlanets.add(sourcePlanet.getParent());
                linkedPlanets.addAll(sourcePlanet.getChildren());
                int linkedDistance = distancesFromYou.get(sourcePlanet) + 1;
                for (Planet linkedPlanet : linkedPlanets) {
                    if (linkedPlanet != null && !distancesFromYou.containsKey(linkedPlanet)) {
                        distancesFromYou.put(linkedPlanet, linkedDistance);
                        planetsToCheck.addLast(linkedPlanet);
                    }
                }
            }
            partBAnswer = distancesFromYou.get(sanPlanet)-2;
        }
        return new BasicPuzzleResults<>(
            totalOrbits,
            partBAnswer
        );
    }
}
