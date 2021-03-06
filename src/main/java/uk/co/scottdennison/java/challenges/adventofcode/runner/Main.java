package uk.co.scottdennison.java.challenges.adventofcode.runner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.output.DisplayWriter;
import uk.co.scottdennison.java.libs.text.output.table.DisplayTextualTableBuilder;
import uk.co.scottdennison.java.libs.text.output.table.DisplayTextualTableBuilder.Alignment;
import uk.co.scottdennison.java.libs.text.output.table.DisplayTextualTableBuilder.CharacterSet;

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
import java.util.List;
import java.util.Objects;

public class Main {
	private static final int MIN_YEAR = 2015;
	private static final int MAX_YEAR = 2020;
	private static final int MIN_DAY = 1;
	private static final int MAX_DAY = 25;

	public static class CommandLineBean {
		@Option(name = "-y", aliases = "--year", usage = "only run puzzles from a specified year")
		private Integer year;

		@Option(name = "-d", aliases = "--day", usage = "only run puzzles from a specified year")
		private Integer day;

		@Option(name = "-u", aliases = "--user", usage = "only run puzzle data sets for user u")
		private Integer user;

		public Integer getYear() {
			return this.year;
		}

		public Integer getDay() {
			return this.day;
		}

		public Integer getUser() {
			return this.user;
		}
	}

	private static class PuzzleRunResults {
		private final String dataSetName;
		private final long nanoseconds;
		private final PuzzleRunPartResults partAResults;
		private final PuzzleRunPartResults partBResults;

		public PuzzleRunResults(String dataSetName, long nanoseconds, PuzzleRunPartResults partAResults, PuzzleRunPartResults partBResults) {
			this.dataSetName = dataSetName;
			this.nanoseconds = nanoseconds;
			this.partAResults = partAResults;
			this.partBResults = partBResults;
		}

		public String getDataSetName() {
			return this.dataSetName;
		}

		public long getNanoseconds() {
			return this.nanoseconds;
		}

		public PuzzleRunPartResults getPartAResults() {
			return this.partAResults;
		}

		public PuzzleRunPartResults getPartBResults() {
			return this.partBResults;
		}
	}

	private static class PuzzleRunPartResults {
		private final String expectedResult;
		private final String actualResult;

		public PuzzleRunPartResults(String expectedResult, String actualResult) {
			this.expectedResult = expectedResult;
			this.actualResult = actualResult;
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

		public void run(PrintWriter consoleWriter, boolean restrictedCharacterSet, Integer userFilter) {
			Path dataPath = Paths.get(String.format("data/year%04d/day%02d/io", this.year, this.day));
			consoleWriter.format("Running year %d day %d%n", this.year, this.day);
			List<PuzzleRunResults> puzzleRunResults = new ArrayList<>();
			if (userFilter == null) {
				puzzleRunResults.addAll(runWithDataSets(consoleWriter, dataPath, "Example", "examples", null));
			}
			puzzleRunResults.addAll(runWithDataSets(consoleWriter, dataPath, "User", "users", userFilter == null ? null : Integer.toString(userFilter)));
			DisplayTextualTableBuilder displayTextualTableBuilder = new DisplayTextualTableBuilder();
			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			for (PuzzleRunResults puzzleRunResultsEntry : puzzleRunResults) {
				displayTextualTableBuilder.addRow(true);
				displayTextualTableBuilder.addEntry("Data Set", puzzleRunResultsEntry.getDataSetName(), Alignment.LEFT);
				displayTextualTableBuilder.addEntry("Time taken (ns)", numberFormat.format(puzzleRunResultsEntry.getNanoseconds()), Alignment.RIGHT);
				addPuzzleRunPartResultsToTable(displayTextualTableBuilder, "A", puzzleRunResultsEntry.getPartAResults());
				addPuzzleRunPartResultsToTable(displayTextualTableBuilder, "B", puzzleRunResultsEntry.getPartBResults());
			}
			consoleWriter.write(displayTextualTableBuilder.build("\t", restrictedCharacterSet ? CharacterSet.ASCII : CharacterSet.BASIC_BOX_DRAWING_DOUBLE_WIDTH_OUTER, true, null));
			consoleWriter.flush();
		}

		private void addPuzzleRunPartResultsToTable(DisplayTextualTableBuilder displayTextualTableBuilder, String partCode, PuzzleRunPartResults partResults) {
			boolean hasExpectedResult = partResults.hasExpectedResult();
			String expectedResult;
			String actualResult = partResults.getActualResult();
			String resultState;
			if (hasExpectedResult) {
				expectedResult = partResults.getExpectedResult();
				if (Objects.equals(actualResult, expectedResult)) {
					resultState = "\u001B[32mSUCCESS\u001B[0m";
				}
				else {
					resultState = "\u001B[31mFAILURE\u001B[0m";
				}
			}
			else {
				expectedResult = "";
				resultState = "\u001B[33mUNKNOWN\u001B[0m";
			}
			displayTextualTableBuilder.addEntry("Part " + partCode + " - Expected Answer", expectedResult, Alignment.RIGHT);
			displayTextualTableBuilder.addEntry("Part " + partCode + " - Actual Answer", actualResult, Alignment.RIGHT);
			displayTextualTableBuilder.addEntry("Part " + partCode + " - State", resultState, Alignment.CENTER);
		}

		private List<PuzzleRunResults> runWithDataSets(PrintWriter consoleWriter, Path dataPath, String dataSetsName, String dataSetsFolderName, String filter) {
			Path dataSetsPath = dataPath.resolve(dataSetsFolderName);
			List<Path> dataSetPaths = new ArrayList<>();
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dataSetsPath, Files::isDirectory)) {
				for (Path path : directoryStream) {
					dataSetPaths.add(path);
				}
			} catch (IOException ex) {
				throw new IllegalStateException("Unable to iterate directory " + dataSetsPath);
			}
			List<PuzzleRunResults> puzzleRunResults = new ArrayList<>();
			for (Path dataSetPath : dataSetPaths) {
				String dataSetPartialName = dataSetPath.getFileName().toString();
				if (filter == null || filter.equalsIgnoreCase(dataSetPartialName)) {
					puzzleRunResults.add(runWithDataSet(consoleWriter, dataPath, dataSetPath, dataSetsName + " " + dataSetPartialName));
				}
			}
			return puzzleRunResults;
		}

