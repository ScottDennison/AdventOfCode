package uk.co.scottdennison.java.soft.challenges.adventofcode.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataDownloader {
    private static class User {
        private final int number;
        private final String sessionToken;

        private User(int number, String sessionToken) {
            this.number = number;
            this.sessionToken = sessionToken;
        }

        public int getNumber() {
            return this.number;
        }

        public String getSessionToken() {
            return this.sessionToken;
        }
    }

    private static final Pattern PATTERN_ANONYMOUS_USER_OPTION_TEXT = Pattern.compile("^\\(anonymous user #(?<userNumber>[0-9]+)\\)$");
    private static final Pattern PATTERN_YEAR_LINK_TEXT = Pattern.compile("^\\[(?<yearNumber>[0-9]+)\\]$");

    public static void main(String[] args) throws IOException {
        System.out.println("Starting");
        int userCount = args.length;
        if (userCount < 1) {
            throw new IllegalArgumentException("Expected at least one argument. Each argument is expected to be a session token.");
        }
        Set<String> uniqueSessionTokens = new HashSet<>();
        SortedMap<Integer,User> userNumberToUserMap = new TreeMap<>();
        for (int userIndex=0; userIndex<userCount; userIndex++) {
            String sessionToken = args[userIndex];
            if (!uniqueSessionTokens.add(sessionToken)) {
                throw new IllegalArgumentException("Session token " + sessionToken + " appears multiple times");
            }
            int userNumber = fetchUserNumber(sessionToken);
            User newUser = new User(userNumber, sessionToken);
            User existingUser = userNumberToUserMap.put(userNumber, newUser);
            if (existingUser != null) {
                throw new IllegalStateException("Session tokens " + sessionToken + " and " + existingUser.getSessionToken() + " both point to user number " + userNumber);
            }
        }
        Path dataDirectoryPath = Paths.get("data");
        if (!Files.isDirectory(dataDirectoryPath)) {
            throw new IllegalStateException("Data directory does not exist");
        }
        for (int yearNumber : fetchYearNumbers()) {
            Path yearDataDirectoryPath = dataDirectoryPath.resolve(String.format("year%04d",yearNumber));
            for (int dayNumber : fetchDayNumbersForYear(yearNumber)) {
                Path dayDataDirectoryPath = yearDataDirectoryPath.resolve(String.format("day%02d",dayNumber));
                Path ioDirectoryPath = dayDataDirectoryPath.resolve("io");
                Path examplesDirectoryPath = ioDirectoryPath.resolve("examples");
                createDirectories(examplesDirectoryPath);
                handleExamples(examplesDirectoryPath);
                Path usersDirectoryPath = ioDirectoryPath.resolve("users");
                for (User user : userNumberToUserMap.values()) {
                    Path userDirectoryPath = usersDirectoryPath.resolve(Integer.toString(user.getNumber()));
                    createDirectories(userDirectoryPath);
                    handleUserInput(userDirectoryPath,yearNumber,dayNumber,user.getSessionToken());
                    handleUserOutput(userDirectoryPath,"a");
                    handleUserOutput(userDirectoryPath,"b");
                }
            }
        }
        System.out.println("Done");
    }

    private static void createDirectories(Path desiredDirectoryPath) throws IOException {
        Deque<Path> pathsToCreate = new LinkedList<>();
        Path currentPath = desiredDirectoryPath;
        while (!Files.isDirectory(currentPath)) {
            pathsToCreate.addFirst(currentPath);
            currentPath = currentPath.getParent();
        }
        for (Path pathToCreate : pathsToCreate) {
            System.out.println("Creating directory " + pathToCreate);
            Files.createDirectory(pathToCreate);
        }
    }

    private static void createFile(Path desiredFilePath) throws IOException {
        System.out.println("Creating empty file " + desiredFilePath);
        Files.createFile(desiredFilePath);
    }

    private static void createFile(Path desiredFilePath, byte[] contents) throws IOException {
        System.out.println("Creating file with contents " + desiredFilePath);
        Files.write(desiredFilePath, contents);
    }

    private static void handleExamples(Path examplesDirectoryPath) throws IOException {
        boolean examplesDirectoryEmpty;
        try (DirectoryStream<Path> examplesDirectoryStream = Files.newDirectoryStream(examplesDirectoryPath)) {
            examplesDirectoryEmpty = !examplesDirectoryStream.iterator().hasNext();
        }
        if (examplesDirectoryEmpty) {
            createFile(examplesDirectoryPath.resolve(".not_yet_populated"));
        }
    }

    private static void handleUserInput(Path userDirectoryPath, int yearNumber, int dayNumber, String sessionToken) throws IOException {
        Path inputFile = userDirectoryPath.resolve("input.txt");
        if (!Files.isRegularFile(inputFile)) {
            createFile(inputFile,fetchInput(yearNumber,dayNumber,sessionToken));
        }
    }

    private static void handleUserOutput(Path userDirectoryPath, String outputName) throws IOException {
        String standardFileName = "output_" + outputName + ".txt";
        Path standardFilePath = userDirectoryPath.resolve(standardFileName);
        if (Files.isRegularFile(standardFilePath)) {
            return;
        }
        String missingFileName = standardFileName + ".missing";
        Path missingFilePath = userDirectoryPath.resolve(missingFileName);
        if (Files.isRegularFile(missingFilePath)) {
            return;
        }
        createFile(missingFilePath);
    }

    private static Connection openConnection(String path) {
        //System.out.println("Opening connection to " + path);
        return Jsoup.connect("https://adventofcode.com/" + path);
    }

    private static Connection openConnection(String path, String sessionToken) {
        Connection connection = openConnection(path);
        //System.out.println("Setting session cookie to " + sessionToken);
        connection = connection.cookie("session", sessionToken);
        return connection;
    }

    private static Connection.Response fetchResponse(Connection connection) throws IOException {
        //System.out.println("Fetching response");
        Connection.Response response = connection.execute();
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            throw new IllegalStateException("Unexpected status code");
        }
        return response;
    }

    private static Document fetchDocument(Connection connection) throws IOException {
        //System.out.println("Parsing response");
        return fetchResponse(connection).parse();
    }

    private static int fetchUserNumber(String sessionToken) throws IOException {
        Document document = fetchDocument(openConnection("settings", sessionToken));
        Matcher matcher = PATTERN_ANONYMOUS_USER_OPTION_TEXT.matcher(document.select("input[type=\"radio\"][name=\"display_name\"][value=\"anonymous\"]").get(0).parent().text());
        if (!matcher.matches()) {
            throw new IllegalStateException("Could not extract user number");
        }
        return Integer.parseInt(matcher.group("userNumber"));
    }

    private static SortedSet<Integer> fetchYearNumbers() throws IOException {
        Document document = fetchDocument(openConnection("events"));
        SortedSet<Integer> yearNumbers = new TreeSet<>();
        for (Element element : document.select(".eventlist-event a")) {
            Matcher matcher = PATTERN_YEAR_LINK_TEXT.matcher(element.text());
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not extract year number");
            }
            int yearNumber = Integer.parseInt(matcher.group("yearNumber"));
            if (!yearNumbers.add(yearNumber)) {
                throw new IllegalStateException("Duplicate year number: " + yearNumber);
            }
        }
        if (yearNumbers.isEmpty()) {
            throw new IllegalStateException("No years found");
        }
        return yearNumbers;
    }

    private static SortedSet<Integer> fetchDayNumbersForYear(int yearNumber) throws IOException {
        Document document = fetchDocument(openConnection(Integer.toString(yearNumber)));
        SortedSet<Integer> dayNumbers = new TreeSet<>();
        for (Element element : document.select(".calendar a .calendar-day")) {
            int dayNumber = Integer.parseInt(element.text());
            if (!dayNumbers.add(dayNumber)) {
                throw new IllegalStateException("Duplicate day number: " + dayNumber);
            }
        }
        if (dayNumbers.isEmpty()) {
            throw new IllegalStateException("No days found");
        }
        return dayNumbers;
    }

    private static byte[] fetchInput(int yearNumber, int dayNumber, String sessionToken) throws IOException {
        return fetchResponse(openConnection(yearNumber + "/day/" + dayNumber + "/input", sessionToken)).bodyAsBytes();
    }
}
