package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day11 implements IPuzzle {
	private static final String[] ORDINALS = {
		"zeroth",
		"first",
		"second",
		"third",
		"fourth",
		"fifth",
		"sixth",
		"seventh",
		"eighth",
		"ninth",
		"tenth",
		"eleventh",
		"twelfth",
		"thirteenth",
		"fourteenth",
		"fifteenth",
		"sixteenth",
		"seventeenth",
		"eighteenth",
		"nineteenth",
		"twentieth"
	};

	private enum DeviceType {
		GENERATOR,
		MICROCHIP
	}

	private static final class Device {
		private final String element;
		private final DeviceType deviceType;
		private transient int hashCode;

		private Device(String element, DeviceType deviceType) {
			this.element = element;
			this.deviceType = deviceType;
		}

		public static Device createNew(String element, DeviceType deviceType) {
			if (element == null) {
				throw new IllegalStateException("Element is NULL");
			}
			else if (deviceType == null) {
				throw new IllegalStateException("Device type is NULL");
			}
			return new Device(element, deviceType);
		}

		public String getElement() {
			return this.element;
		}

		public DeviceType getDeviceType() {
			return this.deviceType;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != Device.class) {
				return false;
			}
			Device otherDevice = (Device) otherObject;
			return Objects.equals(this.element, otherDevice.element) && this.deviceType == otherDevice.deviceType;
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = 31 * hashCode + this.element.hashCode();
				hashCode = 31 * hashCode + this.deviceType.hashCode();
				this.hashCode = hashCode;
			}
			return hashCode;
		}
	}

	private static final class RadioisotopeTestingFacility {
		private final Map<Device, Integer> deviceIndexLookup;
		private final Device[] devices;
		private final byte[] deviceFloorNumbers;
		private final byte elevatorFloor;
		private transient int hashCode;

		private RadioisotopeTestingFacility(Map<Device, Integer> deviceIndexLookup, Device[] devices, byte[] deviceFloorNumbers, byte elevatorFloorNumber) {
			this.deviceIndexLookup = deviceIndexLookup;
			this.devices = devices;
			this.deviceFloorNumbers = deviceFloorNumbers;
			this.elevatorFloor = elevatorFloorNumber;
		}

		public static RadioisotopeTestingFacility createNew(Map<Device, Integer> deviceToFloorNumberMap, int elevatorFloorNumber) {
			if (deviceToFloorNumberMap == null) {
				throw new IllegalStateException("Device to floor map is NULL");
			}
			if (elevatorFloorNumber < 0 || elevatorFloorNumber > Byte.MAX_VALUE) {
				throw new IllegalStateException("Elevator floor not in storable range");
			}
			Map<Device, Integer> deviceLookup = new HashMap<>();
			int deviceCount = deviceToFloorNumberMap.size();
			Device[] devices = new Device[deviceCount];
			byte[] deviceFloorNumbers = new byte[deviceCount];
			int deviceIndex = 0;
			for (Map.Entry<Device, Integer> deviceEntry : deviceToFloorNumberMap.entrySet()) {
				Device device = deviceEntry.getKey();
				if (device == null) {
					throw new IllegalStateException("Device is NULL");
				}
				devices[deviceIndex] = device;
				deviceLookup.put(deviceEntry.getKey(), deviceIndex);
				Integer deviceFloorIntBoxed = deviceEntry.getValue();
				if (deviceFloorIntBoxed == null) {
					throw new IllegalStateException("Device floor is NULL");
				}
				int deviceFloorInt = deviceFloorIntBoxed;
				if (deviceFloorInt < 0 || deviceFloorInt > Byte.MAX_VALUE) {
					throw new IllegalStateException("Device floor not in storable range");
				}
				deviceFloorNumbers[deviceIndex] = (byte) deviceFloorInt;
				deviceIndex++;
			}
			return new RadioisotopeTestingFacility(deviceLookup, devices, deviceFloorNumbers, (byte) elevatorFloorNumber);
		}

		public RadioisotopeTestingFacility createNewWithDeviceMovedBetweenFloors(Device device, int fromFloorNumber, int toFloorNumber) {
			Integer deviceIndexBoxed = this.deviceIndexLookup.get(device);
			if (deviceIndexBoxed == null) {
				throw new IllegalStateException("Device not known by factory");
			}
			int deviceIndex = deviceIndexBoxed;
			if (this.deviceFloorNumbers[deviceIndex] != fromFloorNumber) {
				throw new IllegalStateException("Device not on specified floor");
			}
			if (toFloorNumber < 0 || toFloorNumber > Byte.MAX_VALUE) {
				throw new IllegalStateException("To floor not in storable range");
			}
			byte[] newDeviceFloorNumbers = Arrays.copyOf(this.deviceFloorNumbers, this.deviceFloorNumbers.length);
			newDeviceFloorNumbers[deviceIndex] = (byte) toFloorNumber;
			return new RadioisotopeTestingFacility(this.deviceIndexLookup, this.devices, newDeviceFloorNumbers, this.elevatorFloor);
		}

		public RadioisotopeTestingFacility createNewWithNewElevatorFloor(int floorNumber) {
			if (floorNumber < 0 || floorNumber > Byte.MAX_VALUE) {
				throw new IllegalStateException("Floor not in storable range");
			}
			return new RadioisotopeTestingFacility(this.deviceIndexLookup, this.devices, this.deviceFloorNumbers, (byte) floorNumber);
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != RadioisotopeTestingFacility.class) {
				return false;
			}
			RadioisotopeTestingFacility otherRadioisotopeTestingFacility = (RadioisotopeTestingFacility) otherObject;
			return
				this.elevatorFloor == otherRadioisotopeTestingFacility.elevatorFloor
					&&
					this.deviceIndexLookup == otherRadioisotopeTestingFacility.deviceIndexLookup // Same instance
					&&
					Arrays.equals(this.deviceFloorNumbers, otherRadioisotopeTestingFacility.deviceFloorNumbers);
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = 31 * hashCode + System.identityHashCode(this.deviceIndexLookup);
				hashCode = 31 * hashCode + this.elevatorFloor;
				hashCode = 31 * hashCode + Arrays.hashCode(this.deviceFloorNumbers);
				this.hashCode = hashCode;
			}
			return hashCode;
		}

		public int getDeviceCountForFloor(int floorNumber) {
			if (floorNumber < 0 || floorNumber > Byte.MAX_VALUE) {
				throw new IllegalStateException("Floor not in storable range");
			}
			byte floorNumberByte = (byte) floorNumber;
			int matchingDeviceCount = 0;
			for (byte deviceFloorNumber : this.deviceFloorNumbers) {
				if (deviceFloorNumber == floorNumberByte) {
					matchingDeviceCount++;
				}
			}
			return matchingDeviceCount;
		}

		public Iterator<Device> iterateDevicesOnFloor(int floorNumber) {
			if (floorNumber < 0 || floorNumber > Byte.MAX_VALUE) {
				throw new IllegalStateException("Floor not in storable range");
			}
			byte floorNumberByte = (byte) floorNumber;
			return new Iterator<Device>() {
				private int deviceIndex = -1;

				@Override
				public boolean hasNext() {
					if (this.deviceIndex < -1) {
						return false;
					}
					while (true) {
						if (++this.deviceIndex >= RadioisotopeTestingFacility.this.deviceFloorNumbers.length) {
							this.deviceIndex = -2;
							return false;
						}
						if (RadioisotopeTestingFacility.this.deviceFloorNumbers[this.deviceIndex] == floorNumberByte) {
							return true;
						}
					}
				}

				@Override
				public Device next() {
					return RadioisotopeTestingFacility.this.devices[this.deviceIndex];
				}
			};
		}

		public boolean isValid() {
			Set<Byte> floors = new HashSet<>();
			Map<Byte, Set<String>> microchipElementsPerFloor = new HashMap<>();
			Map<Byte, Set<String>> generatorElementsPerFloor = new HashMap<>();
			int deviceCount = this.devices.length;
			for (int deviceIndex = 0; deviceIndex < deviceCount; deviceIndex++) {
				Byte floor = this.deviceFloorNumbers[deviceIndex];
				if (floors.add(floor)) {
					microchipElementsPerFloor.put(floor, new HashSet<>());
					generatorElementsPerFloor.put(floor, new HashSet<>());
				}
				Device device = this.devices[deviceIndex];
				String deviceElement = device.getElement();
				switch (device.getDeviceType()) {
					case MICROCHIP:
						microchipElementsPerFloor.get(floor).add(deviceElement);
						break;
					case GENERATOR:
						generatorElementsPerFloor.get(floor).add(deviceElement);
						break;
					default:
						throw new IllegalStateException("Unexpected device type.");
				}
			}
			for (Byte floor : floors) {
				Set<String> microchipElementsForFloor = microchipElementsPerFloor.get(floor);
				Set<String> generatorElementsForFloor = generatorElementsPerFloor.get(floor);
				if (!microchipElementsForFloor.isEmpty() && !generatorElementsForFloor.isEmpty()) {
					for (String microchipElement : microchipElementsForFloor) {
						if (!generatorElementsForFloor.contains(microchipElement)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		public int getElevatorFloor() {
			return this.elevatorFloor;
		}
	}

	private static final Pattern PATTERN_LINE = Pattern.compile("The (?<floorOrdinal>[a-z]+) floor contains (?:nothing relevant)?(?<devices>.*?)\\.$");
	private static final Pattern PATTERN_DEVICES_SPLIT = Pattern.compile("(?: *, *and +)|(?: +and +)|(?: *, *)");
	private static final Pattern PATTERN_DEVICE = Pattern.compile("^a (?<element>[a-z]+)(?:-compatible)? (?<deviceType>[a-z]+)$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		if (Runtime.getRuntime().maxMemory() < (3L * 1024L * 1024L * 1024L)) {
			printWriter.write("WARNING: The implementation of this problem is NOT memory efficient, and it is recommended to use >=3GB of java heap to prevent excessive GC overhead");
			printWriter.flush();
		}
		Map<String, Integer> ordinalValues = new HashMap<>();
		for (int index = 0; index < ORDINALS.length; index++) {
			if (ordinalValues.put(ORDINALS[index], index) != null) {
				throw new IllegalStateException("Duplicate ordinal.");
			}
		}
		Map<Device, Integer> deviceToFloorNumberMap = new HashMap<>();
		int minimumFloorNumber = Integer.MAX_VALUE;
		int maximumFloorNumber = Integer.MIN_VALUE;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher lineMatcher = PATTERN_LINE.matcher(inputLine);
			if (!lineMatcher.matches()) {
				throw new IllegalStateException("Unparseable line");
			}
			Integer floorNumber = ordinalValues.get(lineMatcher.group("floorOrdinal"));
			if (floorNumber == null) {
				throw new IllegalStateException("Unmatched floor number.");
			}
			int floorNumberPrimitive = floorNumber;
			minimumFloorNumber = Math.min(minimumFloorNumber, floorNumberPrimitive);
			maximumFloorNumber = Math.max(maximumFloorNumber, floorNumberPrimitive);
			String devicesString = lineMatcher.group("devices");
			if (!devicesString.isEmpty()) {
				for (String deviceString : PATTERN_DEVICES_SPLIT.split(devicesString)) {
					Matcher deviceMatcher = PATTERN_DEVICE.matcher(deviceString);
					if (!deviceMatcher.matches()) {
						throw new IllegalStateException("Unparseable device");
					}
					Device device = Device.createNew(deviceMatcher.group("element"), DeviceType.valueOf(deviceMatcher.group("deviceType").toUpperCase(Locale.ENGLISH)));
					if (deviceToFloorNumberMap.put(device, floorNumber) != null) {
						throw new IllegalStateException("Duplicate device.");
					}
				}
			}
		}
		if ((maximumFloorNumber - minimumFloorNumber + 1) < 2) {
			throw new IllegalStateException("Invalid number of floors");
		}
		int stepsRequiredForPartA = runSummaryWithWrappingPrint(deviceToFloorNumberMap, minimumFloorNumber, maximumFloorNumber, printWriter);
		printWriter.format("Adding new devices%n");
		printWriter.flush();
		deviceToFloorNumberMap.put(new Device("elerium", DeviceType.GENERATOR), minimumFloorNumber);
		deviceToFloorNumberMap.put(new Device("elerium", DeviceType.MICROCHIP), minimumFloorNumber);
		deviceToFloorNumberMap.put(new Device("dilithium", DeviceType.GENERATOR), minimumFloorNumber);
		deviceToFloorNumberMap.put(new Device("dilithium", DeviceType.MICROCHIP), minimumFloorNumber);
		int stepsRequiredForPartB = runSummaryWithWrappingPrint(deviceToFloorNumberMap, minimumFloorNumber, maximumFloorNumber, printWriter);
		return new BasicPuzzleResults<>(
			stepsRequiredForPartA,
			stepsRequiredForPartB
		);
	}

	private static int runSummaryWithWrappingPrint(Map<Device, Integer> deviceToFloorNumberMap, int minimumFloorNumber, int maximumFloorNumber, PrintWriter printWriter) {
		printWriter.format("Running simulation for %d devices%n", deviceToFloorNumberMap.size());
		printWriter.flush();
		int stepsRequired = runSimulation(deviceToFloorNumberMap, minimumFloorNumber, maximumFloorNumber, printWriter);
		printWriter.format("Total moves required: %d%n", stepsRequired);
		printWriter.flush();
		return stepsRequired;
	}

	private static int runSimulation(Map<Device, Integer> deviceToFloorNumberMap, int minimumFloorNumber, int maximumFloorNumber, PrintWriter printWriter) {
		RadioisotopeTestingFacility startingFacility = RadioisotopeTestingFacility.createNew(deviceToFloorNumberMap, 1);
		Set<RadioisotopeTestingFacility> seenFacilities = new HashSet<>();
		Set<RadioisotopeTestingFacility> pendingFacilities = Collections.singleton(startingFacility);
		int totalDeviceCount = deviceToFloorNumberMap.size();
		int stepsCompleted = 0;
		boolean solutionPending = true;
		while (solutionPending && !pendingFacilities.isEmpty()) {
			printWriter.format("Simulation in progress. Steps completed: %3d, Valid solutions seen: %9d, Solutions to investigate: %9d%n", stepsCompleted, seenFacilities.size(), pendingFacilities.size());
			printWriter.flush();
			Set<RadioisotopeTestingFacility> newPendingFacilities = new HashSet<>();
			for (RadioisotopeTestingFacility pendingFacility : pendingFacilities) {
				if (pendingFacility.isValid() && seenFacilities.add(pendingFacility)) {
					if (pendingFacility.getDeviceCountForFloor(maximumFloorNumber) == totalDeviceCount) {
						solutionPending = false;
						break;
					}
					int currentFloorNumber = pendingFacility.getElevatorFloor();
					if (currentFloorNumber > minimumFloorNumber) {
						int targetFloorNumber = currentFloorNumber - 1;
						if (pendingFacility.getDeviceCountForFloor(targetFloorNumber) > 0) {
							addPossibleMovesAfterMovingElevator(newPendingFacilities, pendingFacility, currentFloorNumber, targetFloorNumber, 1);
						}
					}
					if (currentFloorNumber < maximumFloorNumber) {
						addPossibleMovesAfterMovingElevator(newPendingFacilities, pendingFacility, currentFloorNumber, currentFloorNumber + 1, Math.min(2, pendingFacility.getDeviceCountForFloor(currentFloorNumber)));
					}
				}
			}
			pendingFacilities = newPendingFacilities;
			if (solutionPending) {
				stepsCompleted++;
			}
		}
		if (solutionPending) {
			throw new IllegalStateException("No solution.");
		}
		return stepsCompleted;
	}

	private static void addPossibleMovesAfterMovingElevator(Set<RadioisotopeTestingFacility> pendingFacilities, RadioisotopeTestingFacility currentFacility, int currentFloorNumber, int newFloorNumber, int allowedRecursion) {
		addPossibleMoves(pendingFacilities, currentFacility.createNewWithNewElevatorFloor(newFloorNumber), currentFloorNumber, newFloorNumber, new HashSet<>(), allowedRecursion);
	}

	private static void addPossibleMoves(Set<RadioisotopeTestingFacility> pendingFacilities, RadioisotopeTestingFacility currentFacility, int currentFloorNumber, int newFloorNumber, Set<Device> devicesLookedAt, int recursionsLeft) {
		int newRecursionsLeft = recursionsLeft - 1;
		HashSet<Device> newDevicesLookedAt = new HashSet<>(devicesLookedAt);
		Iterator<Device> deviceIterator = currentFacility.iterateDevicesOnFloor(currentFloorNumber);
		while (deviceIterator.hasNext()) {
			Device device = deviceIterator.next();
			if (newDevicesLookedAt.add(device)) {
				RadioisotopeTestingFacility newFacility = currentFacility.createNewWithDeviceMovedBetweenFloors(device, currentFloorNumber, newFloorNumber);
				if (newRecursionsLeft > 0) {
					addPossibleMoves(pendingFacilities, newFacility, currentFloorNumber, newFloorNumber, newDevicesLookedAt, newRecursionsLeft);
				}
				else {
					pendingFacilities.add(newFacility);
				}
			}
		}
	}
}
