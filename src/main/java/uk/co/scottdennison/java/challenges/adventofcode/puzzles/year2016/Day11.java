package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day11 {
	/**
	 The first floor contains a strontium generator, a strontium-compatible microchip, a plutonium generator, and a plutonium-compatible microchip.
	 The second floor contains a thulium generator, a ruthenium generator, a ruthenium-compatible microchip, a curium generator, and a curium-compatible microchip.
	 The third floor contains a thulium-compatible microchip.
	 The fourth floor contains nothing relevant.
	 */

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

	private static class ImmutableIterator<T> implements Iterator<T> {
		private final Iterator<T> iterator;

		private ImmutableIterator(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public T next() {
			return this.iterator.next();
		}

		@Override
		public void remove() {
			throw new IllegalStateException("Items are not allowed to be removed.");
		}

		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			this.iterator.forEachRemaining(action);
		}
	}

	private static boolean equalsByHashCodeFirst(Object left, Object right) {
		if (left.hashCode() == right.hashCode()) {
			return left.equals(right);
		} else {
			return false;
		}
	}

	private static boolean equalsByHashCodeFirstNullSafe(Object left, Object right) {
		if (left == null) {
			return right == null;
		} else if (right == null) {
			return false;
		} else if (left.hashCode() == right.hashCode()) {
			return left.equals(right);
		} else {
			return false;
		}
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
			Device otherDevice = (Device)otherObject;
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

	private static final class Floor {
		private final Set<Device> devices;
		private transient int hashCode;

		private Floor(Set<Device> devices) {
			this.devices = devices;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != Floor.class) {
				return false;
			}
			Floor otherFloor = (Floor)otherObject;
			return this.devices.equals(otherFloor.devices);
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = this.devices.hashCode();
			}
			return hashCode;
		}

		public boolean isValid() {
			if (devices.isEmpty()) {
				return true;
			}
			Set<String> microchipElements = new HashSet<>();
			Set<String> generatorElements = new HashSet<>();
			for (Device device : this.devices) {
				switch (device.getDeviceType()) {
					case GENERATOR:
						generatorElements.add(device.getElement());
						break;
					case MICROCHIP:
						microchipElements.add(device.getElement());
						break;
					default:
						throw new IllegalStateException("Unexpected device type");
				}
			}
			if (generatorElements.isEmpty() || microchipElements.isEmpty()) {
				return true;
			}
			for (String microchipElement : microchipElements) {
				if (!generatorElements.contains(microchipElement)) {
					return false;
				}
			}
			return true;
		}

		public Iterator<Device> iterateDevices() {
			return new ImmutableIterator<>(this.devices.iterator());
		}

		public static Floor createNew(Set<Device> devices) {
			return new Floor(new HashSet<>(devices));
		}

		public Floor createNewWithDeviceRemoved(Device device) {
			Set<Device> newDevices = new HashSet<>(this.devices);
			if (newDevices.remove(device)) {
				return new Floor(newDevices);
			} else {
				return this;
			}
		}

		public Floor createNewWithDeviceAdded(Device device) {
			Set<Device> newDevices = new HashSet<>(this.devices);
			if (newDevices.add(device)) {
				return new Floor(newDevices);
			} else {
				return this;
			}
		}

		public int getDeviceCount() {
			return this.devices.size();
		}
	}

	private static final class RadioisotopeTestingFacility {
		private final Floor[] floors;
		private final int elevatorFloor;
		private transient int hashCode;

		private RadioisotopeTestingFacility(Floor[] floors, int elevatorFloor) {
			this.floors = floors;
			this.elevatorFloor = elevatorFloor;
		}

		public static RadioisotopeTestingFacility createNew(Floor[] floors, int elevatorPosition) {
			return new RadioisotopeTestingFacility(Arrays.copyOf(floors, floors.length), elevatorPosition);
		}

		public RadioisotopeTestingFacility createNewWithDeviceMovedBetweenFloors(Device device, int fromFloorIndex, int toFloorIndex) {
			Floor existingFromFloor = this.floors[fromFloorIndex];
			Floor modifiedFromFloor = existingFromFloor.createNewWithDeviceRemoved(device);
			if (modifiedFromFloor != existingFromFloor) {
				Floor existingToFloor = this.floors[toFloorIndex];
				Floor modifiedToFloor = existingToFloor.createNewWithDeviceAdded(device);
				if (modifiedToFloor != existingToFloor) {
					Floor[] newFloors = Arrays.copyOf(this.floors, this.floors.length);
					newFloors[fromFloorIndex] = modifiedFromFloor;
					newFloors[toFloorIndex] = modifiedToFloor;
					return new RadioisotopeTestingFacility(newFloors, this.elevatorFloor);
				}
			}
			return this;
		}

		public RadioisotopeTestingFacility createNewWithNewElevatorFloor(int floorIndex) {
			return new RadioisotopeTestingFacility(this.floors, floorIndex);
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != RadioisotopeTestingFacility.class) {
				return false;
			}
			RadioisotopeTestingFacility otherRadioisotopeTestingFacility = (RadioisotopeTestingFacility)otherObject;
			if (this.elevatorFloor != otherRadioisotopeTestingFacility.elevatorFloor || this.floors.length != otherRadioisotopeTestingFacility.floors.length) {
				return false;
			}
			int floorsCount = floors.length;
			for (int floorIndex=0; floorIndex<floorsCount; floorIndex++) {
				if (!equalsByHashCodeFirst(this.floors[floorIndex],otherRadioisotopeTestingFacility.floors[floorIndex])) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = 31 * hashCode + this.elevatorFloor;
				hashCode = 31 * hashCode + Arrays.hashCode(this.floors);
			}
			return hashCode;
		}

		public Floor getFloor(int floorIndex) {
			return this.floors[floorIndex];
		}

		public boolean isValid() {
			for (Floor floor : this.floors) {
				if (!floor.isValid()) {
					return false;
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

	/*
	//BEGIN DEBUG
	private static Map<String, Device> deviceNamesUnorderedNameToDeviceMap;
	private static Map<Device, String> deviceNamesUnorderedDeviceToNameMap;
	private static List<String> deviceNamesOrderedList;
	private static Map<RadioisotopeTestingFacility, List<RadioisotopeTestingFacility>> chains = new HashMap<>();
	//END DEBUG
	*/

	public static void main(String[] args) throws IOException {
		Map<String, Integer> ordinalValues = new HashMap<>();
		for (int index=0; index< ORDINALS.length; index++) {
			if (ordinalValues.put(ORDINALS[index], index) != null) {
				throw new IllegalStateException("Duplicate ordinal.");
			}
		}
		Map<Integer,Floor> floorsMap = new HashMap<>();
		Set<Device> allDevices = new HashSet<>();
		int minimumFloorNumber = Integer.MAX_VALUE;
		int maximumFloorNumber = Integer.MIN_VALUE;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher lineMatcher = PATTERN_LINE.matcher(fileLine);
			if (!lineMatcher.matches()) {
				throw new IllegalStateException("Unparseable line");
			}
			Integer floorNumber = ordinalValues.get(lineMatcher.group("floorOrdinal"));
			if (floorNumber == null) {
				throw new IllegalStateException("Unmatched floor number.");
			}
			int floorNumberPrimitive = (int)floorNumber;
			minimumFloorNumber = Math.min(minimumFloorNumber, floorNumberPrimitive);
			maximumFloorNumber = Math.max(maximumFloorNumber, floorNumberPrimitive);
			Set<Device> devices = new HashSet<>();
			if (floorNumber == 1) {
				devices.add(new Device("elerium",DeviceType.GENERATOR));
				devices.add(new Device("elerium",DeviceType.MICROCHIP));
				devices.add(new Device("dilithium",DeviceType.GENERATOR));
				devices.add(new Device("dilithium",DeviceType.MICROCHIP));
			}
			String devicesString = lineMatcher.group("devices");
			if (!devicesString.isEmpty()) {
				for (String deviceString : PATTERN_DEVICES_SPLIT.split(devicesString)) {
					Matcher deviceMatcher = PATTERN_DEVICE.matcher(deviceString);
					if (!deviceMatcher.matches()) {
						throw new IllegalStateException("Unparseable device");
					}
					Device device = Device.createNew(deviceMatcher.group("element"), DeviceType.valueOf(deviceMatcher.group("deviceType").toUpperCase(Locale.ENGLISH)));
					if (!allDevices.add(device)) {
						throw new IllegalStateException("Duplicate device.");
					}
					devices.add(device);
				}
			}
			if (floorsMap.put(floorNumber, Floor.createNew(devices)) != null) {
				throw new IllegalStateException("Duplicate floor");
			}
		}
		/*
		// BEGIN DEBUG
		deviceNamesUnorderedNameToDeviceMap = new HashMap<>();
		deviceNamesUnorderedDeviceToNameMap = new HashMap<>();
		for (Device device : allDevices) {
			String deviceName = device.getElement().toUpperCase(Locale.ENGLISH).charAt(0) + "" + device.getDeviceType().name().charAt(0);
			deviceNamesUnorderedNameToDeviceMap.put(deviceName, device);
			deviceNamesUnorderedDeviceToNameMap.put(device, deviceName);

		}
		deviceNamesOrderedList = deviceNamesUnorderedNameToDeviceMap.keySet().stream().sorted().collect(Collectors.toList());
		// END DEBUG
		*/
		int floorsCount = maximumFloorNumber-minimumFloorNumber+1;
		if (floorsCount < 2) {
			throw new IllegalStateException("Invalid number of floors");
		}
		Floor[] floorsArray = new Floor[floorsCount];
		for (int floorNumber=minimumFloorNumber; floorNumber<=maximumFloorNumber; floorNumber++) {
			Floor floor = floorsMap.get(floorNumber);
			if (floor == null) {
				throw new IllegalStateException("Missing floors");
			}
			floorsArray[floorNumber-minimumFloorNumber] = floor;
		}
		RadioisotopeTestingFacility startingFacility = RadioisotopeTestingFacility.createNew(floorsArray, 0);
		Set<RadioisotopeTestingFacility> seenFacilities = new HashSet<>();
		Set<RadioisotopeTestingFacility> pendingFacilities = Collections.singleton(startingFacility);
		int totalDeviceCount = allDevices.size();
		int stepsCompleted = 0;
		int topFloorIndex = floorsArray.length-1;
		boolean solutionPending = true;
		while (solutionPending && !pendingFacilities.isEmpty()) {
			// BEGIN DEBUG
			System.out.println("------");
			System.out.println(stepsCompleted + "\t" + seenFacilities.size() + "\t" + pendingFacilities.size());
			// END DEBUG
			Set<RadioisotopeTestingFacility> newPendingFacilities = new HashSet<>();
			for (RadioisotopeTestingFacility pendingFacility : pendingFacilities) {
				if (seenFacilities.add(pendingFacility) && pendingFacility.isValid()) {
					/*
					// BEGIN DEBUG
					//System.out.println("---");
					dumpFacility("",pendingFacility);
					// END DEBUg
					*/
					if (pendingFacility.getFloor(topFloorIndex).getDeviceCount() == totalDeviceCount) {
						/*
						//BEGIN DEBUG
						System.out.println("Dumping chain");
						for (RadioisotopeTestingFacility chainFacility : chains.get(pendingFacility)) {
							System.out.println("---");
							dumpFacility("",chainFacility);
						}
						//END DEBUG
						*/
						solutionPending = false;
						break;
					}
					/*
					//BEGIN DEBUG
					Set<RadioisotopeTestingFacility> existingAddedFacilites = new HashSet<>(newPendingFacilities);
					//END DEBUG
					*/
					int currentFloorIndex = pendingFacility.getElevatorFloor();
					if (currentFloorIndex > 0) {
						addPossibleMovesAfterMovingElevator(newPendingFacilities, pendingFacility, currentFloorIndex, currentFloorIndex-1);
					}
					if (currentFloorIndex < topFloorIndex) {
						addPossibleMovesAfterMovingElevator(newPendingFacilities, pendingFacility, currentFloorIndex, currentFloorIndex + 1);
					}
					/*
					//BEGIN DEBUG
					Set<RadioisotopeTestingFacility> newlyAddedFacilites = new HashSet<>(newPendingFacilities);
					newlyAddedFacilites.removeAll(existingAddedFacilites);
					List<RadioisotopeTestingFacility> existingChain = chains.get(pendingFacility);
					if (existingChain == null) {
						existingChain = new ArrayList<>();
					}
					for (RadioisotopeTestingFacility newlyAdded : newlyAddedFacilites) {
						if (!chains.containsKey(newlyAdded)) {
							List<RadioisotopeTestingFacility> newChain = new ArrayList<>(existingChain);
							newChain.add(newlyAdded);
							chains.put(newlyAdded,newChain);
						}
					}
					//END DEBUG
					*/
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
		System.out.format("Total moves required: %d%n", stepsCompleted);
	}

	/*
	// BEGIN DEBUG
	private static void dumpFacility(String prefix, RadioisotopeTestingFacility facility) {
		int currentFloorIndex = facility.getElevatorFloor();
		for (int floorIndex=facility.floors.length-1; floorIndex>=0; floorIndex--) {
			System.out.print(prefix+"F"+(floorIndex+1)+" "+(floorIndex==currentFloorIndex?"E":"."));
			Set<Device> devices = new HashSet<>();
			Iterator<Device> deviceIterator = facility.getFloor(floorIndex).iterateDevices();
			while (deviceIterator.hasNext()) {
				devices.add(deviceIterator.next());
			}
			System.out.print(" ");
			for (String deviceName : deviceNamesOrderedList) {
				Device device = deviceNamesUnorderedNameToDeviceMap.get(deviceName);
				System.out.print(" ");
				if (devices.contains(device)) {
					System.out.print(deviceName);
				} else {
					System.out.print(". ");
				}
			}
			System.out.println(" ");
		}
	}
	*/

	private static void addPossibleMovesAfterMovingElevator(Set<RadioisotopeTestingFacility> pendingFacilities, RadioisotopeTestingFacility currentFacility, int currentFloorIndex, int newFloorIndex) {
		addPossibleMoves(pendingFacilities, currentFacility.createNewWithNewElevatorFloor(newFloorIndex), currentFloorIndex, currentFacility.getFloor(currentFloorIndex), newFloorIndex, new HashSet<>(), 2);
	}

	private static void addPossibleMoves(Set<RadioisotopeTestingFacility> pendingFacilities, RadioisotopeTestingFacility currentFacility, int currentFloorIndex, Floor currentFloor, int newFloorIndex, Set<Device> devicesLookedAt, int recursionsLeft) {
		int newRecursionsLeft = recursionsLeft - 1;
		/*
		//BEGIN DEBUG
		StringBuilder prefixBuilder = new StringBuilder();
		for (int prefixNumber=2; prefixNumber>=recursionsLeft; prefixNumber--) {
			prefixBuilder.append("   ");
		}
		String prefix = prefixBuilder.toString();
		//END DEBUG
		*/
		HashSet<Device> newDevicesLookedAt = new HashSet<>(devicesLookedAt);
		Iterator<Device> deviceIterator = currentFloor.iterateDevices();
		while (deviceIterator.hasNext()) {
			Device device = deviceIterator.next();
			if (newDevicesLookedAt.add(device)) {
				RadioisotopeTestingFacility newFacility = currentFacility.createNewWithDeviceMovedBetweenFloors(device, currentFloorIndex, newFloorIndex);
				/*
				//BEGIN DEBUG
				String info = prefix+"Moving " + deviceNamesUnorderedDeviceToNameMap.get(device) + " to floor " + newFloorIndex + " with newRecursionsLeft=" + newRecursionsLeft;
				System.out.println(info);
				dumpFacility(prefix+"  ",newFacility);
				//END DEBUG
				*/
				if (currentFacility != newFacility) {
					pendingFacilities.add(newFacility);
					if (newRecursionsLeft > 0) {
						addPossibleMoves(pendingFacilities, newFacility, currentFloorIndex, currentFloor, newFloorIndex, newDevicesLookedAt, newRecursionsLeft);
					}
				}
			}
		}
	}
}