		private PuzzleRunResults runWithDataSet(PrintWriter consoleWriter, Path dataPath, Path dataSetPath, String dataSetName) {
			consoleWriter.format("\tRunning with data set %s%n", dataSetName);
			consoleWriter.flush();
			char[] inputCharacters = readDataSetFile(dataSetPath, "input.txt", false);
			char[] outputACharacters = readDataSetFile(dataSetPath, "output_a.txt", true);
			char[] outputBCharacters = readDataSetFile(dataSetPath, "output_b.txt", true);
			IPuzzleConfigProvider puzzleConfigProvider = configName -> {
				String configFileName = "config_" + configName + ".txt";
				Path directoryPath = dataSetPath;
				while (true) {
					Path filePath = directoryPath.resolve(configFileName);
					if (Files.isRegularFile(filePath)) {
						return readFile(filePath);
					}
					if (directoryPath.equals(dataPath)) {
						throw new IllegalStateException("No such config found.");
					}
					directoryPath = directoryPath.getParent();
				}
			};
			PrintWriter displayPrinterWriter = new PrintWriter(new DisplayWriter(consoleWriter, "----------------------------------------", "\t\t", false));
			boolean partBPotentiallyUnsolvable = outputBCharacters == null;
			long startTime = System.nanoTime();
			IPuzzle puzzleInstance = this.createPuzzleInstance();
			IPuzzleResults puzzleResults = puzzleInstance.runPuzzle(inputCharacters, puzzleConfigProvider, partBPotentiallyUnsolvable, displayPrinterWriter);
			long finishTime = System.nanoTime();
			displayPrinterWriter.close();
			return new PuzzleRunResults(
				dataSetName,
				finishTime - startTime,
				createPartResults(outputACharacters, puzzleResults.getPartAAnswerString()),
				createPartResults(outputBCharacters, puzzleResults.getPartBAnswerString())
			);
		}

		private static PuzzleRunPartResults createPartResults(char[] expectedResult, String actualResult) {
			String expectedResultTrimmedString;
			String actualResultTrimmedString;
			if (expectedResult == null) {
				expectedResultTrimmedString = null;
			}
			else {
				expectedResultTrimmedString = new String(expectedResult).trim();
			}
			if (actualResult == null) {
				actualResultTrimmedString = null;
			}
			else {
				actualResultTrimmedString = actualResult.trim();
			}
			return new PuzzleRunPartResults(expectedResultTrimmedString, actualResultTrimmedString);
		}

		private IPuzzle createPuzzleInstance() {
			try {
				return this.puzzleClazz.newInstance();
			} catch (IllegalAccessException | InstantiationException ex) {
				throw new IllegalStateException("Unable to instantiate puzzle class", ex);
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
		runPuzzles(commandLineBean.getYear(), commandLineBean.getDay(), commandLineBean.getUser());
	}

	private static void runPuzzles(Integer yearFilter, Integer dayFilter, Integer userFilter) {
		List<PuzzleRunner> puzzleRunners = new ArrayList<>();
		String mainClassName = Main.class.getName();
		String mainPackageName = mainClassName.substring(0, mainClassName.lastIndexOf('.'));
		String mainParentPackageName = mainPackageName.substring(0, mainPackageName.lastIndexOf('.'));
		String puzzlesPackageName = mainParentPackageName + ".puzzles";
		for (int year = MIN_YEAR; year <= MAX_YEAR; year++) {
			if (yearFilter == null || year == yearFilter) {
				String yearPackageName = String.format("%s.year%04d", puzzlesPackageName, year);
				for (int day = MIN_DAY; day <= MAX_DAY; day++) {
					if (dayFilter == null || day == dayFilter) {
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
				puzzleRunner.run(consoleOutWriter, restrictedCharacterSet, userFilter);
			}
		}
	}
}
