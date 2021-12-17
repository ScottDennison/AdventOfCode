package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;
import java.util.LinkedList;

public class Day17 implements IPuzzle {
    private static class Route {
        private final int x;
        private final int y;
        private final byte[] hashInputReference;

        public Route(int x, int y, byte[] hashInputReference) {
            this.x = x;
            this.y = y;
            this.hashInputReference = hashInputReference;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public byte[] getHashInputReference() {
            return this.hashInputReference;
        }
    }

    private static final int MIN_X = 0;
    private static final int MIN_Y = 0;
    private static final int MAX_X = 3;
    private static final int MAX_Y = 3;
    private static final int START_X = MIN_X;
    private static final int START_Y = MIN_Y;
    private static final int TARGET_X = MAX_X;
    private static final int TARGET_Y = MAX_Y;
    private static final int MIN_VALID_HASH_VALUE = 11;
    private static final int MAX_VALID_HASH_VALUE = 15;

    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        byte[] passcode = new String(inputCharacters).trim().getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not create MessageDigest instance",ex);
        }
        Deque<Route> thisLengthPossibilities = new LinkedList<>();
        thisLengthPossibilities.addLast(new Route(START_X,START_Y,passcode));
        int routeLength = 0;
        int shortestRouteLength = 0;
        String shortestRoute = null;
        int longestRouteLength = -1;
        while (true) {
            int thisLengthPossibilitiesCount = thisLengthPossibilities.size();
            if (thisLengthPossibilitiesCount == 0) {
                break;
            }
            printWriter.println("Trying " + thisLengthPossibilitiesCount + " possibilit" + (thisLengthPossibilitiesCount==1?"y":"ies") + " with length " + routeLength);
            Deque<Route> nextLengthPossibilities = new LinkedList<>();
            Route currentRoute;
            while ((currentRoute=thisLengthPossibilities.pollFirst()) != null) {
                int currentX = currentRoute.getX();
                int currentY = currentRoute.getY();
                byte[] currentHashInput = currentRoute.getHashInputReference();
                if (currentX == TARGET_X && currentY == TARGET_Y) {
                    if (shortestRoute == null) {
                        shortestRoute = new String(currentHashInput, passcode.length, currentHashInput.length-passcode.length, StandardCharsets.US_ASCII);
                        printWriter.println("Shortest route found");
                        shortestRouteLength = routeLength;
                    } else if (routeLength == shortestRouteLength) {
                        throw new IllegalStateException("Multiple shortest solutions.");
                    }
                    longestRouteLength = routeLength;
                } else {
                    byte[] hash = messageDigest.digest(currentHashInput);
                    attemptRoute(nextLengthPossibilities, currentX, currentY - 1, hash, 0, 'U', currentHashInput);
                    attemptRoute(nextLengthPossibilities, currentX, currentY + 1, hash, 1, 'D', currentHashInput);
                    attemptRoute(nextLengthPossibilities, currentX - 1, currentY, hash, 2, 'L', currentHashInput);
                    attemptRoute(nextLengthPossibilities, currentX + 1, currentY, hash, 3, 'R', currentHashInput);
                }
            }
            thisLengthPossibilities = nextLengthPossibilities;
            routeLength++;
        }
        if (shortestRoute == null) {
            throw new IllegalStateException("No possible route found");
        }
        return new BasicPuzzleResults<>(
            shortestRoute,
            longestRouteLength
        );
    }

    private static void attemptRoute(Deque<Route> nextLengthPossibilities, int potentialX, int potentialY, byte[] hash, int hashCharacterIndex, char newHashInputCharacter, byte[] currentHashInput) {
        if (potentialX >= MIN_X && potentialX <= MAX_X && potentialY >= MIN_Y && potentialY <= MAX_Y) {
            int hashValue = (hash[hashCharacterIndex>>1]>>((hashCharacterIndex&1)==0?4:0))&0xF;
            if (hashValue >= MIN_VALID_HASH_VALUE && hashValue <= MAX_VALID_HASH_VALUE) {
                int currentHashInputLength = currentHashInput.length;
                byte[] newHashInput = new byte[currentHashInputLength+1];
                System.arraycopy(currentHashInput,0,newHashInput,0,currentHashInputLength);
                newHashInput[currentHashInputLength] = (byte)newHashInputCharacter;
                nextLengthPossibilities.addLast(new Route(potentialX,potentialY,newHashInput));
            }
        }
    }
}
