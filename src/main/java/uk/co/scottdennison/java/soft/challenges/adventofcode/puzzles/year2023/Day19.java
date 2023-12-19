package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day19 implements IPuzzle {
    private static class Range {
        private final int minimumInclusive;
        private final int maximumInclusive;

        public Range(int minimumInclusive, int maximumInclusive) {
            if (minimumInclusive > maximumInclusive) {
                throw new IllegalStateException("Invalid range");
            }
            this.minimumInclusive = minimumInclusive;
            this.maximumInclusive = maximumInclusive;
        }

        public int getSize() {
            return maximumInclusive-minimumInclusive+1;
        }

        public Range createNewWithPotentialNewMinimum(int newMinimumInclusive) {
            return new Range(Math.max(minimumInclusive,newMinimumInclusive),maximumInclusive);
        }

        public Range createNewWithPotentialNewMaximum(int newMaximumInclusive) {
            return new Range(minimumInclusive,Math.min(maximumInclusive,newMaximumInclusive));
        }

        public Range createNewCombinedWith(Range otherRange) {
            return new Range(Math.max(minimumInclusive,otherRange.minimumInclusive),Math.min(maximumInclusive,otherRange.maximumInclusive));
        }

        @Override
        public String toString() {
            return minimumInclusive + "-" + maximumInclusive;
        }
    }

    private static class RangeGroup {
        private final Map<String,Range> rangePerAttributeMap;

        private RangeGroup(Map<String, Range> rangePerAttributeMap) {
            this.rangePerAttributeMap = rangePerAttributeMap;
        }

        public long getCombinations() {
            long combinations = 1;
            for (Range range : rangePerAttributeMap.values()) {
                combinations *= range.getSize();
            }
            return combinations;
        }

        private RangeGroup createWithModifiedRange(String attributeName, UnaryOperator<Range> rangeOperator) {
            Map<String,Range> newRangePerAttributeMap = new LinkedHashMap<>(rangePerAttributeMap);
            newRangePerAttributeMap.compute(
                attributeName,
                (__, range) -> {
                    if (range == null) {
                        throw new IllegalStateException("New attribute not allowed");
                    }
                    return rangeOperator.apply(range);
                }
            );
            return new RangeGroup(newRangePerAttributeMap);
        }

        public RangeGroup createNewRangeGroupWithSuccessAppliedForAttribute(String attributeName, Condition condition) {
            return createWithModifiedRange(attributeName, condition::createNewRangeWithSuccessApplied);
        }

        public RangeGroup createNewRangeGroupWithFailureAppliedForAttribute(String attributeName, Condition condition) {
            return createWithModifiedRange(attributeName, condition::createNewRangeWithFailureApplied);
        }

        public static RangeGroup createNewFromMap(Map<String, Range> rangePerAttribute) {
            return new RangeGroup(new LinkedHashMap<>(rangePerAttribute));
        }

        public RangeGroup createNewCombinedWith(RangeGroup otherRangeGroup) {
            if (!rangePerAttributeMap.keySet().equals(otherRangeGroup.rangePerAttributeMap.keySet())) {
                throw new IllegalStateException("Mismatching attributes");
            }
            Map<String,Range> newRangePerAttributeMap = new LinkedHashMap<>();
            for (Map.Entry<String,Range> thisRangePerAttributeMapEntry : rangePerAttributeMap.entrySet()) {
                String attributeName = thisRangePerAttributeMapEntry.getKey();
                newRangePerAttributeMap.put(attributeName,thisRangePerAttributeMapEntry.getValue().createNewCombinedWith(otherRangeGroup.rangePerAttributeMap.get(attributeName)));
            }
            return new RangeGroup(newRangePerAttributeMap);
        }

        @Override
        public String toString() {
            return rangePerAttributeMap.toString();
        }
    }

    private static class DependentRangeGroup {
        private final String dependentGroupName;
        private final RangeGroup rangeGroup;

        public DependentRangeGroup(String dependentGroupName, RangeGroup rangeGroup) {
            this.dependentGroupName = dependentGroupName;
            this.rangeGroup = rangeGroup;
        }

        public String getDependentGroupName() {
            return this.dependentGroupName;
        }

        public RangeGroup getRangeGroup() {
            return this.rangeGroup;
        }

        @Override
        public String toString() {
            return "DependentRangeGroup{" +
                "dependentGroupName=\"" + this.dependentGroupName + '"' + ", " +
                "rangeGroup=" + this.rangeGroup + "}";
        }
    }

    private static enum ConditionType {
        LESS_THAN {
            @Override
            boolean test(int value1, int value2) {
                return value1 < value2;
            }

            Range createNewRangeWithSuccessApplied(Range range, int value) {
                return range.createNewWithPotentialNewMaximum(value-1);
            }

            Range createNewRangeWithFailureApplied(Range range, int value) {
                return range.createNewWithPotentialNewMinimum(value);
            }
        },
        GREATER_THAN {
            @Override
            boolean test(int value1, int value2) {
                return value1 > value2;
            }

            Range createNewRangeWithSuccessApplied(Range range, int value) {
                return range.createNewWithPotentialNewMinimum(value+1);
            }

            Range createNewRangeWithFailureApplied(Range range, int value) {
                return range.createNewWithPotentialNewMaximum(value);
            }
        };

        abstract boolean test(int value1, int value2);
        abstract Range createNewRangeWithSuccessApplied(Range range, int value);
        abstract Range createNewRangeWithFailureApplied(Range range, int value);

        public static ConditionType getConditionTypeForCharacter(char character) {
            switch (character) {
                case '<':
                    return LESS_THAN;
                case '>':
                    return GREATER_THAN;
                default:
                    throw new IllegalArgumentException("Unexpected condition type character");
            }
        }
    }

    private static class Condition {
        private final String attributeName;
        private final ConditionType conditionType;
        private final int value;
        private final String successRuleName;

        public Condition(String attributeName, ConditionType conditionType, int value, String successRuleName) {
            this.attributeName = attributeName;
            this.conditionType = conditionType;
            this.value = value;
            this.successRuleName = successRuleName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public ConditionType getConditionType() {
            return conditionType;
        }

        public int getValue() {
            return value;
        }

        public String getSuccessRuleName() {
            return successRuleName;
        }

        public boolean test(int attributeValue) {
            return conditionType.test(attributeValue, value);
        }

        Range createNewRangeWithSuccessApplied(Range range) {
            return conditionType.createNewRangeWithSuccessApplied(range, value);
        }

        Range createNewRangeWithFailureApplied(Range range) {
            return conditionType.createNewRangeWithFailureApplied(range, value);
        }
    }

    private static class Rule {
        private final String ruleName;
        private final List<Condition> conditions;
        private final String fallbackRuleName;

        public Rule(String ruleName, List<Condition> conditions, String fallbackRuleName) {
            this.ruleName = ruleName;
            this.conditions = new ArrayList<>(conditions);
            this.fallbackRuleName = fallbackRuleName;
        }

        public String getRuleName() {
            return ruleName;
        }

        public List<Condition> getConditions() {
            return Collections.unmodifiableList(conditions);
        }

        public String getFallbackRuleName() {
            return fallbackRuleName;
        }
    }

    private static class Part {
        private final Map<String,Integer> attributes;

        public Part(Map<String, Integer> attributes) {
            this.attributes = new LinkedHashMap<>(attributes);
        }

        public int getValueForAttribute(String attributeName) {
            Integer value = attributes.get(attributeName);
            if (value == null) {
                throw new IllegalStateException("No such attribute");
            }
            return value;
        }

        public int getAttributeSum() {
            return attributes.values().stream().mapToInt(i -> i).sum();
        }
    }

    private static final Pattern PATTERN_RULE_LINE = Pattern.compile("^(?<ruleName>[a-zA-Z]+)\\{(?:(?<conditions>.+),)?(?<fallbackRuleName>[a-zA-Z]+)\\}$");
    private static final Pattern PATTERN_RULE_CONDITION_SPLIT = Pattern.compile(",");
    private static final Pattern PATTERN_RULE_CONDITION = Pattern.compile("^(?<attributeName>[a-zA-Z]+)(?<conditionType>[<>])(?<value>[0-9]+):(?<successRuleName>[a-zA-Z]+)$");
    private static final Pattern PATTERN_PART_LINE = Pattern.compile("^\\{(?<attributes>.+)\\}$");
    private static final Pattern PATTERN_PART_ATTRIBUTE_SPLIT = Pattern.compile(",");
    private static final Pattern PATTERN_PART_ATTRIBUTE = Pattern.compile("^(?<attributeName>[a-zA-Z]+)=(?<value>[0-9]+)");

    private static <InnerContainerType> void parseLine(String inputLine, Pattern lineRule, String innerGroupName, Pattern innerSplitRule, Pattern innerRule, Supplier<InnerContainerType> containerCreator, BiConsumer<Matcher, InnerContainerType> innerAdder, BiConsumer<Matcher, InnerContainerType> outerAdder) {
        Matcher lineMatcher = lineRule.matcher(inputLine);
        if (!lineMatcher.matches()) {
            throw new IllegalStateException("Could not parse line");
        }
        InnerContainerType container = containerCreator.get();
        innerSplitRule.splitAsStream(lineMatcher.group(innerGroupName)).forEachOrdered(
            innerPart -> {
                Matcher innerMatcher = innerRule.matcher(innerPart);
                if (!innerMatcher.matches()) {
                    throw new IllegalStateException("Could not parse inner part");
                }
                innerAdder.accept(innerMatcher, container);
            }
        );
        outerAdder.accept(
            lineMatcher,
            container
        );
    }

    private static void applyRangeGroup(Map<String,DependentRangeGroup> unresolvedRangeGroup, List<DependentRangeGroup> acceptedRangeGroups, String sourceRuleName, String destinationRuleName, RangeGroup rangeGroup) {
        if (!destinationRuleName.equals("R")) {
            DependentRangeGroup dependentRangeGroup = new DependentRangeGroup(sourceRuleName, rangeGroup);
            if (destinationRuleName.equals("A")) {
                acceptedRangeGroups.add(dependentRangeGroup);
            }
            else {
                if (unresolvedRangeGroup.put(destinationRuleName,dependentRangeGroup) != null) {
                    throw new IllegalStateException("Rule graph is not a tree.");
                }
            }
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Iterator<String> inputLinesIterator = LineReader.stringsIterator(inputCharacters);
        List<Part> parts = new ArrayList<>();
        Map<String,Rule> rules = new HashMap<>();
        while (true) {
            String inputLine = inputLinesIterator.next();
            if (inputLine.isEmpty()) {
                break;
            }
            parseLine(
                inputLine,
                PATTERN_RULE_LINE,
                "conditions",
                PATTERN_RULE_CONDITION_SPLIT,
                PATTERN_RULE_CONDITION,
                ArrayList::new,
                (Matcher innerMatcher, List<Condition> conditions) -> {
                    conditions.add(
                        new Condition(
                            innerMatcher.group("attributeName"),
                            ConditionType.getConditionTypeForCharacter(innerMatcher.group("conditionType").charAt(0)),
                            Integer.parseInt(innerMatcher.group("value")),
                            innerMatcher.group("successRuleName")
                        )
                    );
                },
                (Matcher lineMatcher, List<Condition> conditions) -> {
                    String ruleName = lineMatcher.group("ruleName");
                    if (
                        rules.put(
                            ruleName,
                            new Rule(
                                ruleName,
                                conditions,
                                lineMatcher.group("fallbackRuleName")
                            )
                        ) != null
                    ) {
                        throw new IllegalStateException("Multiple rules share the name: " + ruleName);
                    }
                }
            );
        }
        while (inputLinesIterator.hasNext()) {
            String inputLine = inputLinesIterator.next();
            parseLine(
                inputLine,
                PATTERN_PART_LINE,
                "attributes",
                PATTERN_PART_ATTRIBUTE_SPLIT,
                PATTERN_PART_ATTRIBUTE,
                LinkedHashMap::new,
                (Matcher innerMatcher, Map<String, Integer> attributes) -> {
                    String attributeName = innerMatcher.group("attributeName");
                    if (
                        attributes.put(
                            attributeName,
                            Integer.parseInt(innerMatcher.group("value"))
                        ) != null
                    ) {
                        throw new IllegalStateException("Multiple attributes share the name: " + attributeName);
                    }
                },
                (Matcher outerMatcher, Map<String, Integer> attributes) -> {
                    parts.add(
                        new Part(
                            attributes
                        )
                    );
                }
            );
        }

        int attributeSumSum = 0;
        for (Part part : parts) {
            String ruleName = "in";
            boolean accepted;
            outerLoop: while (true) {
                switch (ruleName) {
                    case "A":
                        accepted = true;
                        break outerLoop;
                    case "R":
                        accepted = false;
                        break outerLoop;
                    default:
                        Rule rule = rules.get(ruleName);
                        if (rule == null) {
                            throw new IllegalStateException("No rule with rule name: " + ruleName);
                        }
                        for (Condition condition : rule.getConditions()) {
                            String attributeName = condition.getAttributeName();
                            if (condition.test(part.getValueForAttribute(attributeName))) {
                                ruleName = condition.getSuccessRuleName();
                                continue outerLoop;
                            }
                        }
                        ruleName = rule.getFallbackRuleName();
                }
            }
            if (accepted) {
                attributeSumSum += part.getAttributeSum();
            }
        }

        Range baseRange = new Range(1,4000);
        Map<String,Range> baseRangeGroupMap = new LinkedHashMap<>();
        baseRangeGroupMap.put("x",baseRange);
        baseRangeGroupMap.put("m",baseRange);
        baseRangeGroupMap.put("a",baseRange);
        baseRangeGroupMap.put("s",baseRange);
        RangeGroup baseRangeGroup = RangeGroup.createNewFromMap(baseRangeGroupMap);

        Map<String,RangeGroup> resolvedRangeGroups = new HashMap<>();
        resolvedRangeGroups.put("in", baseRangeGroup);

        Map<String,DependentRangeGroup> unresolvedRangeGroup = new HashMap<>();

        List<DependentRangeGroup> acceptedRangeGroups = new ArrayList<>();

        for (Rule rule : rules.values()) {
            String ruleName = rule.getRuleName();
            RangeGroup rangeGroup = baseRangeGroup;
            for (Condition condition : rule.getConditions()) {
                String attributeName = condition.getAttributeName();
                String successRuleName = condition.getSuccessRuleName();
                applyRangeGroup(unresolvedRangeGroup,acceptedRangeGroups,ruleName,successRuleName,rangeGroup.createNewRangeGroupWithSuccessAppliedForAttribute(attributeName, condition));
                rangeGroup = rangeGroup.createNewRangeGroupWithFailureAppliedForAttribute(attributeName, condition);
            }
            applyRangeGroup(unresolvedRangeGroup,acceptedRangeGroups,ruleName,rule.getFallbackRuleName(),rangeGroup);
        }

        boolean changeMade;
        do {
            changeMade = false;
            Iterator<Map.Entry<String,DependentRangeGroup>> unresolvedRangeGroupEntryIterator = unresolvedRangeGroup.entrySet().iterator();
            while (unresolvedRangeGroupEntryIterator.hasNext()) {
                Map.Entry<String,DependentRangeGroup> unresolvedRangeGroupEntry = unresolvedRangeGroupEntryIterator.next();
                DependentRangeGroup dependentRangeGroup = unresolvedRangeGroupEntry.getValue();
                RangeGroup dependentResolvedRangeGroup = resolvedRangeGroups.get(dependentRangeGroup.getDependentGroupName());
                if (dependentResolvedRangeGroup != null) {
                    if (
                        resolvedRangeGroups.put(
                            unresolvedRangeGroupEntry.getKey(),
                            dependentRangeGroup.getRangeGroup().createNewCombinedWith(dependentResolvedRangeGroup)
                        ) != null
                    ) {
                        throw new IllegalStateException("Something went wrong.");
                    }
                    unresolvedRangeGroupEntryIterator.remove();
                    changeMade = true;
                }
            }
        } while (changeMade);

        if (!unresolvedRangeGroup.isEmpty()) {
            throw new IllegalStateException("Could not resolve range groups");
        }

        // This does NOT account for overlap
        long totalCombinations = 0;
        for (DependentRangeGroup acceptedDependentRangeGroup : acceptedRangeGroups) {
            RangeGroup dependentResolvedRangeGroup = resolvedRangeGroups.get(acceptedDependentRangeGroup.getDependentGroupName());
            if (dependentResolvedRangeGroup == null) {
                throw new IllegalStateException("Accepted range group depends on unknown range group");
            }
            totalCombinations += acceptedDependentRangeGroup.getRangeGroup().createNewCombinedWith(dependentResolvedRangeGroup).getCombinations();
        }

        return new BasicPuzzleResults<>(
            attributeSumSum,
            totalCombinations
        );
    }
}
