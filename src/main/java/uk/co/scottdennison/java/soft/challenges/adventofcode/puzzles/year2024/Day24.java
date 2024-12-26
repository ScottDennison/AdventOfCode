package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.datastructure.Pair;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day24 implements IPuzzle {
    private enum Operation {
        AND {
            @Override
            public boolean execute(boolean input1, boolean input2) {
                return input1 && input2;
            }
        },
        OR {
            @Override
            public boolean execute(boolean input1, boolean input2) {
                return input1 || input2;
            }
        },
        XOR {
            @Override
            public boolean execute(boolean input1, boolean input2) {
                return input1 ^ input2;
            }
        };

        public abstract boolean execute(boolean input1, boolean input2);
    }

    private static final class DeviceSpec {
        private final String inputName1;
        private final String inputName2;
        private final Operation operation;
        private final String outputName;

        public DeviceSpec(String inputName1, String inputName2, Operation operation, String outputName) {
            this.inputName1 = inputName1;
            this.inputName2 = inputName2;
            this.operation = operation;
            this.outputName = outputName;
        }

        public String getInputName1() {
            return inputName1;
        }

        public String getInputName2() {
            return inputName2;
        }

        public Operation getOperation() {
            return operation;
        }

        public String getOutputName() {
            return outputName;
        }
    }

    private static enum CorrectSystemType {
        NOT_APPLICABLE {
            @Override
            public Set<String> calculateIncorrectOutputNames(Collection<DeviceSpec> devices) {
                return Collections.emptySet();
            }
        },
        AND {
            @Override
            Set<String> calculateIncorrectOutputNames(Collection<DeviceSpec> devices) {
                Set<String> incorrectOutputNames = new HashSet<>();
                for (DeviceSpec device : devices) {
                    if (device.getOperation() != Operation.AND) {
                        throw new IllegalStateException("Expected all devices to be of type AND");
                    }
                    int inputsNumber = CorrectSystemType.getXYInputsCommonNumber(device).orElseThrow(() -> new IllegalStateException("Unexpected inputs of AND gate"));
                    String outputName = device.getOutputName();
                    if (!outputName.equals(String.format("z%02d",inputsNumber))) {
                        incorrectOutputNames.add(outputName);
                    }
                }
                return incorrectOutputNames;
            }
        },
        FULL_ADDER {
            @Override
            Set<String> calculateIncorrectOutputNames(Collection<DeviceSpec> devices) {
                Set<String> incorrectOutputNames = new HashSet<>();
                Map<Operation,Set<DeviceSpec>> devicesByOperation = devices.stream().collect(Collectors.groupingBy(DeviceSpec::getOperation,Collectors.toSet()));
                Map<String,DeviceSpec> devicesByOutputName = devices.stream().collect(Collectors.toMap(DeviceSpec::getOutputName, Function.identity()));
                Map<String,Map<Operation,Set<DeviceSpec>>> devicesByInputNameByType = devices
                    .stream()
                    .flatMap(
                        device ->
                            Stream
                                .of(device.getInputName1(), device.getInputName2())
                                .map(inputName -> new Pair<>(inputName, device))
                    )
                    .collect(
                        Collectors.groupingBy(
                            Pair::getLeft,
                            Collectors.mapping(
                                Pair::getRight,
                                Collectors.groupingBy(
                                    DeviceSpec::getOperation,
                                    Collectors.toSet()
                                )
                            )
                        )
                    );

                // Some of these checks may overlap, but better to be safe than sorry.

                for (DeviceSpec device : devicesByOperation.get(Operation.XOR)) {
                    OptionalInt xyCommonNumber = getXYInputsCommonNumber(device);
                    String outputName = device.getOutputName();
                    boolean deviceValid;
                    if (xyCommonNumber.isPresent()) {
                        if (xyCommonNumber.getAsInt() == 0) {
                            deviceValid = outputName.equals("z00");
                        }
                        else {
                            deviceValid = hasDeviceCounts(devicesByInputNameByType.get(outputName), 1, 0, 1);
                        }
                    }
                    else {
                        deviceValid = outputName.startsWith("z");
                    }
                    if (!deviceValid) {
                        incorrectOutputNames.add(outputName);
                    }
                }

                for (DeviceSpec device : devicesByOperation.get(Operation.AND)) {
                    OptionalInt xyCommonNumber = getXYInputsCommonNumber(device);
                    if (xyCommonNumber.isPresent()) {
                        boolean deviceValid;
                        String outputName = device.getOutputName();
                        if (xyCommonNumber.getAsInt() == 0) {
                            deviceValid = hasDeviceCounts(devicesByInputNameByType.get(outputName), 1, 0, 1);
                        }
                        else {
                            deviceValid = hasDeviceCounts(devicesByInputNameByType.get(outputName), 0, 1, 0);
                        }
                        if (!deviceValid) {
                            incorrectOutputNames.add(outputName);
                        }
                    }
                }

                int highestZ = Integer.MIN_VALUE;
                for (DeviceSpec device : devices) {
                    String outputName = device.getOutputName();
                    if (outputName.startsWith("z")) {
                        int number = Integer.parseInt(outputName.substring(1));
                        if (number > highestZ) {
                            highestZ = number;
                        }
                    }
                }
                for (Map.Entry<String,DeviceSpec> devicesByOutputNameEntry : devicesByOutputName.entrySet()) {
                    String outputName = devicesByOutputNameEntry.getKey();
                    if (outputName.startsWith("z")) {
                        int number = Integer.parseInt(outputName.substring(1));
                        if (devicesByOutputNameEntry.getValue().getOperation() != (number == highestZ ? Operation.OR : Operation.XOR)) {
                            incorrectOutputNames.add(outputName);
                        }
                    }
                }

                for (DeviceSpec device : devicesByOperation.get(Operation.OR)) {
                    DeviceSpec supplyingDevice1 = devicesByOutputName.get(device.getInputName1());
                    if (supplyingDevice1.getOperation() != Operation.AND) {
                        incorrectOutputNames.add(supplyingDevice1.getOutputName());
                    }
                    DeviceSpec supplyingDevice2 = devicesByOutputName.get(device.getInputName2());
                    if (supplyingDevice2.getOperation() != Operation.AND) {
                        incorrectOutputNames.add(supplyingDevice2.getOutputName());
                    }
                }

                return incorrectOutputNames;
            }
        };

        private static int getDeviceCount(Map<Operation,Set<DeviceSpec>> devicesByOperation, Operation operation) {
            Set<DeviceSpec> devices = devicesByOperation.get(operation);
            if (devices == null) {
                return 0;
            }
            return devices.size();
        }

        private static boolean hasDeviceCounts(Map<Operation,Set<DeviceSpec>> devicesByOperation, int expectedANDCount, int expectedORCount, int expectedXORCount) {
            if (devicesByOperation == null) {
                return false;
            }
            return
                getDeviceCount(devicesByOperation, Operation.AND) == expectedANDCount
                &&
                getDeviceCount(devicesByOperation, Operation.OR) == expectedORCount
                &&
                getDeviceCount(devicesByOperation, Operation.XOR) == expectedXORCount;
        }

        private static OptionalInt getXYInputsCommonNumber(DeviceSpec device) {
            String inputName1 = device.getInputName1();
            String inputName2 = device.getInputName2();
            if ((inputName1.startsWith("x") && inputName2.startsWith("y")) || (inputName1.startsWith("y") && inputName2.startsWith("x"))) {
                int input1 = Integer.parseInt(inputName1.substring(1));
                int input2 = Integer.parseInt(inputName2.substring(1));
                if (input1 == input2) {
                    return OptionalInt.of(input1);
                }
                throw new IllegalStateException("Inputs started with x and y, but numbers were not equal");
            }
            return OptionalInt.empty();
        }

        abstract Set<String> calculateIncorrectOutputNames(Collection<DeviceSpec> completeDevices);
    }

    private static final Pattern PATTERN_INPUT_LINE_WIRE = Pattern.compile("^(?<wireName>[xy][0-9]{2}): (?<state>[01])$");
    private static final Pattern PATTERN_INPUT_LINE_DEVICE = Pattern.compile("^(?<inputName1>[a-z0-9]{3}) (?<operation>[A-Z]+) (?<inputName2>[a-z0-9]{3}) -> (?<outputName>[a-z0-9]{3})$");

    private static void parseInput(Map<String, Boolean> wireStates, List<DeviceSpec> devices, char[] inputCharacters) {
        boolean processingWires = true;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher;
            if (processingWires) {
                if (inputLine.isEmpty()) {
                    processingWires = false;
                }
                else {
                    matcher = PATTERN_INPUT_LINE_WIRE.matcher(inputLine);
                    if (!matcher.matches()) {
                        throw new IllegalStateException("Could not parse input line.");
                    }
                    String wireName = matcher.group("wireName");
                    boolean state;
                    switch (matcher.group("state")) {
                        case "0":
                            state = false;
                            break;
                        case "1":
                            state = true;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected state.");
                    }
                    if (wireStates.put(wireName, state) != null) {
                        throw new IllegalStateException("Duplicate wire.");
                    }
                }
            }
            else {
                matcher = PATTERN_INPUT_LINE_DEVICE.matcher(inputLine);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Could not parse input line.");
                }
                String inputName1 = matcher.group("inputName1");
                String inputName2 = matcher.group("inputName2");
                devices.add(
                    new DeviceSpec(
                        inputName1,
                        inputName2,
                        Operation.valueOf(matcher.group("operation")),
                        matcher.group("outputName")
                    )
                );
            }
        }
    }

    private static BigInteger runPartA(Map<String,Boolean> initalWireStates, List<DeviceSpec> devices) {
        Map<String,Boolean> wireStates = new HashMap<>(initalWireStates);
        Deque<DeviceSpec> incompleteDevices = new LinkedList<>(devices);
        while (true) {
            boolean deviceCompleted = false;
            Iterator<DeviceSpec> deviceIterator = incompleteDevices.iterator();
            while (deviceIterator.hasNext()) {
                DeviceSpec device = deviceIterator.next();
                Boolean input1 = wireStates.get(device.getInputName1());
                Boolean input2 = wireStates.get(device.getInputName2());
                if (input1 != null && input2 != null) {
                    deviceIterator.remove();
                    if (wireStates.put(device.getOutputName(), device.getOperation().execute(input1, input2)) != null) {
                        throw new IllegalStateException("Output wire already has a state.");
                    }
                    deviceCompleted = true;
                }
            }
            if (!deviceCompleted) {
                break;
            }
        }
        BigInteger output = BigInteger.ZERO;
        for (Map.Entry<String,Boolean> wireStateEntry : wireStates.entrySet()) {
            if (wireStateEntry.getValue()) {
                String wireName = wireStateEntry.getKey();
                if (wireName.startsWith("z")) {
                    output = output.setBit(Integer.parseInt(wireName.substring(1)));
                }
            }
        }
        return output;
    }

    private static String runPartB(IPuzzleConfigProvider configProvider, Collection<DeviceSpec> devices) {
        Set<String> incorrectOutputNames = CorrectSystemType.valueOf(new String(configProvider.getPuzzleConfigChars("correct_system_type"))).calculateIncorrectOutputNames(devices);
        int actualIncorrectOutputNameCount = incorrectOutputNames.size();
        int expectedIncorrectOutputNameCount = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("incorrect_gate_count")).trim());
        if (actualIncorrectOutputNameCount != expectedIncorrectOutputNameCount) {
            throw new IllegalStateException("Expected " + expectedIncorrectOutputNameCount + " incorrect gates but got " + actualIncorrectOutputNameCount);
        }
        return incorrectOutputNames.stream().sorted().collect(Collectors.joining(","));
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Boolean> wireStates = new HashMap<>();
        List<DeviceSpec> devices = new ArrayList<>();
        parseInput(wireStates, devices, inputCharacters);
        return new BasicPuzzleResults<>(
            runPartA(wireStates, devices),
            runPartB(configProvider, devices)
        );
    }
}
