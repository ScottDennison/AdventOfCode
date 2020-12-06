package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day04 {
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

	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		fileLines.add(""); // Add a blank line to the end to prevent having to do any special end-of-file handling.
		Map<String, FieldDefinition> fieldDefinitionsByKey = Stream.of(FIELD_DEFINITIONS).collect(Collectors.toMap(FieldDefinition::getKey, Function.identity()));
		Set<String> requiredFieldKeys = Stream.of(FIELD_DEFINITIONS).filter(fieldDefinition -> !fieldDefinition.isOptional()).map(FieldDefinition::getKey).collect(Collectors.toSet());
		Set<String> foundFieldKeys = new HashSet<>();
		boolean isPassportDataValid = true;
		int validPassportsWithoutDataChecks = 0;
		int validPassportsWithDataChecks = 0;
		int passportNumber = 1;
		for (String fileLine : fileLines) {
			String trimmedFileLine = fileLine.trim();
			if (trimmedFileLine.isEmpty()) {
				Set<String> missingFieldKeys = new HashSet<>(requiredFieldKeys);
				missingFieldKeys.removeAll(foundFieldKeys);
				if (missingFieldKeys.isEmpty()) {
					validPassportsWithoutDataChecks++;
					if (isPassportDataValid) {
						validPassportsWithDataChecks++;
					}
				}
				else if (LOG) {
					System.out.format("Passport %d has missing fields: %s%n", passportNumber, missingFieldKeys);
				}
				// Reset
				isPassportDataValid = true;
				passportNumber++;
				foundFieldKeys.clear();
			}
			else {
				String[] fields = trimmedFileLine.split(" ");
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
									System.out.format("Passport %d has unexpected field: %s%n", passportNumber, key);
								}
								isPassportDataValid = false;
							}
							else if (!fieldDefinition.getValidator().isValid(value)) {
								if (LOG) {
									System.out.format("Passport %d has invalid value for field %s: %s%n", passportNumber, key, value);
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
		System.out.format("Valid passports without data check: %d%n", validPassportsWithoutDataChecks);
		System.out.format("Valid passports with    data check: %d%n", validPassportsWithDataChecks);
	}
}
