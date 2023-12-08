package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day21 implements IPuzzle {
    private interface Action {
        char[] process(char[] input);
    }

    private static abstract class ActionLineProcessor {
        private final Pattern pattern;

        public ActionLineProcessor(String patternRegex) {
            this.pattern = Pattern.compile("^" + patternRegex + "$");
        }

        protected Optional<Action> processLine(String line) {
            Matcher matcher = this.pattern.matcher(line);
            if (matcher.matches()) {
                return Optional.of(createAction(matcher));
            }
            else {
                return Optional.empty();
            }
        }

        protected abstract Action createAction(Matcher matcher);
    }

    private static final ActionLineProcessor[] ACTION_LINE_PROCESSORS = {
        new ActionLineProcessor("swap position (?<x>[0-9]+) with position (?<y>[0-9]+)") {
            @Override
            protected Action createAction(Matcher matcher) {
                final int x = Integer.parseInt(matcher.group("x"));
                final int y = Integer.parseInt(matcher.group("y"));
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        return swap(input,x,y);
                    }
                };
            }
        },
        new ActionLineProcessor("swap letter (?<x>[a-z]) with letter (?<y>[a-z])") {
            @Override
            protected Action createAction(Matcher matcher) {
                final char x = matcher.group("x").charAt(0);
                final char y = matcher.group("y").charAt(0);
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        return swap(input,find(input,x),find(input,y));
                    }
                };
            }
        },
        new ActionLineProcessor("rotate (?<direction>[a-z]+) (?<x>[0-9]+) step(?:s)?") {
            @Override
            protected Action createAction(Matcher matcher) {
                final int stepsMultiplier;
                switch (matcher.group("direction")) {
                    case "left":
                        stepsMultiplier = -1;
                        break;
                    case "right":
                        stepsMultiplier = 1;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected direction");
                }
                final int steps = Integer.parseInt(matcher.group("x"))*stepsMultiplier;
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        return rotate(input, steps);
                    }
                };
            }
        },
        new ActionLineProcessor("rotate based on position of letter (?<x>[a-z])") {
            @Override
            protected Action createAction(Matcher matcher) {
                final char x = matcher.group("x").charAt(0);
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        int index = find(input, x);
                        return rotate(input, 1+index+(index>=4?1:0));
                    }
                };
            }
        },
        new ActionLineProcessor("reverse positions (?<x>[0-9]+) through (?<y>[0-9]+)") {
            @Override
            protected Action createAction(Matcher matcher) {
                final int x = Integer.parseInt(matcher.group("x"));
                final int y = Integer.parseInt(matcher.group("y"));
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        return reverse(input,x,y);
                    }
                };
            }
        }
        ,
        new ActionLineProcessor("move position (?<x>[0-9]+) to position (?<y>[0-9]+)") {
            @Override
            protected Action createAction(Matcher matcher) {
                final int x = Integer.parseInt(matcher.group("x"));
                final int y = Integer.parseInt(matcher.group("y"));
                return new Action() {
                    @Override
                    public char[] process(char[] input) {
                        return move(input,x,y);
                    }
                };
            }
        }
    };

    private static char[] swap(char[] input, int index1, int index2) {
        if (index1 == index2) {
            return input;
        }
        char[] output = Arrays.copyOf(input,input.length);
        char temp = output[index1];
        output[index1] = output[index2];
        output[index2] = temp;
        return output;
    }

    private static int find(char[] input, char character) {
        int length = input.length;
        for (int index=0; index<length; index++) {
            if (input[index] == character) {
                return index;
            }
        }
        throw new IllegalArgumentException("No such character");
    }

    private static char[] rotate(char[] input, int steps) {
        int inputLength = input.length;
        int absoluteSteps = steps;
        while (absoluteSteps < inputLength) {
            absoluteSteps += inputLength;
        }
        char[] output = new char[inputLength];
        for (int index=0; index<inputLength; index++) {
            output[(index + absoluteSteps) % inputLength] = input[index];
        }
        return output;
    }

    private static char[] reverse(char[] input, int x, int y) {
        char[] output = Arrays.copyOf(input,input.length);
        for (int xIter=x, yIter=y; xIter<=y; xIter++, yIter--) {
            output[xIter] = input[yIter];
        }
        return output;
    }

    private static char[] move(char[] input, int x, int y) {
        int inputLength = input.length;
        char[] temp = new char[inputLength-1];
        char[] output = new char[inputLength];
        System.arraycopy(input, 0, temp, 0, x);
        System.arraycopy(input, x+1, temp, x, inputLength-1-x);
        System.arraycopy(temp, 0, output, 0, y);
        output[y] = input[x];
        System.arraycopy(temp, y, output, y+1, inputLength-1-y);
        return output;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int actionCount = inputLines.length;
        Action[] actions = new Action[actionCount];
        for (int actionIndex=0; actionIndex<actionCount; actionIndex++) {
            Action action = null;
            String inputLine = inputLines[actionIndex];
            for (ActionLineProcessor actionLineProcessor : ACTION_LINE_PROCESSORS) {
                Optional<Action> optionalAction = actionLineProcessor.processLine(inputLine);
                if (optionalAction.isPresent()) {
                    if (action != null) {
                        throw new IllegalStateException("Multiple matching options.");
                    }
                    action = optionalAction.get();
                    break;
                }
            }
            if (action == null) {
                throw new IllegalStateException("No matching options");
            }
            actions[actionIndex]  = action;
        }
        return new BasicPuzzleResults<>(
            new String(scramble(actions,new String(configProvider.getPuzzleConfigChars("start_password")).trim().toCharArray())),
            new String(recurseForTarget(actions,new String(configProvider.getPuzzleConfigChars("existing_password")).trim().toCharArray()))
        );
    }

    private static char[] scramble(Action[] actions, char[] password) {
        for (Action action : actions) {
            password = action.process(password);
        }
        return password;
    }

    private static char[] recurseForTarget(Action[] actions, char[] targetPassword) {
        int passwordLength = targetPassword.length;
        boolean[] used = new boolean[passwordLength];
        char[] startPassword = new char[passwordLength];
        return recurseForTarget(actions, startPassword, targetPassword, used, 0, passwordLength);
    }

    private static char[] recurseForTarget(Action[] actions, char[] startPassword, char[] targetPassword, boolean[] used, int recurseLevel, int passwordLength) {
        if (recurseLevel == passwordLength) {
            char[] scrambledPassword = scramble(actions, startPassword);
            for (int index=0; index<passwordLength; index++) {
                if (scrambledPassword[index] != targetPassword[index]) {
                    return null;
                }
            }
            return startPassword;
        } else {
            int nextRecurseLevel = recurseLevel+1;
            for (int index=0; index<passwordLength; index++) {
                if (!used[index]) {
                    startPassword[recurseLevel] = targetPassword[index];
                    used[index] = true;
                    char[] result = recurseForTarget(actions, startPassword, targetPassword, used, nextRecurseLevel, passwordLength);
                    if (result != null) {
                        return result;
                    }
                    used[index] = false;
                }
            }
            return null;
        }
    }
}
