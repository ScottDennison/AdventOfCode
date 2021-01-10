package uk.co.scottdennison.java.challenges.adventofcode.runner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
	private static final int MIN_YEAR = 2015;
	private static final int MAX_YEAR = 2020;
	private static final int MIN_DAY = 1;
	private static final int MAX_DAY = 25;

	private static final String DIVIDER = "----------------------------------------";

	public static class CommandLineBean {
		@Option(name="-y",aliases="--year",usage="only run puzzles from a specified year")
		private Integer year;

		@Option(name="-d",aliases="--day",usage="only run puzzles from a specified year")
		private Integer day;

		public Integer getYear() {
			return this.year;
		}

		public Integer getDay() {
			return this.day;
		}
	}

	private static class NoopOutputStream extends OutputStream {
		@Override
		public void write(int b) {
			// Do nothing.
		}

		@Override
		public void write(byte[] b) {
			// Do nothing.
		}

		@Override
		public void write(byte[] b, int off, int len) {
			// Do nothing.
		}

		@Override
		public void flush() {
			// Do nothing.
		}

		@Override
		public void close() {
			// Do nothing.
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

		public void run(boolean verbose) {
			Path dataPath = Paths.get(String.format("data/year%04d/day%02d/io",this.year,this.day));
			runWithDataSets(dataPath,"Example","examples",verbose);
			runWithDataSets(dataPath,"User","users",verbose);
		}

		private void runWithDataSets(Path dataPath, String dataSetsName, String dataSetsFolderName, boolean verbose) {
			Path dataSetsPath = dataPath.resolve(dataSetsFolderName);
			List<Path> dataSetPaths = new ArrayList<>();
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dataSetsPath,Files::isDirectory)) {
				for (Path path : directoryStream) {
					dataSetPaths.add(path);
				}
			} catch (IOException ex) {
				throw new IllegalStateException("Unable to iterate directory " + dataSetsPath);
			}
			for (Path dataSetPath : dataSetPaths) {
				runWithDataSet(dataPath, dataSetPath, dataSetsName + " " + dataSetPath.getFileName().toString(), verbose);
			}
		}

		private void runWithDataSet(Path dataPath, Path dataSetPath, String dataSetName, boolean verbose) {
			char[] inputCharacters = readDataSetFile(dataSetPath,"input.txt",false);
			char[] outputACharacters = readDataSetFile(dataSetPath,"output_a.txt",true);
			char[] outputBCharacters = readDataSetFile(dataSetPath,"output_b.txt",true);
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
			PrintWriter progressWriter;
			if (verbose) {
				progressWriter = new PrintWriter(new OutputStreamWriter(System.out,StandardCharsets.UTF_8));
			} else {
				progressWriter = new PrintWriter(new NoopOutputStream());
			}
			System.out.format("Running year %04d day %02d with data set %s%n",this.year,this.day,dataSetName);
			if (verbose) {
				System.out.println(DIVIDER);
			}
			boolean partBPotentiallyUnsolvable = outputBCharacters == null;
			long startTime = System.nanoTime();
			IPuzzle puzzleInstance = this.createPuzzleInstance();
			IPuzzleResults puzzleResults = puzzleInstance.runPuzzle(inputCharacters,puzzleConfigProvider,partBPotentiallyUnsolvable,progressWriter);
			long finishTime = System.nanoTime();
			if (verbose) {
				System.out.println(puzzleResults.getPartASummary());
				System.out.println(puzzleResults.getPartBSummary());
				System.out.println(DIVIDER);
			}
			System.out.format("Took %d nanoseconds%n",finishTime-startTime);
			outputResults("A",puzzleResults.getPartAAnswerString(),outputACharacters);
			outputResults("B",puzzleResults.getPartBAnswerString(),outputBCharacters);
			System.out.println();
			System.out.println();
		}

		private IPuzzle createPuzzleInstance() {
			try {
				return this.puzzleClazz.newInstance();
			} catch (IllegalAccessException | InstantiationException ex) {
				throw new IllegalStateException("Unable to instantiate puzzle class",ex);
			}
		}

		private static void outputResults(String partName, String actualResult, char[] expectedResult) {
			System.out.format("Result for part %s: \"%s\"", partName, actualResult);
			if (expectedResult == null) {
				System.out.println(" (No expected result)");
			} else {
				String expectedResultString = new String(expectedResult).trim();
				System.out.format(" (Expected result \"%s\") - ", expectedResultString);
				if (expectedResultString.equals(actualResult)) {
					System.out.println("PASSED");
				} else {
					System.out.println("FAILED");
				}
			}
		}

		private static char[] readDataSetFile(Path dataSetPath, String fileName, boolean allowMissing) {
			Path path = dataSetPath.resolve(fileName);
			if (Files.isRegularFile(path)) {
				return readFile(path);
			} else if (allowMissing) {
				Path missingPath = dataSetPath.resolve(fileName+".missing");
				if (Files.isRegularFile(missingPath)) {
					char[] fileContents = readFile(missingPath);
					if (fileContents.length == 0) {
						return null;
					}
				}
			}
			throw new IllegalStateException("Could not read file " + path + (allowMissing?"":" or an equivalent .missing file"));
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
			} else {
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

		List<PuzzleRunner> puzzleRunners = new ArrayList<>();
		String mainClassName = Main.class.getName();
		String mainPackageName = mainClassName.substring(0,mainClassName.lastIndexOf('.'));
		String mainParentPackageName = mainPackageName.substring(0,mainPackageName.lastIndexOf('.'));
		String puzzlesPackageName = mainParentPackageName+".puzzles";
		Integer yearFilter = commandLineBean.getYear();
		Integer dayFilter = commandLineBean.getDay();
		for (int year=MIN_YEAR; year<=MAX_YEAR; year++) {
			if (yearFilter == null || year == yearFilter) {
				String yearPackageName = String.format("%s.year%04d",puzzlesPackageName,year);
				for (int day = MIN_DAY; day <= MAX_DAY; day++) {
					if (dayFilter == null || day == dayFilter) {
						String dayClassName = String.format("%s.Day%02d",yearPackageName,day);
						Class<?> clazz;
						try {
							clazz = Class.forName(dayClassName);
						} catch (ClassNotFoundException ex) {
							clazz = null;
						}
						if (clazz != null && IPuzzle.class.isAssignableFrom(clazz)) {
							puzzleRunners.add(new PuzzleRunner(year,day,clazz.asSubclass(IPuzzle.class)));
						}
					}
				}
			}
		}
		int puzzleRunnerCount = puzzleRunners.size();
		if (puzzleRunnerCount == 0) {
			System.err.println("No matching puzzles found.");
		} else {
			boolean verbose = puzzleRunnerCount == 1;
			for (PuzzleRunner puzzleRunner : puzzleRunners) {
				puzzleRunner.run(verbose);
			}
		}
	}
}
