package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day11 implements IPuzzle {
    private static final boolean DEBUG_SLINGING = false;
    private static final boolean DEBUG_ROUND = false;
    private static final boolean DEBUG_INSPECTION_COUNTS = false;

    private static long powExact(long a, long b) {
        double result = Math.pow(a, b);
        if (result > Long.MAX_VALUE || result < Long.MIN_VALUE) {
            throw new ArithmeticException("long overflow");
        }
        return (long)result;
    }

    private enum Operator {
        ADD      ('+',"increases by", true, Math::addExact),
        SUBTRACT ('-', "decreases by", false, Math::subtractExact),
        MULTIPLY ('*', "is multiplied by", true, Math::multiplyExact),
        DIVIDE   ('/', "is divided by", false, (a,b) -> a/b),
        POWER   ('^',"raised to the power of", false, (a,b) -> powExact(a,b));

        private final char characterRepresentiation;
        private final String actionText;
        private final boolean symmetric;
        private final LongBinaryOperator longBinaryOperator;

        private static final Map<Character, Operator> characterRepresentationToOperatorMap;
        static {
            characterRepresentationToOperatorMap = new HashMap<>();
            for (Operator operator : Operator.values()) {
                if (characterRepresentationToOperatorMap.put(operator.getCharacterRepresentation(), operator) != null) {
                    throw new IllegalStateException("Duplicate character representation");
                }
            }
        }

        public static Operator getOperatorFromCharacterRepresentation(char characterRepresentation) {
            Operator operator = Operator.characterRepresentationToOperatorMap.get(characterRepresentation);
            if (operator == null) {
                throw new IllegalStateException("No such operator");
            }
            return operator;
        }

        Operator(char characterRepresentiation, String actionText, boolean symmetric, LongBinaryOperator longBinaryOperator) {
            this.characterRepresentiation = characterRepresentiation;
            this.actionText = actionText;
            this.symmetric = symmetric;
            this.longBinaryOperator = longBinaryOperator;
        }

        public char getCharacterRepresentation() {
            return this.characterRepresentiation;
        }

        public String getActionText() {
            return this.actionText;
        }

        public boolean isSymmetric() {
            return this.symmetric;
        }

        public LongBinaryOperator getLongBinaryOperator() {
            return this.longBinaryOperator;
        }
    }

    private final class Operation {
        private final LongUnaryOperator getLongUnaryOperator;
        private final String description;

        private Operation(LongUnaryOperator getLongUnaryOperator, String description) {
            this.getLongUnaryOperator = getLongUnaryOperator;
            this.description = description;
        }

        public LongUnaryOperator getGetLongUnaryOperator() {
            return this.getLongUnaryOperator;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private static interface MonkeyReference {
        Monkey getReferencedMonkey();
    }

    private static final class Monkey implements MonkeyReference {
        private final int number;
        private final List<Long> initialItems;
        private final LinkedList<Long> items;
        private final Operation operation;
        private final long testDivisor;
        private final MonkeyReference testPassedMonkeyToHandToReference;
        private final MonkeyReference testFailedMonkeyToHandToReference;

        private int inspectionCount;

        public Monkey(int number, List<Long> items, Operation operation, long testDivisor, MonkeyReference testPassedMonkeyToHandToReference, MonkeyReference testFailedMonkeyToHandToReference) {
            this.number = number;
            this.initialItems = new ArrayList<>(items);
            this.items = new LinkedList<>();
            this.operation = operation;
            this.testDivisor = testDivisor;
            this.testPassedMonkeyToHandToReference = testPassedMonkeyToHandToReference;
            this.testFailedMonkeyToHandToReference = testFailedMonkeyToHandToReference;
            this.reset();
        }

        public int getNumber() {
            return this.number;
        }

        public List<Long> getItems() {
            return Collections.unmodifiableList(this.items);
        }

        public OptionalLong removeItemFromStart() {
            Long itemValue = this.items.pollFirst();
            if (itemValue == null) {
                return OptionalLong.empty();
            } else {
                return OptionalLong.of(itemValue);
            }
        }

        public void addItemToEnd(long item) {
            this.items.addLast(item);
        }

        public Operation getOperation() {
            return this.operation;
        }

        public long getTestDivisor() {
            return this.testDivisor;
        }

        public MonkeyReference getTestPassedMonkeyToHandToReference() {
            return this.testPassedMonkeyToHandToReference;
        }

        public MonkeyReference getTestFailedMonkeyToHandToReference() {
            return this.testFailedMonkeyToHandToReference;
        }

        public int getInspectionCount() {
            return this.inspectionCount;
        }

        public void incrementInspectionCount() {
            this.inspectionCount++;
        }

        public void reset() {
            this.items.clear();
            this.items.addAll(this.initialItems);
            this.inspectionCount = 0;
        }

        @Override
        public Monkey getReferencedMonkey() {
            return this;
        }
    }

    private static final class IndirectMonkeyReference implements MonkeyReference {
        private final int number;
        private Monkey monkey = null;

        public IndirectMonkeyReference(int number) {
            this.number = number;
        }

        public void realize(Map<Integer,Monkey> allMonkeys) {
            if (this.monkey != null) {
                throw new IllegalStateException("Indirect monkey already realized");
            }
            Monkey realizedMonkey = allMonkeys.get(this.number);
            if (realizedMonkey == null) {
                throw new IllegalStateException("No such monkey");
            }
            this.monkey = realizedMonkey;
        }

        @Override
        public Monkey getReferencedMonkey() {
            if (this.monkey == null) {
                throw new IllegalStateException("Indirect monkey not realizied");
            }
            return this.monkey;
        }
    }

    private static final Pattern INPUT_PATTERN = Pattern.compile("^Monkey (?<thisMonkeyNumber>[0-9]+) *: *\\r?\\n  Starting items *: *(?<startingItems>(?:[0-9]+)(?:(?:, (?:[0-9]+))*)) *\\r?\\n  Operation *: *new *= *(?<leftOperand>old|(?:[0-9]+)) *(?<operator>[" + Arrays.stream(Operator.values()).map(operator -> Pattern.quote(Character.toString(operator.getCharacterRepresentation()))).collect(Collectors.joining()) + "]) *(?<rightOperand>old|(?:[0-9]+)) *\\r?\\n  Test *: *divisible by (?<testDivisor>[0-9]+) *\\r?\\n    If (?<condition1>true|false) *: *throw to monkey (?!\\k<thisMonkeyNumber>)(?<otherMonkeyNumber1>[0-9]+) *\\r?\\n    If (?!\\k<condition1>)(?<condition2>true|false) *: *throw to monkey (?!\\k<thisMonkeyNumber>)(?<otherMonkeyNumber2>[0-9]+) *(?:\\r?\\n)*",Pattern.CASE_INSENSITIVE);

    private static void parseResultMonkeyReference(Map<Boolean,MonkeyReference> resultMonkeyReferenceMap, Map<Integer,Monkey> monkeyMap, Collection<IndirectMonkeyReference> indirectMonkeyReferences, Matcher matcher, int number) {
        final String condition = matcher.group("condition"+number);
        final int otherMonkeyNumber = Integer.parseInt(matcher.group("otherMonkeyNumber"+number));
        final Boolean booleanValue;
        switch (condition.toLowerCase(Locale.ENGLISH)) {
            case "true":
                booleanValue = Boolean.TRUE;
                break;
            case "false":
                booleanValue = Boolean.FALSE;
                break;
            default:
                throw new IllegalStateException("Could not parse condition.");
        }
        MonkeyReference monkeyReference = monkeyMap.get(otherMonkeyNumber);
        if (monkeyReference == null) {
            final IndirectMonkeyReference indirectMonkeyReference = new IndirectMonkeyReference(otherMonkeyNumber);
            indirectMonkeyReferences.add(indirectMonkeyReference);
            monkeyReference = indirectMonkeyReference;
        }
        if (resultMonkeyReferenceMap.put(booleanValue,monkeyReference) != null) {
            throw new IllegalStateException("Duplicate condition.");
        }
    }

    private Monkey[] parseInput(final char[] inputCharacters) {
        final Matcher matcher = INPUT_PATTERN.matcher(new String(inputCharacters));
        final SortedMap<Integer, Monkey> monkeyMap = new TreeMap<>();
        final Collection<IndirectMonkeyReference> indirectMonkeyReferences = new LinkedList<>();
        while (matcher.lookingAt()) {
            final int thisMonkeyNumber = Integer.parseInt(matcher.group("thisMonkeyNumber"));
            final Map<Boolean, MonkeyReference> resultMonkeyReferenceMap = new HashMap<>();
            parseResultMonkeyReference(resultMonkeyReferenceMap, monkeyMap, indirectMonkeyReferences, matcher, 1);
            parseResultMonkeyReference(resultMonkeyReferenceMap, monkeyMap, indirectMonkeyReferences, matcher, 2);
            Operator operator = Operator.getOperatorFromCharacterRepresentation(matcher.group("operator").charAt(0));
            String leftOperand = matcher.group("leftOperand");
            String rightOperand = matcher.group("rightOperand");
            boolean leftOperandIsOld = leftOperand.equalsIgnoreCase("old");
            boolean rightOperandIsOld = rightOperand.equalsIgnoreCase("old");
            if (!leftOperandIsOld) {
                if (!rightOperandIsOld) {
                    throw new IllegalStateException("Expected at least one operand to be old.");
                } else if (operator.isSymmetric()) {
                    String tempOperand = leftOperand;
                    leftOperand = rightOperand;
                    rightOperand = tempOperand;
                    leftOperandIsOld = true;
                    rightOperandIsOld = false;
                } else {
                    throw new IllegalStateException("old is the second operand but the operation is not symmetric.");
                }
            }
            final String operatorActionText = operator.getActionText();
            final LongBinaryOperator longBinaryOperator = operator.getLongBinaryOperator();
            final LongUnaryOperator longUnaryOperator;
            final String description;
            if (rightOperandIsOld) {
                longUnaryOperator = oldValue -> longBinaryOperator.applyAsLong(oldValue, oldValue);
                description = operatorActionText + " itself";
            } else {
                final long rightOperandValue = Long.parseLong(rightOperand);
                longUnaryOperator = oldValue -> longBinaryOperator.applyAsLong(oldValue, rightOperandValue);
                description = operatorActionText + " " + rightOperand;
            }
            final Monkey monkey = new Monkey(
                thisMonkeyNumber,
                Arrays.stream(matcher.group("startingItems").split(",")).map(String::trim).mapToLong(Long::parseLong).boxed().collect(Collectors.toList()),
                new Operation(longUnaryOperator, description),
                Long.parseLong(matcher.group("testDivisor")),
                resultMonkeyReferenceMap.get(Boolean.TRUE),
                resultMonkeyReferenceMap.get(Boolean.FALSE)
            );
            if (monkeyMap.put(thisMonkeyNumber, monkey) != null) {
                throw new IllegalStateException("Duplicate this monkey number.");
            }
            matcher.region(matcher.end(), matcher.regionEnd());
        }
        if (!matcher.hitEnd()) {
            throw new IllegalStateException("Could not parse full input.");
        }
        indirectMonkeyReferences.forEach(indirectMonkeyReference -> indirectMonkeyReference.realize(monkeyMap));
        return monkeyMap.values().toArray(new Monkey[0]);
    }

    private static long solve(final PrintWriter printWriter, final Monkey[] monkeys, final int rounds, final Long boredomDivisor, final boolean useModuloArithmatic) {
        for (Monkey monkey : monkeys) {
            monkey.reset();
        }
        if (DEBUG_SLINGING || DEBUG_ROUND || DEBUG_INSPECTION_COUNTS) {
            printWriter.format(
                "Solving for %d rounds with %d monkeys with boredomDivisor=%d%n%n",
                rounds,
                monkeys.length,
                boredomDivisor
            );
        }
        final boolean useBoredomDivisor;
        final long boredomDivisorUnboxed;
        if (boredomDivisor == null) {
            useBoredomDivisor = false;
            boredomDivisorUnboxed = 0;
        } else {
            useBoredomDivisor = true;
            boredomDivisorUnboxed = boredomDivisor;
        }
        final long moduloArithmaticModulo;
        if (useModuloArithmatic) {
            long gcd = 1;
            long product = 1;
            for (Monkey monkey : monkeys) {
                long testDivisor = monkey.getTestDivisor();
                product = Math.multiplyExact(product,testDivisor);
                gcd = ExtendedEuclideanAlgorithm.solve(gcd, testDivisor).getGcd();
            }
            final long lcm = product / gcd;
            if (lcm > Integer.MAX_VALUE) {
                // old * old in danger of overflowing
                throw new IllegalStateException("Modulo arithmatic too large - danger of overflow.");
            }
            moduloArithmaticModulo = lcm;
            if (DEBUG_ROUND || DEBUG_SLINGING) {
                printWriter.format(
                    "All worry values are modulo %d%n%n",
                    moduloArithmaticModulo
                );
            }
        } else {
            moduloArithmaticModulo = 1;
        }
        for (int round=1; round<=rounds; round++) {
            if (DEBUG_SLINGING) {
                printWriter.format(
                    "Round %d:%n",
                    round
                );
            }
            for (Monkey monkey : monkeys) {
                if (DEBUG_SLINGING) {
                    printWriter.format(
                        "  Monkey %d:%n",
                        monkey.getNumber()
                    );
                }
                final Operation operation = monkey.getOperation();
                final LongUnaryOperator longUnaryOperator = operation.getGetLongUnaryOperator();
                final long testDivisor = monkey.getTestDivisor();
                final MonkeyReference testPassedMonkeyToHandToReference = monkey.getTestPassedMonkeyToHandToReference();
                final MonkeyReference testFailedMonkeyToHandToReference = monkey.getTestFailedMonkeyToHandToReference();
                while (true) {
                    final OptionalLong optionalOldItemValue = monkey.removeItemFromStart();
                    if (!optionalOldItemValue.isPresent()) {
                        break;
                    }
                    final long oldItemValue = optionalOldItemValue.getAsLong();
                    final long newItemValuePreBoredom = longUnaryOperator.applyAsLong(oldItemValue);
                    final long newItemValuePostBoredom;
                    if (useBoredomDivisor) {
                        newItemValuePostBoredom = newItemValuePreBoredom / boredomDivisorUnboxed;
                    } else {
                        newItemValuePostBoredom = newItemValuePreBoredom;
                    }
                    final boolean divisorTestPasses = newItemValuePostBoredom % testDivisor == 0;
                    monkey.incrementInspectionCount();
                    final Monkey monkeyToPassTo = (divisorTestPasses?testPassedMonkeyToHandToReference:testFailedMonkeyToHandToReference).getReferencedMonkey();
                    final long newItemValue;
                    if (useModuloArithmatic) {
                        newItemValue = newItemValuePostBoredom % moduloArithmaticModulo;
                    } else {
                        newItemValue = newItemValuePostBoredom;
                    }
                    monkeyToPassTo.addItemToEnd(newItemValue);
                    if (DEBUG_SLINGING) {
                        printWriter.format(
                            "" +
                                "    Monkey inspects an item with a worry level of %d.%n" +
                                "      Worry level %s to %d.%n",
                            oldItemValue,
                            operation.getDescription(),
                            newItemValuePreBoredom
                        );
                        if (useBoredomDivisor) {
                            printWriter.format(
                                "" +
                                    "      Monkey gets bored with item. Worry level is divided by %d to %d.%n",
                                boredomDivisorUnboxed,
                                newItemValuePostBoredom
                            );
                        }
                        printWriter.format(
                            "" +
                                "      Current worry level %s divisible by %d.%n" +
                                "      Item with worry level %d is thrown to monkey %d.%n",
                            divisorTestPasses?"is":"is not",
                            testDivisor,
                            newItemValuePostBoredom,
                            monkeyToPassTo.getNumber()
                        );
                    }
                }
            }
            if (DEBUG_SLINGING) {
                printWriter.println();
            }
            if (DEBUG_ROUND) {
                printWriter.format(
                    "  After round %d, the monkeys are holding items with these worry levels:%n",
                    round
                );
                for (Monkey monkey : monkeys) {
                    printWriter.format(
                        "  Monkey %d: %s%n",
                        monkey.getNumber(),
                        monkey.getItems().stream().map(item -> Long.toString(item)).collect(Collectors.joining(", "))
                    );
                }
                printWriter.println();
            }
        }
        if (DEBUG_INSPECTION_COUNTS) {
            for (Monkey monkey : monkeys) {
                printWriter.format(
                    "Monkey %d inspected items %d times.%n",
                    monkey.getNumber(),
                    monkey.getInspectionCount()
                );
            }
            printWriter.println();
        }
        return Arrays.stream(monkeys).mapToInt(Monkey::getInspectionCount).map(x -> ~x).sorted().mapToLong(x -> ~x).limit(2).reduce(Math::multiplyExact).getAsLong();
    }

    @Override
    public IPuzzleResults runPuzzle(final char[] inputCharacters, final IPuzzleConfigProvider configProvider, final boolean partBPotentiallyUnsolvable, final PrintWriter printWriter) {
        final Monkey[] monkeys = parseInput(inputCharacters);
        return new BasicPuzzleResults<>(
            solve(printWriter, monkeys, 20, 3L, false),
            solve(printWriter, monkeys, 10000, null, true)
        );
    }
}
