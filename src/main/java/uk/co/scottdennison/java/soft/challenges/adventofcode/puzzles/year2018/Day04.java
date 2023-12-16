package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day04 implements IPuzzle {
    private static enum GuardState {
        NOT_ON_DUTY,
        AWAKE,
        ASLEEP
    }

    private static class GuardSchedule {
        private static class Builder {
            private final SortedMap<Integer,GuardState> guardStateChanges;
            private final LocalDate date;
            private Integer guardNumber;

            private Builder(LocalDate date) {
                this.guardStateChanges = new TreeMap<>();
                this.date = date;
            }

            public void setGuardNumber(int guardNumber) {
                if (this.guardNumber != null) {
                    throw new IllegalStateException("Duplicate guard number");
                }
                this.guardNumber = guardNumber;
            }

            public void recordGuardStateChange(int minute, boolean asleep) {
                if (guardStateChanges.put(minute,asleep?GuardState.ASLEEP:GuardState.AWAKE) != null) {
                    throw new IllegalStateException("Multiple state changes at minute " + minute);
                }
            }

            public GuardSchedule build() {
                if (guardNumber == null) {
                    throw new IllegalStateException("No guard number");
                }
                GuardState[] guardStatesPerMinute = new GuardState[60];
                GuardState currentGuardState = GuardState.NOT_ON_DUTY;
                int currentMinute = 0;
                for (Map.Entry<Integer,GuardState> guardStateChangeEntry : guardStateChanges.entrySet()) {
                    int targetMinute = Math.min(60,guardStateChangeEntry.getKey());
                    while (currentMinute < targetMinute) {
                        guardStatesPerMinute[currentMinute++] = currentGuardState;
                    }
                    currentGuardState = guardStateChangeEntry.getValue();
                }
                while (currentMinute < 60) {
                    guardStatesPerMinute[currentMinute++] = currentGuardState;
                }
                return new GuardSchedule(
                    date,
                    guardStatesPerMinute,
                    guardNumber
                );
            }
        }

        public static Builder builder(LocalDate date) {
            return new Builder(date);
        }

        private final LocalDate date;
        private final GuardState[] guardStatesPerMinute;
        private final int guardNumber;

        private GuardSchedule(LocalDate date, GuardState[] guardStatesPerMinute, int guardNumber) {
            this.date = date;
            this.guardStatesPerMinute = guardStatesPerMinute;
            this.guardNumber = guardNumber;
        }

        public int getGuardNumber() {
            return guardNumber;
        }

        public boolean isAsleepAtMinute(int minute) {
            if (minute < 0 || minute >= 60) {
                throw new IllegalArgumentException("Minute out of bounds");
            }
            return guardStatesPerMinute[minute] == GuardState.ASLEEP;
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^\\[(?<year>[0-9]+)-(?<month>[0-9]+)-(?<day>[0-9]+) (?<hour>[0-9]+):(?<minute>[0-9]+)\\] (?<event>.+?) *$");
    private static final Pattern PATTERN_EVENT_SHIFT_START = Pattern.compile("^Guard #(?<guardNumber>[0-9]+) begins shift$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<LocalDate,GuardSchedule.Builder> guardScheduleBuilderMap = new HashMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher lineMatcher = PATTERN_LINE.matcher(inputLine.trim());
            if (!lineMatcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            LocalDate date = LocalDate.of(
                Integer.parseInt(lineMatcher.group("year")),
                Integer.parseInt(lineMatcher.group("month")),
                Integer.parseInt(lineMatcher.group("day"))
            );
            int hour = Integer.parseInt(lineMatcher.group("hour"));
            int minute = Integer.parseInt(lineMatcher.group("minute"));
            switch (hour) {
                case 23:
                    minute -= 60;
                    date = date.plusDays(1);
                    break;
                case 0:
                    break;
                case 1:
                    minute += 60;
                    break;
                default:
                    throw new IllegalStateException("Unexpected hour");
            }
            GuardSchedule.Builder guardScheduleBuilder = guardScheduleBuilderMap.computeIfAbsent(date,GuardSchedule::builder);
            String event = lineMatcher.group("event");
            switch (event) {
                case "falls asleep":
                    guardScheduleBuilder.recordGuardStateChange(minute,true);
                    break;
                case "wakes up":
                    guardScheduleBuilder.recordGuardStateChange(minute,false);
                    break;
                default:
                    Matcher eventShiftStartMatcher = PATTERN_EVENT_SHIFT_START.matcher(event);
                    if (!eventShiftStartMatcher.matches()) {
                        throw new IllegalStateException("Unrecognized event");
                    }
                    guardScheduleBuilder.setGuardNumber(Integer.parseInt(eventShiftStartMatcher.group("guardNumber")));
                    guardScheduleBuilder.recordGuardStateChange(minute,false);
            }
        }
        Map<Integer,Collection<GuardSchedule>> guardSchedulesByGuardNumber = new HashMap<>();
        for (GuardSchedule.Builder guardScheduleBuilder : guardScheduleBuilderMap.values()) {
            GuardSchedule guardSchedule = guardScheduleBuilder.build();
            guardSchedulesByGuardNumber.computeIfAbsent(guardSchedule.getGuardNumber(),__ -> new ArrayList<>()).add(guardSchedule);
        }
        int mostMinutesAsleep = -1;
        int mostAsleepGuardNumber = -1;
        int mostAsleepAtMinuteGuardNumber = -1;
        int mostAsleepAtMinuteMinute = -1;
        int mostAsleepAtMinuteTimes = -1;
        for (Map.Entry<Integer,Collection<GuardSchedule>> guardSchedulesByGuardNumberEntry : guardSchedulesByGuardNumber.entrySet()) {
            int guardNumber = guardSchedulesByGuardNumberEntry.getKey();
            int minutesAsleep = 0;
            for (int minute=0; minute<60; minute++) {
                int asleepTimes = 0;
                for (GuardSchedule guardSchedule : guardSchedulesByGuardNumberEntry.getValue()) {
                    if (guardSchedule.isAsleepAtMinute(minute)) {
                        asleepTimes++;
                        minutesAsleep++;
                    }
                }
                if (asleepTimes > mostAsleepAtMinuteTimes) {
                    mostAsleepAtMinuteTimes = asleepTimes;
                    mostAsleepAtMinuteGuardNumber = guardNumber;
                    mostAsleepAtMinuteMinute = minute;
                }
            }
            if (minutesAsleep > mostMinutesAsleep) {
                mostMinutesAsleep = minutesAsleep;
                mostAsleepGuardNumber = guardNumber;
            }
        }
        Collection<GuardSchedule> guardSchedulesForMostAsleepGuard = guardSchedulesByGuardNumber.get(mostAsleepGuardNumber);
        int mostAsleepMinuteForMostAsleepGuard = -1;
        int mostAsleepMinuteForMostAsleepGuardTimes = -1;
        for (int minute=0; minute<60; minute++) {
            int asleepTimes = 0;
            for (GuardSchedule guardSchedule : guardSchedulesForMostAsleepGuard) {
                if (guardSchedule.isAsleepAtMinute(minute)) {
                    asleepTimes++;
                }
            }
            if (asleepTimes > mostAsleepMinuteForMostAsleepGuardTimes) {
                mostAsleepMinuteForMostAsleepGuardTimes = asleepTimes;
                mostAsleepMinuteForMostAsleepGuard = minute;
            }
        }
        return new BasicPuzzleResults<>(
            mostAsleepGuardNumber * mostAsleepMinuteForMostAsleepGuard,
            mostAsleepAtMinuteGuardNumber * mostAsleepAtMinuteMinute
        );
    }
}
