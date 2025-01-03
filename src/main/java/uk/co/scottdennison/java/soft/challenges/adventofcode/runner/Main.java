package uk.co.scottdennison.java.soft.challenges.adventofcode.runner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import uk.co.scottdennison.java.libs.text.output.DisplayWriter;
import uk.co.scottdennison.java.libs.text.output.table.DisplayTextualTableBuilder;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
	private static final Pattern PATTERN_NEWLINE = Pattern.compile("\\R");

	private static final int MIN_YEAR = 2015;
	private static final int MAX_YEAR = 2024;
	private static final int MIN_DAY = 1;
	private static final int MAX_DAY = 25;

	public static class CommandLineBean {
		@Option(name = "-y", aliases = "--year", usage = "only run puzzles from a specified year")
		private List<Integer> years;

		@Option(name = "-d", aliases = "--day", usage = "only run puzzles from a specified year")
		private List<Integer> days;

		@Option(name = "-u", aliases = "--user", usage = "only run puzzle data sets for user u")
		private List<Integer> users;

		@Option(name = "-j", aliases = "--pre-run-for-jit", usage = "run each data set twice, discarding the first time, in a bid to pre-JIT the code")
		private boolean preRunForJIT;

		private Set<Integer> processSetList(List<Integer> values) {
			if (values == null) {
				return Collections.emptySet();
			}
			return new HashSet<>(values);
		}

		public Set<Integer> getYears() {
			return this.processSetList(this.years);
		}

		public Set<Integer> getDays() {
			return this.processSetList(this.days);
		}

		public Set<Integer> getUsers() {
			return this.processSetList(this.users);
		}

		public boolean isPreRunForJIT() {
			return this.preRunForJIT;
		}
	}

	private static class PuzzleRunResults {
		private final String dataSetName;
		private final boolean exceptionOccured;
		private final long nanoseconds;
		private final PuzzleRunPartResults partAResults;
		private final PuzzleRunPartResults partBResults;

		private PuzzleRunResults(String dataSetName, long nanoseconds, boolean exceptionOccured, PuzzleRunPartResults partAResults, PuzzleRunPartResults partBResults) {
			this.dataSetName = dataSetName;
			this.nanoseconds = nanoseconds;
			this.exceptionOccured = exceptionOccured;
			this.partAResults = partAResults;
			this.partBResults = partBResults;
		}

		public String getDataSetName() {
			return this.dataSetName;
		}

		public long getNanoseconds() {
			return this.nanoseconds;
		}

		public boolean isExceptionOccured() {
			return this.exceptionOccured;
		}

		public PuzzleRunPartResults getPartAResults() {
			return this.partAResults;
		}

		public PuzzleRunPartResults getPartBResults() {
			return this.partBResults;
		}

		private static PuzzleRunResults createForResults(String dataSetName, long nanoseconds, PuzzleRunPartResults partAResults, PuzzleRunPartResults partBResults) {
			return new PuzzleRunResults(dataSetName, nanoseconds, false, partAResults, partBResults);
		}

		private static PuzzleRunResults createForException(String dataSetName, long nanoseconds) {
			return new PuzzleRunResults(dataSetName, nanoseconds, true, null, null);
		}
	}

	private static class PuzzleRunPartResults {
		public enum State {
			SUCCESS ("\u001B[32m"),
			UNKNOWN ("\u001B[33m"),
			FAILURE ("\u001B[31m");

			private final String ansiString;

			State(String ansiColourCode) {
				this.ansiString = ansiColourCode + this.name() + "\u001B[0m";
			}

			public String getAnsiString() {
				return ansiString;
			}
		}

		private final String expectedResult;
		private final String actualResult;
		private final State state;

		public PuzzleRunPartResults(String expectedResult, String actualResult) {
			this.expectedResult = expectedResult;
			this.actualResult = actualResult;
			if (expectedResult != null) {
				if (Objects.equals(actualResult, expectedResult)) {
					this.state = State.SUCCESS;
				}
				else {
					this.state = State.FAILURE;
				}
			}
			else {
				this.state = State.UNKNOWN;
			}
		}

		public boolean hasExpectedResult() {
			return this.expectedResult != null;
		}

		public String getExpectedResult() {
			return this.expectedResult;
		}

		public String getActualResult() {
			return this.actualResult;
		}

		public State getState() {
			return this.state;
		}
	}

	private static class PuzzleConfigProvider implements IPuzzleConfigProvider {
		private final Path dataSetPath;
		private final Path dataPath;
		private long timeSpentLoadingConfig;

		public PuzzleConfigProvider(Path dataSetPath, Path dataPath) {
			this.dataSetPath = dataSetPath;
			this.dataPath = dataPath;
			this.timeSpentLoadingConfig = 0L;
		}

		@Override
		public char[] getPuzzleConfigChars(String configName) {
			long startTime = System.nanoTime();
			String configFileName = "config_" + configName + ".txt";
			Path directoryPath = dataSetPath;
			while (true) {
				Path filePath = directoryPath.resolve(configFileName);
				if (Files.isRegularFile(filePath)) {
					char[] result = readFile(filePath);
					long endTime = System.nanoTime();
					timeSpentLoadingConfig += (endTime-startTime);
					return result;
				}
				if (directoryPath.equals(dataPath)) {
					throw new IllegalStateException("No such config found.");
				}
				directoryPath = directoryPath.getParent();
			}
		}

		public long getTimeSpentLoadingConfig() {
			return timeSpentLoadingConfig;
		}
	}

	private static class PuzzleRunner {
		private final int year;
		private final int day;
		private final Class<? extends IPuzzle> puzzleClazz;

		public PuzzleRunner(int year, int day, Class<? extends IPuzzle> puzzleClazz) {
			this.year = year;
			this.day = day;
			this.puzzleClazz = puzzleClazz;
			// Pre-load the class
			this.createPuzzleInstance();
		}

		public void run(PrintWriter consoleWriter, boolean restrictedCharacterSet, Set<Integer> userFilter, boolean preRunForJIT) {
			Path dataPath = Paths.get(String.format("data/year%04d/day%02d/io", this.year, this.day));
			consoleWriter.format("Running year %d day %d%n", this.year, this.day);
			List<PuzzleRunResults> puzzleRunResults = new ArrayList<>();
			if (userFilter.isEmpty()) {
				puzzleRunResults.addAll(runWithDataSets(consoleWriter, dataPath, "Example", "Examples", "examples", preRunForJIT, null));
			}
			puzzleRunResults.addAll(runWithDataSets(consoleWriter, dataPath, "User", "Users", "users", preRunForJIT, userFilter.stream().map(x -> x.toString()).collect(Collectors.toSet())));
			DisplayTextualTableBuilder displayTextualTableBuilder = new DisplayTextualTableBuilder();
			displayTextualTableBuilder.setDefaultHorizontalAlignment(DisplayTextualTableBuilder.HorizontalAlignment.CENTER_BLOCK);
			displayTextualTableBuilder.setDefaultVerticalAlignment(DisplayTextualTableBuilder.VerticalAlignment.MIDDLE);
			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			Map<PuzzleRunPartResults.State,Integer> stateCounts = new EnumMap<>(PuzzleRunPartResults.State.class);
			boolean exceptionOccuredAtLeastOnce = false;
			for (PuzzleRunResults puzzleRunResultsEntry : puzzleRunResults) {
				if (puzzleRunResultsEntry.isExceptionOccured()) {
					exceptionOccuredAtLeastOnce = true;
				}
			}
			for (PuzzleRunResults puzzleRunResultsEntry : puzzleRunResults) {
				displayTextualTableBuilder.addRow(true);
				displayTextualTableBuilder.addEntry("Data Set", puzzleRunResultsEntry.getDataSetName(), DisplayTextualTableBuilder.HorizontalAlignment.LEFT);
				displayTextualTableBuilder.addEntry("Time taken (ns)", numberFormat.format(puzzleRunResultsEntry.getNanoseconds()), DisplayTextualTableBuilder.HorizontalAlignment.RIGHT);
				if (puzzleRunResultsEntry.isExceptionOccured()) {
					displayTextualTableBuilder.addEntry("State","EXCEPTION");
				}
				else {
					if (exceptionOccuredAtLeastOnce) {
						displayTextualTableBuilder.addEntry("State", "OK");
					}
					addPuzzleRunPartResultsToTable(displayTextualTableBuilder, stateCounts, "A", puzzleRunResultsEntry.getPartAResults());
					addPuzzleRunPartResultsToTable(displayTextualTableBuilder, stateCounts, "B", puzzleRunResultsEntry.getPartBResults());
				}
			}
			consoleWriter.write(displayTextualTableBuilder.build("\t", restrictedCharacterSet ? DisplayTextualTableBuilder.CharacterSet.ASCII : DisplayTextualTableBuilder.CharacterSet.BASIC_BOX_DRAWING_DOUBLE_WIDTH_OUTER, true, null));
			boolean first = true;
			for (Map.Entry<PuzzleRunPartResults.State,Integer> stateCountEntry : stateCounts.entrySet()) {
				int count = stateCountEntry.getValue();
				if (count < 1) {
					continue;
				}
				if (first) {
					consoleWriter.write("\tSummary: ");
					first = false;
				}
				else {
					consoleWriter.write(", ");
				}
				consoleWriter.format("%d x %s",count,stateCountEntry.getKey().getAnsiString());
			}
			if (!first) {
				consoleWriter.format("%n");
			}
			consoleWriter.flush();
		}

		private void addPuzzleRunPartResultsToTable(DisplayTextualTableBuilder displayTextualTableBuilder, Map<PuzzleRunPartResults.State,Integer> stateCounts, String partCode, PuzzleRunPartResults partResults) {
			PuzzleRunPartResults.State state = partResults.getState();
			displayTextualTableBuilder.addEntry("Part " + partCode + " - Expected Answer", partResults.hasExpectedResult()?partResults.getExpectedResult():"", DisplayTextualTableBuilder.HorizontalAlignment.RIGHT_BLOCK);
			displayTextualTableBuilder.addEntry("Part " + partCode + " - Actual Answer", partResults.getActualResult(), DisplayTextualTableBuilder.HorizontalAlignment.RIGHT_BLOCK);
			displayTextualTableBuilder.addEntry("Part " + partCode + " - State", state.getAnsiString());
			stateCounts.put(state, stateCounts.getOrDefault(state,0) + 1);
		}

		private List<PuzzleRunResults> runWithDataSets(PrintWriter consoleWriter, Path dataPath, String dataSetsNameSingular, String dataSetsNamePlural, String dataSetsFolderName, boolean preRunForJIT, Set<String> filters) {
			Path dataSetsPath = dataPath.resolve(dataSetsFolderName);
			List<Path> dataSetPaths = new ArrayList<>();
			Set<String> caseInsensitiveFilters;
			boolean noFilter = filters == null || filters.isEmpty();
			if (noFilter) {
				caseInsensitiveFilters = null;
			}
			else {
				caseInsensitiveFilters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
				caseInsensitiveFilters.addAll(filters);
			}
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dataSetsPath, Files::isDirectory)) {
				for (Path path : directoryStream) {
					if (noFilter || caseInsensitiveFilters.contains(path.getFileName().toString())) {
						dataSetPaths.add(path);
					}
				}
			} catch (IOException ex) {
				throw new IllegalStateException("Unable to iterate directory " + dataSetsPath, ex);
			}
			if (preRunForJIT) {
				consoleWriter.format("\tPre-running " + dataSetsNamePlural + " data sets for JIT to make any optimizations, discarding results%n");
				consoleWriter.flush();
				for (Path dataSetPath : dataSetPaths) {
					runWithDataSet(consoleWriter, dataPath, dataSetPath, dataSetsNameSingular + " " + dataSetPath.getFileName().toString());
				}
			}
			consoleWriter.format("\tRunning " + dataSetsNamePlural + " data sets to gather results%n");
			consoleWriter.flush();
			List<PuzzleRunResults> puzzleRunResults = new ArrayList<>();
			for (Path dataSetPath : dataSetPaths) {
				puzzleRunResults.add(runWithDataSet(consoleWriter, dataPath, dataSetPath, dataSetsNameSingular + " " + dataSetPath.getFileName().toString()));
			}
			return puzzleRunResults;
		}

		private PuzzleRunResults runWithDataSet(PrintWriter consoleWriter, Path dataPath, Path dataSetPath, String dataSetName) {
			consoleWriter.format("\t\tRunning with data set %s%n", dataSetName);
			consoleWriter.flush();
			char[] inputCharacters = readDataSetFile(dataSetPath, "input.txt", false);
			char[] outputACharacters = readDataSetFile(dataSetPath, "output_a.txt", true);
			char[] outputBCharacters = readDataSetFile(dataSetPath, "output_b.txt", true);
			PrintWriter displayPrinterWriter = new PrintWriter(new DisplayWriter(consoleWriter, "----------------------------------------", "\t\t", false));
			boolean partBPotentiallyUnsolvable = outputBCharacters == null;
			PuzzleConfigProvider puzzleConfigProvider = new PuzzleConfigProvider(dataSetPath,dataPath);
			long startTime = -1;
			long finishTime = -1;
			boolean exceptionOccured = false;
			IPuzzleResults puzzleResults;
			// Try and hint to the JVM to clean up any garbage from any previous puzzle runs.
			System.gc();
			try {
				startTime = System.nanoTime();
				puzzleResults = this.createPuzzleInstance().runPuzzle(inputCharacters, puzzleConfigProvider, partBPotentiallyUnsolvable, displayPrinterWriter);
				finishTime = System.nanoTime();
				return PuzzleRunResults.createForResults(
					dataSetName,
					finishTime - startTime - puzzleConfigProvider.getTimeSpentLoadingConfig(),
					createPartResults(outputACharacters, puzzleResults == null ? null : puzzleResults.getPartAAnswerString()),
					createPartResults(outputBCharacters, puzzleResults == null ? null : puzzleResults.getPartBAnswerString())
				);
			} catch (Exception ex) {
				finishTime = System.nanoTime();
				displayPrinterWriter.println();
				ex.printStackTrace(displayPrinterWriter);
				return PuzzleRunResults.createForException(
					dataSetName,
					finishTime - startTime - puzzleConfigProvider.getTimeSpentLoadingConfig()
				);
			} finally {
				displayPrinterWriter.close();
			}
		}

		private static PuzzleRunPartResults createPartResults(char[] expectedResult, String actualResult) {
			String expectedResultTrimmedString;
			String actualResultTrimmedString;
			if (expectedResult == null) {
				expectedResultTrimmedString = null;
			}
			else {
				expectedResultTrimmedString = normalizeNewlines(new String(expectedResult)).trim();
			}
			if (actualResult == null) {
				actualResultTrimmedString = null;
			}
			else {
				actualResultTrimmedString = normalizeNewlines(actualResult).trim();
			}
			return new PuzzleRunPartResults(expectedResultTrimmedString, actualResultTrimmedString);
		}

		private static String normalizeNewlines(String input) {
			return PATTERN_NEWLINE.matcher(input).replaceAll("\n");
		}

		private IPuzzle createPuzzleInstance() {
			try {
				return this.puzzleClazz.newInstance();
			} catch (IllegalAccessException | InstantiationException ex) {
				throw new IllegalStateException("Unable to instantiate puzzle class", ex);
			}
		}
	}

	private static char[] readDataSetFile(Path dataSetPath, String fileName, boolean allowMissing) {
		Path path = dataSetPath.resolve(fileName);
		if (Files.isRegularFile(path)) {
			return readFile(path);
		}
		else if (allowMissing) {
			Path missingPath = dataSetPath.resolve(fileName + ".missing");
			if (Files.isRegularFile(missingPath)) {
				char[] fileContents = readFile(missingPath);
				if (fileContents.length == 0) {
					return null;
				}
			}
		}
		throw new IllegalStateException("Could not read file " + path + (allowMissing ? " or an equivalent .missing file" : ""));
	}

	private static char[] readFile(Path path) {
		byte[] fileBytes;
		try {
			fileBytes = Files.readAllBytes(path);
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to read file bytes");
		}
		CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(fileBytes));
		if (charBuffer.hasArray() && !charBuffer.isReadOnly()) {
			return charBuffer.array();
		}
		else {
			char[] chars = new char[charBuffer.remaining()];
			charBuffer.get(chars);
			return chars;
		}
	}

	public static void main(String[] args) {
		CommandLineBean commandLineBean = new CommandLineBean();
		CmdLineParser cmdLineParser = new CmdLineParser(commandLineBean);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException ex) {
			System.err.println("Could not parse command line arguments");
			cmdLineParser.printUsage(System.err);
			return;
		}
		runPuzzles(commandLineBean.getYears(), commandLineBean.getDays(), commandLineBean.getUsers(), commandLineBean.isPreRunForJIT());
	}

	private static void runPuzzles(Set<Integer> yearFilter, Set<Integer> dayFilter, Set<Integer> userFilter, boolean preRunForJIT) {
		List<PuzzleRunner> puzzleRunners = new ArrayList<>();
		String mainClassName = Main.class.getName();
		String mainPackageName = mainClassName.substring(0, mainClassName.lastIndexOf('.'));
		String mainParentPackageName = mainPackageName.substring(0, mainPackageName.lastIndexOf('.'));
		String puzzlesPackageName = mainParentPackageName + ".puzzles";
		boolean noYearFilter = yearFilter.isEmpty();
		boolean noDayFilter = dayFilter.isEmpty();
		for (int year = MIN_YEAR; year <= MAX_YEAR; year++) {
			if (noYearFilter || yearFilter.contains(year)) {
				String yearPackageName = String.format("%s.year%04d", puzzlesPackageName, year);
				for (int day = MIN_DAY; day <= MAX_DAY; day++) {
					if (noDayFilter || dayFilter.contains(day)) {
						String dayClassName = String.format("%s.Day%02d", yearPackageName, day);
						Class<?> clazz;
						try {
							clazz = Class.forName(dayClassName);
						} catch (ClassNotFoundException ex) {
							clazz = null;
						}
						if (clazz != null && IPuzzle.class.isAssignableFrom(clazz)) {
							puzzleRunners.add(new PuzzleRunner(year, day, clazz.asSubclass(IPuzzle.class)));
						}
					}
				}
			}
		}
		int puzzleRunnerCount = puzzleRunners.size();
		if (puzzleRunnerCount == 0) {
			System.err.println("No matching puzzles found.");
		}
		else {
			PrintWriter consoleOutWriter;
			Console console = System.console();
			boolean restrictedCharacterSet;
			if (console == null) {
				consoleOutWriter = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));
				restrictedCharacterSet = true;
			}
			else {
				consoleOutWriter = console.writer();
				restrictedCharacterSet = false;
			}
			for (PuzzleRunner puzzleRunner : puzzleRunners) {
				puzzleRunner.run(consoleOutWriter, restrictedCharacterSet, userFilter, preRunForJIT);
			}
		}
	}
}
