package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day21 implements IPuzzle {
    private static enum NumericalKeypadButton {
        NUMBER_0,
        NUMBER_1,
        NUMBER_2,
        NUMBER_3,
        NUMBER_4,
        NUMBER_5,
        NUMBER_6,
        NUMBER_7,
        NUMBER_8,
        NUMBER_9,
        A;

        private static final NumericalKeypadButton[] BUTTONS_INDEXED_BY_NUMERICAL_VALUE = new NumericalKeypadButton[]{
            NUMBER_0,
            NUMBER_1,
            NUMBER_2,
            NUMBER_3,
            NUMBER_4,
            NUMBER_5,
            NUMBER_6,
            NUMBER_7,
            NUMBER_8,
            NUMBER_9
        };

        private static Optional<NumericalKeypadButton> getButtonForCharacter(char character) {
            if (character == 'A' || character == 'a') {
                return Optional.of(A);
            }
            else if (character >= '0' || character <= '9') {
                return Optional.of(BUTTONS_INDEXED_BY_NUMERICAL_VALUE[character - '0']);
            }
            else {
                return Optional.empty();
            }
        }
    }

    private static enum DirectionalKeypadButton {
        DIRECTION_UP,
        DIRECTION_DOWN,
        DIRECTION_LEFT,
        DIRECTIOR_RIGHT,
        A;
    }

    private static class PossibleRoute<KeypadButtonType> implements Iterable<KeypadButtonType> {
        private final List<KeypadButtonType> keypadButtons;
        private transient int hashCode;
        private transient int changeCount;

        public PossibleRoute(List<KeypadButtonType> keypadButtons) {
            if (keypadButtons == null || keypadButtons.isEmpty()) {
                throw new IllegalStateException("There must be at least one button");
            }
            this.keypadButtons = keypadButtons;
            this.hashCode = 0;
            this.changeCount = -1;
        }

        @Override
        public Iterator<KeypadButtonType> iterator() {
            return keypadButtons.iterator();
        }

        public int getLength() {
            return keypadButtons.size();
        }

        public int getChangeCount() {
            if (changeCount == -1) {
                int newChangeCount = -1;
                KeypadButtonType lastKeypadButton = null;
                for (KeypadButtonType keypadButton : keypadButtons) {
                    if (!Objects.equals(lastKeypadButton, keypadButton)) {
                        newChangeCount++;
                    }
                    lastKeypadButton = keypadButton;
                }
                changeCount = newChangeCount;
            }
            return changeCount;
        }

        @Override
        public final boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof PossibleRoute)) return false;

            PossibleRoute<?> otherPossibleRoute = (PossibleRoute<?>) otherObject;
            return Objects.equals(keypadButtons, otherPossibleRoute.keypadButtons);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = keypadButtons.hashCode();
            }
            return hashCode;
        }
    }

    private static interface KeypadLevelSolver<KeypadButtonType> {
        long calculateShortestPossibleRouteLength(PossibleRoute<KeypadButtonType> possibleRoute);
    }

    private static class FinalKeypadLevelSolver<KeypadButtonType> implements KeypadLevelSolver<KeypadButtonType> {
        @Override
        public long calculateShortestPossibleRouteLength(PossibleRoute<KeypadButtonType> possibleRoute) {
            return possibleRoute.getLength();
        }
    }

    private static class ChainedKeypadLevelSolver<ThisLevelKeypadButtonType, NextLevelKeypadButtonType> implements KeypadLevelSolver<ThisLevelKeypadButtonType> {
        private final Map<ThisLevelKeypadButtonType, Map<ThisLevelKeypadButtonType, List<PossibleRoute<NextLevelKeypadButtonType>>>> routeMap;
        private final Map<PossibleRoute<ThisLevelKeypadButtonType>, Long> cache;
        private final KeypadLevelSolver<NextLevelKeypadButtonType> nextLevelKeypadLevelSolver;
        private final ThisLevelKeypadButtonType initialButton;

        public ChainedKeypadLevelSolver(Map<ThisLevelKeypadButtonType, Map<ThisLevelKeypadButtonType, List<PossibleRoute<NextLevelKeypadButtonType>>>> routeMap, KeypadLevelSolver<NextLevelKeypadButtonType> nextLevelKeypadLevelSolver, ThisLevelKeypadButtonType initialButton) {
            this.routeMap = routeMap;
            this.cache = new HashMap<>();
            this.nextLevelKeypadLevelSolver = nextLevelKeypadLevelSolver;
            this.initialButton = initialButton;
        }

        @Override
        public long calculateShortestPossibleRouteLength(PossibleRoute<ThisLevelKeypadButtonType> desiredRoute) {
            Long shortestPossibleRouteLengthFromCache = cache.get(desiredRoute);
            if (shortestPossibleRouteLengthFromCache != null) {
                return shortestPossibleRouteLengthFromCache;
            }
            ThisLevelKeypadButtonType lastButton = initialButton;
            long shortestPossibleRouteLength = 0;
            for (ThisLevelKeypadButtonType nextButton : desiredRoute) {
                long shortestPossibleNextLevelRouteLength = Long.MAX_VALUE;
                for (PossibleRoute<NextLevelKeypadButtonType> possibleNextLevelRoute : routeMap.get(lastButton).get(nextButton)) {
                    shortestPossibleNextLevelRouteLength = Math.min(shortestPossibleNextLevelRouteLength, nextLevelKeypadLevelSolver.calculateShortestPossibleRouteLength(possibleNextLevelRoute));
                }
                shortestPossibleRouteLength = Math.addExact(shortestPossibleRouteLength, shortestPossibleNextLevelRouteLength);
                lastButton = nextButton;
            }
            cache.put(desiredRoute, shortestPossibleRouteLength);
            return shortestPossibleRouteLength;
        }
    }

    private static <ThisLevelKeypadButtonType> void findDirectionalKeypadAllPossibleRoutesCheckPotentialRoute(Map<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>> allPossibleRoutesForStartButton, ThisLevelKeypadButtonType[][] padGrid, int padHeight, int padWidth, Set<ThisLevelKeypadButtonType> pathVisitedButtons, DirectionalKeypadButton[] pathDirectionalButtons, int recursionLevel, int y, int x, DirectionalKeypadButton newDirectionalButton) {
        if (y < 0 || y >= padHeight || x < 0 || x >= padWidth) {
            return;
        }
        ThisLevelKeypadButtonType newPathButton = padGrid[y][x];
        if (newPathButton == null) {
            return;
        }
        if (!pathVisitedButtons.add(newPathButton)) {
            return;
        }
        int newRecursionLevel = recursionLevel + 1;
        pathDirectionalButtons[recursionLevel] = newDirectionalButton;
        pathDirectionalButtons[newRecursionLevel] = DirectionalKeypadButton.A;
        allPossibleRoutesForStartButton.computeIfAbsent(newPathButton, __ -> new ArrayList<>()).add(new PossibleRoute<>(Arrays.asList(Arrays.copyOf(pathDirectionalButtons, newRecursionLevel + 1))));
        findDirectionalKeypadAllPossibleRoutes(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, newRecursionLevel, y, x);
        pathVisitedButtons.remove(newPathButton);
    }

    private static <ThisLevelKeypadButtonType> void findDirectionalKeypadAllPossibleRoutes(Map<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>> allPossibleRoutesForStartButton, ThisLevelKeypadButtonType[][] padGrid, int padHeight, int padWidth, Set<ThisLevelKeypadButtonType> pathVisitedButtons, DirectionalKeypadButton[] pathDirectionalButtons, int recursionLevel, int y, int x) {
        findDirectionalKeypadAllPossibleRoutesCheckPotentialRoute(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, recursionLevel, y - 1, x, DirectionalKeypadButton.DIRECTION_UP);
        findDirectionalKeypadAllPossibleRoutesCheckPotentialRoute(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, recursionLevel, y + 1, x, DirectionalKeypadButton.DIRECTION_DOWN);
        findDirectionalKeypadAllPossibleRoutesCheckPotentialRoute(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, recursionLevel, y, x - 1, DirectionalKeypadButton.DIRECTION_LEFT);
        findDirectionalKeypadAllPossibleRoutesCheckPotentialRoute(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, recursionLevel, y, x + 1, DirectionalKeypadButton.DIRECTIOR_RIGHT);
    }

    private static <ThisLevelKeypadButtonType> Map<ThisLevelKeypadButtonType, Map<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>>> findDirectionalKeypadSmallestPossibleRoutes(ThisLevelKeypadButtonType[][] padGrid) {
        int padHeight = padGrid.length;
        int padWidth = padGrid[0].length;
        int maxSize = padHeight * padWidth;
        Set<ThisLevelKeypadButtonType> pathVisitedButtons = new HashSet<>(maxSize);
        DirectionalKeypadButton[] pathDirectionalButtons = new DirectionalKeypadButton[maxSize];
        Map<ThisLevelKeypadButtonType, Map<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>>> allPossibleRoutes = new HashMap<>();
        for (int startY=0; startY<padHeight; startY++) {
            for (int startX=0; startX<padWidth; startX++) {
                ThisLevelKeypadButtonType padStartButton = padGrid[startY][startX];
                if (padStartButton != null) {
                    pathVisitedButtons.add(padStartButton);
                    Map<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>> allPossibleRoutesForStartButton = new HashMap<>();
                    findDirectionalKeypadAllPossibleRoutes(allPossibleRoutesForStartButton, padGrid, padHeight, padWidth, pathVisitedButtons, pathDirectionalButtons, 0, startY, startX);
                    allPossibleRoutesForStartButton.put(padStartButton, Collections.singletonList(new PossibleRoute<>(Collections.singletonList(DirectionalKeypadButton.A))));
                    Map<Character, Character[][]> routesForStartCharacter = new HashMap<>();
                    for (Map.Entry<ThisLevelKeypadButtonType, List<PossibleRoute<DirectionalKeypadButton>>> allPossibleRoutesForStartButtonEntry : allPossibleRoutesForStartButton.entrySet()) {
                        List<PossibleRoute<DirectionalKeypadButton>> allPossibleRoutesForStartButtonList = allPossibleRoutesForStartButtonEntry.getValue();
                        int minLength = allPossibleRoutesForStartButtonList.stream().mapToInt(route -> route.getLength()).min().getAsInt();
                        allPossibleRoutesForStartButtonList = allPossibleRoutesForStartButtonList.stream().filter(route -> route.getLength() == minLength).collect(Collectors.toList());
                        int minChangeCount = allPossibleRoutesForStartButtonList.stream().mapToInt(route -> route.getChangeCount()).min().getAsInt();
                        allPossibleRoutesForStartButtonList = allPossibleRoutesForStartButtonList.stream().filter(route -> route.getChangeCount() == minChangeCount).collect(Collectors.toList());
                        allPossibleRoutesForStartButtonEntry.setValue(allPossibleRoutesForStartButtonList);
                    }
                    allPossibleRoutes.put(padStartButton, allPossibleRoutesForStartButton);
                    pathVisitedButtons.remove(padStartButton);
                }
            }
        }
        return allPossibleRoutes;
    }

    private static KeypadLevelSolver<NumericalKeypadButton> createSolverChain(Map<NumericalKeypadButton, Map<NumericalKeypadButton, List<PossibleRoute<DirectionalKeypadButton>>>> numericalKeypadRoutes, Map<DirectionalKeypadButton, Map<DirectionalKeypadButton, List<PossibleRoute<DirectionalKeypadButton>>>> directionalKeypadRoutes, int levels) {
        KeypadLevelSolver<DirectionalKeypadButton> lastKeypadLevelSolver = new FinalKeypadLevelSolver<>();
        for (int level=0; level<levels; level++) {
            lastKeypadLevelSolver = new ChainedKeypadLevelSolver<>(
                directionalKeypadRoutes,
                lastKeypadLevelSolver,
                DirectionalKeypadButton.A
            );
        }
        return new ChainedKeypadLevelSolver<>(
            numericalKeypadRoutes,
            lastKeypadLevelSolver,
            NumericalKeypadButton.A
        );
    }

    private static final Pattern NON_INT_CHARACTERS = Pattern.compile("[^0-9]+");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<NumericalKeypadButton, Map<NumericalKeypadButton, List<PossibleRoute<DirectionalKeypadButton>>>> numericalKeypadRoutes
            = findDirectionalKeypadSmallestPossibleRoutes(
            new NumericalKeypadButton[][]{
                {
                    NumericalKeypadButton.NUMBER_7,
                    NumericalKeypadButton.NUMBER_8,
                    NumericalKeypadButton.NUMBER_9
                },
                {
                    NumericalKeypadButton.NUMBER_4,
                    NumericalKeypadButton.NUMBER_5,
                    NumericalKeypadButton.NUMBER_6
                },
                {
                    NumericalKeypadButton.NUMBER_1,
                    NumericalKeypadButton.NUMBER_2,
                    NumericalKeypadButton.NUMBER_3
                },
                {
                    null,
                    NumericalKeypadButton.NUMBER_0,
                    NumericalKeypadButton.A
                }
            }
        );
        Map<DirectionalKeypadButton, Map<DirectionalKeypadButton, List<PossibleRoute<DirectionalKeypadButton>>>> directionalKeypadRoutes = findDirectionalKeypadSmallestPossibleRoutes(
            new DirectionalKeypadButton[][]{
                {
                    null,
                    DirectionalKeypadButton.DIRECTION_UP,
                    DirectionalKeypadButton.A
                },
                {
                    DirectionalKeypadButton.DIRECTION_LEFT,
                    DirectionalKeypadButton.DIRECTION_DOWN,
                    DirectionalKeypadButton.DIRECTIOR_RIGHT
                }
            }
        );

        KeypadLevelSolver<NumericalKeypadButton> partASolver = createSolverChain(numericalKeypadRoutes, directionalKeypadRoutes, 2);
        KeypadLevelSolver<NumericalKeypadButton> partBSolver = createSolverChain(numericalKeypadRoutes, directionalKeypadRoutes, 25);

        long partATotal = 0;
        long partBTotal = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            inputLine = inputLine.trim();
            char[] inputLineCharacters = inputLine.toCharArray();
            int inputLineCharacterCount = inputLineCharacters.length;
            NumericalKeypadButton[] numericalKeypadButtons = new NumericalKeypadButton[inputLineCharacterCount];
            for (int inputLineCharacterIndex=0; inputLineCharacterIndex<inputLineCharacterCount; inputLineCharacterIndex++) {
                Optional<NumericalKeypadButton> numericalKeypadButton = NumericalKeypadButton.getButtonForCharacter(inputLineCharacters[inputLineCharacterIndex]);
                if (!numericalKeypadButton.isPresent()) {
                    throw new IllegalStateException("Could not convert character to button");
                }
                numericalKeypadButtons[inputLineCharacterIndex] = numericalKeypadButton.get();
            }
            PossibleRoute<NumericalKeypadButton> topLevelRoute = new PossibleRoute<>(Arrays.asList(numericalKeypadButtons));
            long numericPartOfCode = Long.parseLong(NON_INT_CHARACTERS.matcher(inputLine).replaceAll(""));
            partATotal = Math.addExact(partATotal, Math.multiplyExact(partASolver.calculateShortestPossibleRouteLength(topLevelRoute), numericPartOfCode));
            partBTotal = Math.addExact(partBTotal, Math.multiplyExact(partBSolver.calculateShortestPossibleRouteLength(topLevelRoute), numericPartOfCode));
        }

        return new BasicPuzzleResults<>(
            partATotal,
            partBTotal
        );
    }
}
