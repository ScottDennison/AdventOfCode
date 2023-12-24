package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day20 implements IPuzzle {
    private static final int PART_A_BUTTON_PRESS_COUNT = 1000;
    private static final String BUTTON_MODULE_NAME = "button";
    private static final String BROADCAST_MODULE_NAME = "broadcaster";
    private static final String[] RECIEVER_MODULE_NAMES = {"rx","output"};

    private static class Pulse {
        private final Module sourceModule;
        private final Module targetModule;
        private final boolean high;

        public Pulse(Module sourceModule, Module targetModule, boolean high)
        {
            this.sourceModule = sourceModule;
            this.targetModule = targetModule;
            this.high = high;
        }

        public Module getSourceModule()
        {
            return sourceModule;
        }

        public Module getTargetModule()
        {
            return targetModule;
        }

        public boolean isHigh()
        {
            return high;
        }
    }

    private static enum ModuleType {
        FLIP_FLOP,
        CONJUNCTION,
        BROADCAST,
        RECIEVER,
        BUTTON
    }

    private static interface Module {
        ModuleType getModuleType();
        String getName();
        List<Pulse> handlePulse(Pulse pulse);
        List<Module> getConnectedInputModules();
        List<Module> getConnectedOutputModules();
        void registerConnectedInputModule(Module module);
        void registerConnectedOutputModule(Module module);
        void finalizeModuleConnections();
    }

    private static abstract class AbstractModule implements Module {
        private final String name;
        private final List<Module> connectedInputModulesList;
        private final List<Module> connectedOutputModulesList;
        private final Set<Module> connectedInputModulesSet;
        private final Set<Module> connectedOutputModulesSet;
        private boolean finalized;
        private List<Pulse> readOnlyOutputHighPulses;
        private List<Pulse> readOnlyOutputLowPulses;

        public AbstractModule(String name) {
            this.name = name;
            this.connectedInputModulesList = new ArrayList<>();
            this.connectedOutputModulesList = new ArrayList<>();
            this.connectedInputModulesSet = new HashSet<>();
            this.connectedOutputModulesSet = new HashSet<>();
            this.finalized = false;
            this.readOnlyOutputHighPulses = null;
            this.readOnlyOutputLowPulses = null;
        }

        @Override
        public final String getName() {
            return this.name;
        }

        @Override
        public List<Pulse> handlePulse(Pulse pulse) {
            // By default, return an empty list of output pusles
            return Collections.emptyList();
        }

        @Override
        public List<Module> getConnectedInputModules() {
            return Collections.unmodifiableList(connectedInputModulesList);
        }

        @Override
        public List<Module> getConnectedOutputModules() {
            return Collections.unmodifiableList(connectedOutputModulesList);
        }

        @Override
        public void registerConnectedInputModule(Module module) {
            this.registerConnectedModule(this.connectedInputModulesList,this.connectedInputModulesSet,module);
        }

        @Override
        public void registerConnectedOutputModule(Module module) {
            this.registerConnectedModule(this.connectedOutputModulesList,this.connectedOutputModulesSet,module);
        }

        private void registerConnectedModule(List<Module> connectedModulesList, Set<Module> connectedModulesSet, Module module) {
            if (finalized) {
                throw new IllegalStateException("Cannot connect a module to a finalized module.");
            }
            if (!connectedModulesSet.add(module)) {
                throw new IllegalArgumentException("Duplicate module");
            }
            connectedModulesList.add(module);
        }

        @Override
        public void finalizeModuleConnections() {
            if (finalized) {
                throw new IllegalStateException("Already finalized.");
            }
            this.readOnlyOutputHighPulses = createReadOnlyPulsesToConnectedModules(true);
            this.readOnlyOutputLowPulses = createReadOnlyPulsesToConnectedModules(false);
            finalized = true;
        }

        protected boolean isFinalized() {
            return finalized;
        }

        private List<Pulse> createReadOnlyPulsesToConnectedModules(boolean high) {
            List<Pulse> pulses = new ArrayList<>(connectedOutputModulesList.size());
            for (Module connectedOutputModule : connectedOutputModulesList) {
                pulses.add(new Pulse(this,connectedOutputModule,high));
            }
            return Collections.unmodifiableList(pulses);
        }

        protected List<Pulse> getPulsesToOutputConnectedModules(boolean high) {
            if (!finalized) {
                throw new IllegalStateException("Connected modules not finalized");
            }
            if (high) {
                return readOnlyOutputHighPulses;
            }
            else {
                return readOnlyOutputLowPulses;
            }
        }
    }

    private static final class FlipFlopModule extends AbstractModule {
        private boolean on;

        public FlipFlopModule(String name) {
            super(name);
            this.on = false;
        }

        @Override
        public ModuleType getModuleType() {
            return ModuleType.FLIP_FLOP;
        }

        public List<Pulse> handlePulse(Pulse pulse) {
            if (pulse.isHigh()) {
                return Collections.emptyList();
            } else {
                on = !on;
                return getPulsesToOutputConnectedModules(on);
            }
        }
    }

    private static final class ConjunctionModule extends AbstractModule {
        private Map<Module,Integer> inputModuleNameToMemoryIndexMap = null;
        private int onMemoryCount = 0;
        private int totalMemoryCount = 0;
        private boolean[] memory = null;

        public ConjunctionModule(String name) {
            super(name);
        }

        @Override
        public ModuleType getModuleType() {
            return ModuleType.CONJUNCTION;
        }

        public List<Pulse> handlePulse(Pulse pulse) {
            if (!isFinalized()) {
                throw new IllegalStateException("Cannot handle pulse as module connections are not finalized");
            }
            Integer memoryIndexBoxed = inputModuleNameToMemoryIndexMap.get(pulse.getSourceModule());
            if (memoryIndexBoxed == null) {
                throw new IllegalArgumentException("Unknown input module name");
            }
            int memoryIndex = memoryIndexBoxed;
            boolean pulseHigh = pulse.isHigh();
            if (memory[memoryIndex] != pulseHigh) {
                memory[memoryIndex] = pulseHigh;
                if (pulseHigh) {
                    onMemoryCount++;
                }
                else {
                    onMemoryCount--;
                }
            }
            return getPulsesToOutputConnectedModules(onMemoryCount != totalMemoryCount);
        }

        @Override
        public void finalizeModuleConnections() {
            super.finalizeModuleConnections();
            List<Module> connectedInputModules = getConnectedInputModules();
            inputModuleNameToMemoryIndexMap = new HashMap<>(connectedInputModules.size());
            for (Module connectedInputModule : connectedInputModules) {
                inputModuleNameToMemoryIndexMap.put(connectedInputModule,totalMemoryCount++);
            }
            memory = new boolean[totalMemoryCount];
        }
    }

    private static final class BroadcastModule extends AbstractModule {
        public BroadcastModule(String name) {
            super(name);
        }

        @Override
        public ModuleType getModuleType() {
            return ModuleType.BROADCAST;
        }

        public List<Pulse> handlePulse(Pulse pulse) {
            return getPulsesToOutputConnectedModules(pulse.isHigh());
        }
    }

    private static final class RecieverModule extends AbstractModule {
        public RecieverModule(String name) {
            super(name);
        }

        @Override
        public ModuleType getModuleType() {
            return ModuleType.RECIEVER;
        }
    }

    private static final class ButtonModule extends AbstractModule {
        public ButtonModule(String name) {
            super(name);
        }

        @Override
        public ModuleType getModuleType() {
            return ModuleType.RECIEVER;
        }

        @Override
        public List<Pulse> handlePulse(Pulse pulse) {
            throw new IllegalStateException("The button module cannot handle pulses.");
        }

        @Override
        public void registerConnectedInputModule(Module module) {
            throw new IllegalStateException("The button module cannot have an input module.");
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<symbol>[%&])?(?<name>[a-zA-Z0-9]+) -> (?<outputModules>(?:[a-zA-Z0-9]+)(?:, [a-zA-Z0-9]+)*)$");
    private static final Pattern PATTERN_OUTPUT_MODULE_SPLIT = Pattern.compile(", ");

    private static void registerConnectedModules(Map<String,Module> modules, Map<String,List<String>> pendingModuleRegistrations, BiConsumer<Module, Module> moduleConnectionRegistrationMethod) {
        for (Map.Entry<String, List<String>> pendingModuleRegistrationsEntry : pendingModuleRegistrations.entrySet()) {
            String leftModuleName = pendingModuleRegistrationsEntry.getKey();
            Module leftModule = modules.get(leftModuleName);
            if (leftModule == null) {
                throw new IllegalStateException("No such module: " + leftModuleName);
            }
            for (String rightModuleName : pendingModuleRegistrationsEntry.getValue()) {
                Module rightModule = modules.get(rightModuleName);
                if (rightModule == null) {
                    throw new IllegalStateException("No such module: " + rightModuleName);
                }
                moduleConnectionRegistrationMethod.accept(leftModule, rightModule);
            }
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Set<String> recieverModuleNamesSet = new HashSet<>(Arrays.asList(RECIEVER_MODULE_NAMES));
        Map<String,List<String>> pendingInputModuleRegistrations = new HashMap<>();
        Map<String,List<String>> pendingOutputModuleRegistrations = new HashMap<>();
        Map<String,Module> modules = new HashMap<>();
        BroadcastModule broadcastModule = null;
        String recieverModuleName = null;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_LINE.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            String symbolString = matcher.group("symbol");
            String name = matcher.group("name");
            Module module;
            if (symbolString == null)  {
                if (name.equals(BROADCAST_MODULE_NAME)) {
                    broadcastModule = new BroadcastModule(name);
                    module = broadcastModule;
                }
                else if (recieverModuleNamesSet.contains(name)) {
                    throw new IllegalStateException("A module with the name " + name + " should only be an output and not an input.");
                }
                else {
                    throw new IllegalStateException("Unsupported module type");
                }
            }
            else {
                if (name.equals(BROADCAST_MODULE_NAME) || recieverModuleNamesSet.contains(name)) {
                    throw new IllegalStateException("A module with the name " + name + " cannot have a symbol.");
                }
                switch (symbolString.charAt(0)) {
                    case '%':
                        module = new FlipFlopModule(name);
                        break;
                    case '&':
                        module = new ConjunctionModule(name);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected symbol");
                }
            }
            List<String> pendingOutputModuleRegistrationsForModule = new ArrayList<>();
            pendingOutputModuleRegistrations.put(name, pendingOutputModuleRegistrationsForModule);
            for (String connectedOutputModuleName : PATTERN_OUTPUT_MODULE_SPLIT.split(matcher.group("outputModules"))) {
                pendingOutputModuleRegistrationsForModule.add(connectedOutputModuleName);
                pendingInputModuleRegistrations.computeIfAbsent(connectedOutputModuleName,__ -> new ArrayList<>()).add(name);
                if (recieverModuleNamesSet.contains(connectedOutputModuleName)) {
                    if (recieverModuleName != null) {
                        throw new IllegalStateException("Multiple reciever modules");
                    }
                    recieverModuleName = connectedOutputModuleName;
                }
            }
            if (modules.put(name,module) != null) {
                throw new IllegalStateException("Duplicate module name: " + name);
            }
        }
        RecieverModule recieverModule;
        boolean partBUnsolvable = false;
        if (recieverModuleName == null) {
            if (partBPotentiallyUnsolvable) {
                partBUnsolvable = true;
                recieverModule = null;
            }
            else{
                throw new IllegalStateException("No reciever module.");
            }
        }
        else {
            recieverModule = new RecieverModule(recieverModuleName);
            modules.put(recieverModuleName, recieverModule);
        }
        if (broadcastModule == null) {
            throw new IllegalStateException("No broadcast module");
        }
        ButtonModule buttonModule = new ButtonModule(BUTTON_MODULE_NAME);
        modules.put(BUTTON_MODULE_NAME,buttonModule);
        pendingInputModuleRegistrations.computeIfAbsent(BROADCAST_MODULE_NAME,__ -> new ArrayList<>()).add(BUTTON_MODULE_NAME);
        pendingOutputModuleRegistrations.put(BUTTON_MODULE_NAME, Arrays.asList(BROADCAST_MODULE_NAME));
        registerConnectedModules(modules,pendingInputModuleRegistrations,Module::registerConnectedInputModule);
        registerConnectedModules(modules,pendingOutputModuleRegistrations,Module::registerConnectedOutputModule);
        for (Module module : modules.values()) {
            module.finalizeModuleConnections();
        }

        return new BasicPuzzleResults<>(
            solvePartA(buttonModule,broadcastModule),
            partBUnsolvable?null:solvePartB(broadcastModule,recieverModule,partBPotentiallyUnsolvable)
        );
    }

    private static int solvePartA(ButtonModule buttonModule, BroadcastModule broadcastModule) {
        Deque<Pulse> pendingPulses = new LinkedList<>();
        int lowPulseCount = 0;
        int highPulseCount = 0;
        for (int timesButtonPressed=1; timesButtonPressed<=PART_A_BUTTON_PRESS_COUNT; timesButtonPressed++) {
            pendingPulses.add(new Pulse(buttonModule,broadcastModule,false));
            Pulse pulse;
            while ((pulse = pendingPulses.pollFirst()) != null) {
                if (pulse.isHigh()) {
                    highPulseCount++;
                }
                else {
                    lowPulseCount++;
                }
                pendingPulses.addAll(pulse.getTargetModule().handlePulse(pulse));
            }
        }
        return lowPulseCount*highPulseCount;
    }

    private static Long solvePartB(BroadcastModule broadcastModule, RecieverModule recieverModule, boolean partBPotentiallyUnsolvable) {
        Long result = attemptSolvePartB(broadcastModule, recieverModule);
        if (result == null && !partBPotentiallyUnsolvable) {
            throw new IllegalStateException("Unable to solve part B, but puzzle is not marked as potentially unsolvable.");
        }
        return result;
    }

    private static Long attemptSolvePartB(BroadcastModule broadcastModuleDepth0, RecieverModule recieverModuleDepth0) {
        List<Module> recieverModuleDepth0InputModules = recieverModuleDepth0.getConnectedInputModules();
        if (recieverModuleDepth0InputModules.size() != 1) {
            return null;
        }
        Module recieverModuleDepth1 = recieverModuleDepth0InputModules.get(0);
        if (recieverModuleDepth1.getModuleType() != ModuleType.CONJUNCTION) {
            return null;
        }

        Set<Module> remainingCentralConjunctionModules = new HashSet<>();

        for (Module recieverModuleDepth2 : recieverModuleDepth1.getConnectedInputModules()) {
            if (recieverModuleDepth2.getModuleType() != ModuleType.CONJUNCTION) {
                return null;
            }
            List<Module> recieverModuleDepth2InputModules = recieverModuleDepth2.getConnectedInputModules();
            if (recieverModuleDepth2InputModules.size() != 1) {
                return null;
            }
            Module recieverModuleDepth3 = recieverModuleDepth2InputModules.get(0);
            if (recieverModuleDepth3.getModuleType() != ModuleType.CONJUNCTION) {
                return null;
            }
            remainingCentralConjunctionModules.add(recieverModuleDepth3);
        }

        long result = 1;
        for (Module broadcastModuleDepth1 : broadcastModuleDepth0.getConnectedOutputModules()) {
            if (broadcastModuleDepth1.getModuleType() != ModuleType.FLIP_FLOP) {
                return null;
            }
            Set<Module> broadcastModuleDepth1InputModules = new HashSet<>(broadcastModuleDepth1.getConnectedInputModules());
            Set<Module> broadcastModuleDepth1OutputModules = new HashSet<>(broadcastModuleDepth1.getConnectedOutputModules());
            if (broadcastModuleDepth1InputModules.size() != 2 || broadcastModuleDepth1OutputModules.size() != 2) {
                return null;
            }
            if (!broadcastModuleDepth1InputModules.remove(broadcastModuleDepth0)) {
                return null;
            }
            Module centralConjunctionModule = broadcastModuleDepth1InputModules.iterator().next();
            if (!remainingCentralConjunctionModules.remove(centralConjunctionModule)) {
                return null;
            }
            if (!broadcastModuleDepth1OutputModules.remove(centralConjunctionModule)) {
                return null;
            }
            Module pendingModuleParent = broadcastModuleDepth1;
            Module pendingModule = broadcastModuleDepth1OutputModules.iterator().next();
            long counterTargetValue = 1;
            long nextCounterBit = counterTargetValue << 1;
            while (pendingModule != null) {
                if (pendingModule.getModuleType() != ModuleType.FLIP_FLOP) {
                    return null;
                }
                Set<Module> pendingModuleInputModules = new HashSet<>(pendingModule.getConnectedInputModules());
                Set<Module> pendingModuleOutputModules = new HashSet<>(pendingModule.getConnectedOutputModules());
                if (!pendingModuleInputModules.remove(pendingModuleParent)) {
                    return null;
                }
                Module newPendingModule;
                boolean removedCentralConjunctionModuleFromPendingModuleInputModules = pendingModuleInputModules.remove(centralConjunctionModule);
                boolean removedCentralConjunctionModuleFromPendingModuleOutputModules = pendingModuleOutputModules.remove(centralConjunctionModule);
                if (removedCentralConjunctionModuleFromPendingModuleInputModules) {
                    if (removedCentralConjunctionModuleFromPendingModuleOutputModules) {
                        return null;
                    }
                    else {
                        if (pendingModuleInputModules.size() != 0 && pendingModuleOutputModules.size() != 1) {
                            return null;
                        }
                        newPendingModule = pendingModuleOutputModules.iterator().next();
                    }
                }
                else {
                    if (removedCentralConjunctionModuleFromPendingModuleOutputModules) {
                        counterTargetValue |= nextCounterBit;
                        if (pendingModuleInputModules.size() != 0) {
                            return null;
                        }
                        switch (pendingModuleOutputModules.size()) {
                            case 0:
                                newPendingModule = null;
                                break;
                            case 1:
                                newPendingModule = pendingModuleOutputModules.iterator().next();
                                break;
                            default:
                                return null;
                        }
                    }
                    else {
                        return null;
                    }
                }
                pendingModuleParent = pendingModule;
                pendingModule = newPendingModule;
                nextCounterBit <<= 1;
            }
            result = ExtendedEuclideanAlgorithm.solveForLcmOnly(result, counterTargetValue);
        }

        if (!remainingCentralConjunctionModules.isEmpty()) {
            return null;
        }

        return result;
    }
}
