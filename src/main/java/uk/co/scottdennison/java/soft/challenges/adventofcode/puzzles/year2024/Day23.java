package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Day23 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Set<String>> linkages = new TreeMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            String[] inputLineParts = inputLine.trim().split("-");
            if (inputLineParts.length != 2) {
                throw new IllegalStateException("Expected two parts to the input line");
            }
            // Intering them saves 10ms
            String inputLinePart0 = inputLineParts[0].intern();
            String inputLinePart1 = inputLineParts[1].intern();
            linkages.computeIfAbsent(inputLinePart0,__ -> new HashSet<>()).add(inputLinePart1);
            linkages.computeIfAbsent(inputLinePart1,__ -> new HashSet<>()).add(inputLinePart0);
        }
        List<String[]> clusters = new ArrayList<>();
        clusters.add(new String[0]);
        List<String[]> newClusters = new ArrayList<>();
        int entryNumber = 0;
        for (Map.Entry<String,Set<String>> linkagesEntry : linkages.entrySet()) {
            String linkageSource = linkagesEntry.getKey();
            Set<String> linkageDestinations = linkagesEntry.getValue();
            for (String[] cluster : clusters) {
                boolean clusterMatches = true;
                for (String clusterComputer : cluster) {
                    if (!linkageDestinations.contains(clusterComputer)) {
                        clusterMatches = false;
                        break;
                    }
                }
                if (clusterMatches) {
                    int clusterSize = cluster.length;
                    String[] newCluster = new String[clusterSize + 1];
                    System.arraycopy(cluster, 0, newCluster, 0, clusterSize);
                    newCluster[clusterSize] = linkageSource;
                    newClusters.add(newCluster);
                }
            }
            clusters.addAll(newClusters);
            newClusters.clear();
        }
        SortedMap<Integer,String[][]> clustersBySize = clusters.stream().collect(Collectors.groupingBy(cluster -> cluster.length, TreeMap::new, Collectors.collectingAndThen(Collectors.toList(), list -> list.toArray(new String[list.size()][]))));
        int clustersContainsComputerStartingWithTCount = 0;
        for (String[] cluster : clustersBySize.getOrDefault(3, new String[0][])) {
            boolean clusterContainsComputerStartingWithT = false;
            for (String clusterComputer : cluster) {
                if (clusterComputer.charAt(0) == 't') {
                    clusterContainsComputerStartingWithT = true;
                    break;
                }
            }
            if (clusterContainsComputerStartingWithT) {
                clustersContainsComputerStartingWithTCount++;
            }
        }
        String[][] largestClusters = clustersBySize.get(clustersBySize.lastKey());
        if (largestClusters.length != 1) {
            throw new IllegalStateException("Expected exactly one largest cluster");
        }
        return new BasicPuzzleResults<>(
            clustersContainsComputerStartingWithTCount,
            Arrays.stream(largestClusters[0]).sorted().collect(Collectors.joining(","))
        );
    }
}
