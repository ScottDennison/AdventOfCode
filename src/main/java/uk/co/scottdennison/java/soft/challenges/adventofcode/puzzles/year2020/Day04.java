package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day04 implements IPuzzle {
	private static final boolean LOG = false;

	private static class BasicNumberValidator implements Validator {
		private static final Pattern PATTERN = Pattern.compile("^[0-9]+$");

		private final int minimum;
		private final int maximum;

		public BasicNumberValidator(int minimum, int maximum) {
			this.minimum = minimum;
			this.maximum = maximum;
		}

		@Override
		public boolean isValid(String data) {
			if (PATTERN.matcher(data).matches()) {
				try {
					int value = Integer.parseInt(data);
					return (value >= minimum && value <= maximum);
				} catch (NumberFormatException ex) {
					return false;
				}
			}
			return false;
		}
	}

	private static class HeightValidator implements Validator {
		private enum HeightType {
			CENTIMETERS("cm", 150, 193),
			INCHES("in", 59, 76);

			private final String ending;
			private final Validator subValidator;

			HeightType(String ending, int minimum, int maximum) {
				this.ending = ending;
				this.subValidator = new BasicNumberValidator(minimum, maximum);
			}

			public String getEnding() {
				return this.ending;
			}

			public Validator getSubValidator() {
				return this.subValidator;
			}
		}

		@Override
		public boolean isValid(String data) {
			for (HeightType heightType : HeightType.values()) {
				String ending = heightType.getEnding();
				if (data.endsWith(ending) && heightType.getSubValidator().isValid(data.substring(0, data.length() - ending.length()))) {
					return true;
				}
			}
			return false;
		}
	}

	private static class ListValidator implements Validator {
		private final Set<String> validEntries;

		public ListValidator(String... validEntries) {
			this.validEntries = new HashSet<>(Arrays.asList(validEntries));
		}

		@Override
		public boolean isValid(String data) {
			return this.validEntries.contains(data);
		}
	}

	private static class PatternValidator implements Validator {
		private final Pattern pattern;

		public PatternValidator(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean isValid(String data) {
			return this.pattern.matcher(data).matches();
		}
	}

	private static class AlwaysValidValidator implements Validator {
		@Override
		public boolean isValid(String data) {
			return true;
		}
	}

	private interface Validator {
		boolean isValid(String data);
	}

	private static class FieldDefinition {
		private final String key;
		private final boolean optional;
		private final Validator validator;

		public FieldDefinition(String key, boolean optional, Validator validator) {
			this.key = key;
			this.optional = optional;
			this.validator = validator;
		}

		public String getKey() {
			return this.key;
		}

		public boolean isOptional() {
			return this.optional;
		}

		public Validator getValidator() {
			return this.validator;
		}
	}

	private static final FieldDefinition[] FIELD_DEFINITIONS = {
		new FieldDefinition("byr", false, new BasicNumberValidator(1920, 2002)),
		new FieldDefinition("iyr", false, new BasicNumberValidator(2010, 2020)),
		new FieldDefinition("eyr", false, new BasicNumberValidator(2020, 2030)),
		new FieldDefinition("hgt", false, new HeightValidator()),
		new FieldDefinition("hcl", false, new PatternValidator(Pattern.compile("^#[0-9a-f]{6}$"))),
		new FieldDefinition("ecl", false, new ListValidator("amb", "blu", "brn", "gry", "grn", "hzl", "oth")),
		new FieldDefinition("pid", false, new PatternValidator(Pattern.compile("^[0-9]{9}$"))),
		new FieldDefinition("cid", true, new AlwaysValidValidator())
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		inputLines.add(""); // Add a blank line to the end to prevent having to do any special end-of-input handling.
		Map<String, FieldDefinition> fieldDefinitionsByKey = Stream.of(FIELD_DEFINITIONS).collect(Collectors.toMap(FieldDefinition::getKey, Function.identity()));
		Set<String> requiredFieldKeys = Stream.of(FIELD_DEFINITIONS).filter(fieldDefinition -> !fieldDefinition.isOptional()).map(FieldDefinition::getKey).collect(Collectors.toSet());
		Set<String> foundFieldKeys = new HashSet<>();
		boolean isPassportDataValid = true;
		int validPassportsWithoutDataChecks = 0;
		int validPassportsWithDataChecks = 0;
		int passportNumber = 1;
		for (String inputLine : inputLines) {
			String trimmedInputLine = inputLine.trim();
			if (trimmedInputLine.isEmpty()) {
				Set<String> missingFieldKeys = new HashSet<>(requiredFieldKeys);
				missingFieldKeys.removeAll(foundFieldKeys);
				if (missingFieldKeys.isEmpty()) {
					validPassportsWithoutDataChecks++;
					if (isPassportDataValid) {
						validPassportsWithDataChecks++;
					}
				}
				else if (LOG) {
					printWriter.format("Passport %d has missing fields: %s%n", passportNumber, missingFieldKeys);
					printWriter.flush();
				}
				// Reset
				isPassportDataValid = true;
				passportNumber++;
				foundFieldKeys.clear();
			}
			else {
				String[] fields = trimmedInputLine.split(" ");
				for (String field : fields) {
					String[] fieldParts = field.split(":");
					if (fieldParts.length == 2) {
						String key = fieldParts[0].trim();
						foundFieldKeys.add(key);
						if (isPassportDataValid || LOG) {
							String value = fieldParts[1].trim();
							FieldDefinition fieldDefinition = fieldDefinitionsByKey.get(key);
							if (fieldDefinition == null) {
								if (LOG) {
									printWriter.format("Passport %d has unexpected field: %s%n", passportNumber, key);
									printWriter.flush();
								}
								isPassportDataValid = false;
							}
							else if (!fieldDefinition.getValidator().isValid(value)) {
								if (LOG) {
									printWriter.format("Passport %d has invalid value for field %s: %s%n", passportNumber, key, value);
									printWriter.flush();
								}
								isPassportDataValid = false;
							}
						}
					}
					else {
						throw new IllegalStateException("Invalid field entry");
					}
				}
			}
		}
		return new BasicPuzzleResults<>(
			validPassportsWithoutDataChecks,
			validPassportsWithDataChecks
		);
	}
}
