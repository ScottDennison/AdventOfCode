package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day10 {
	private interface Receiver {
		void receive(int value);
	}

	private static class OutputBinReceiver implements Receiver {
		private final FactoryManager factoryManager;
		private final int outputBinNumber;

		public OutputBinReceiver(FactoryManager factoryManager, int outputBinNumber) {
			this.factoryManager = factoryManager;
			this.outputBinNumber = outputBinNumber;
		}

		@Override
		public void receive(int value) {
			this.factoryManager.getOutputBin(this.outputBinNumber).putValue(value);
		}
	}

	private static class BotReceiver implements Receiver {
		private final FactoryManager factoryManager;
		private final int botNumber;

		public BotReceiver(FactoryManager factoryManager, int botNumber) {
			this.factoryManager = factoryManager;
			this.botNumber = botNumber;
		}

		@Override
		public void receive(int value) {
			this.factoryManager.getBot(this.botNumber).handleValue(value);
		}
	}

	private static class OutputBin {
		private boolean hasValue = false;
		private int value;

		public void putValue(int value) {
			if (this.hasValue) {
				throw new IllegalArgumentException("This output bin already as a value");
			}
			this.value = value;
			this.hasValue = true;
		}

		public int getValue() {
			if (!this.hasValue) {
				throw new IllegalStateException("Output bin does not have a value.");
			}
			return this.value;
		}
	}

	private static class Bot {
		private final int number;
		private final Receiver lowValueReceiver;
		private final Receiver highValueReceiver;

		private int receivedValueCount = 0;
		private int value1;
		private int value2;

		public Bot(int number, Receiver lowValueReceiver, Receiver highValueReceiver) {
			this.number = number;
			this.lowValueReceiver = lowValueReceiver;
			this.highValueReceiver = highValueReceiver;
		}

		public int getNumber() {
			return this.number;
		}

		public void handleValue(int value) {
			switch (receivedValueCount) {
				case 0:
					this.value1 = value;
					this.receivedValueCount++;
					break;
				case 1:
					if (value > this.value1) {
						this.value2 = value;
					}
					else {
						this.value2 = this.value1;
						this.value1 = value;
					}
					this.highValueReceiver.receive(this.value2);
					this.lowValueReceiver.receive(this.value1);
					this.receivedValueCount++;
					break;
				case 2:
					throw new IllegalStateException("Bot has already done it's work.");
			}
		}

		public boolean hasBotDoneWork() {
			return this.receivedValueCount == 2;
		}

		private void ensureBotHasDoneWork() {
			if (!this.hasBotDoneWork()) {
				throw new IllegalStateException("Bot has not yet completed it's work");
			}
		}

		public int getHighInputValue() {
			this.ensureBotHasDoneWork();
			return this.value2;
		}

		public int getLowInputValue() {
			this.ensureBotHasDoneWork();
			return this.value1;
		}
	}

	private static class FactoryManager {
		private final Map<Integer, Bot> registeredBots = new HashMap<>();
		private final Map<Integer, OutputBin> registeredOutputBins = new HashMap<>();

		public void registerBot(int number, Bot bot) {
			if (this.registeredBots.put(number, bot) != null) {
				throw new IllegalStateException("Bot already exists.");
			}
		}

		public Bot getBot(int number) {
			Bot bot = this.registeredBots.get(number);
			if (bot == null) {
				throw new IllegalStateException("Bot does not exist.");
			}
			return bot;
		}

		public Iterator<Bot> iterateBots() {
			return this.registeredBots.values().iterator();
		}

		public void registerOutputBin(int number, OutputBin outputBin) {
			if (this.registeredOutputBins.put(number, outputBin) != null) {
				throw new IllegalStateException("Output bin already exists.");
			}
		}

		public OutputBin getOutputBin(int number) {
			OutputBin outputBin = this.registeredOutputBins.get(number);
			if (outputBin == null) {
				throw new IllegalStateException("Output bin does not exist.");
			}
			return outputBin;
		}
	}

	private static class ReceiverHolder {
		private Receiver receiver;

		public Receiver getReceiver() {
			if (this.receiver == null) {
				throw new IllegalStateException("Receiver not set.");
			}
			return this.receiver;
		}

		public void setReceiver(Receiver receiver) {
			if (this.receiver != null) {
				throw new IllegalStateException("Receiver already set.");
			}
			this.receiver = receiver;
		}
	}

	private static final String LEVEL_LOW = "low";
	private static final String LEVEL_HIGH = "high";

	private static final String REGEX_LEVEL = LEVEL_LOW + '|' + LEVEL_HIGH;
	private static final Pattern PATTERN_VALUE_INPUT = Pattern.compile("^value (?<value>[0-9]+) goes to bot (?<botNumber>[0-9]+)$");
	private static final Pattern PATTERN_BOT_INSTRUCTION = Pattern.compile("^bot (?<inputBotNumber>[0-9]+) gives (?<leftLevel>" + REGEX_LEVEL + ") to (?<leftOutputType>bot|output) (?<leftOutputNumber>[0-9]+) and (?!\\2)(?<rightLevel>" + REGEX_LEVEL + ") to (?<rightOutputType>bot|output) (?<rightOutputNumber>[0-9]+)$");

	private static final int DESIRED_LOW_VALUE = 17;
	private static final int DESIRED_HIGH_VALUE = 61;
	private static final int MINIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT = 0;
	private static final int MAXIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT = 2;

	public static void main(String[] args) throws IOException {
		FactoryManager factoryManager = new FactoryManager();
		Map<Integer, Set<Integer>> botInputs = new HashMap<>();
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher valueInputMatcher = PATTERN_VALUE_INPUT.matcher(fileLine);
			if (valueInputMatcher.matches()) {
				botInputs.computeIfAbsent(Integer.parseInt(valueInputMatcher.group("botNumber")), __ -> new HashSet<>()).add(Integer.parseInt(valueInputMatcher.group("value")));
			}
			else {
				Matcher botInstructionMatcher = PATTERN_BOT_INSTRUCTION.matcher(fileLine);
				if (botInstructionMatcher.matches()) {
					Map<String, ReceiverHolder> receiverHolders = new HashMap<>();
					receiverHolders.put(LEVEL_LOW, new ReceiverHolder());
					receiverHolders.put(LEVEL_HIGH, new ReceiverHolder());
					parseBotInstruction(botInstructionMatcher, receiverHolders, factoryManager, "left");
					parseBotInstruction(botInstructionMatcher, receiverHolders, factoryManager, "right");
					int inputBotNumber = Integer.parseInt(botInstructionMatcher.group("inputBotNumber"));
					factoryManager.registerBot(inputBotNumber, new Bot(inputBotNumber, receiverHolders.get(LEVEL_LOW).getReceiver(), receiverHolders.get(LEVEL_HIGH).getReceiver()));
				}
				else {
					throw new IllegalStateException("Unparseable file line");
				}
			}
		}
		for (Map.Entry<Integer, Set<Integer>> botInputEntry : botInputs.entrySet()) {
			int botNumber = botInputEntry.getKey();
			for (int value : botInputEntry.getValue()) {
				factoryManager.getBot(botNumber).handleValue(value);
			}
		}
		Iterator<Bot> botIterator = factoryManager.iterateBots();
		Integer desiredBotNumber = null;
		while (botIterator.hasNext()) {
			Bot bot = botIterator.next();
			if (bot.hasBotDoneWork() && bot.getHighInputValue() == DESIRED_HIGH_VALUE && bot.getLowInputValue() == DESIRED_LOW_VALUE) {
				if (desiredBotNumber == null) {
					desiredBotNumber = bot.getNumber();
				}
				else {
					throw new IllegalStateException("Multiple bots have handled the desired values.");
				}
			}
		}
		if (desiredBotNumber == null) {
			throw new IllegalStateException("No bots have handled the desired values");
		}
		System.out.format("The bot that has handled %d and %d is %d%n", DESIRED_LOW_VALUE, DESIRED_HIGH_VALUE, desiredBotNumber);
		int outputBinProduct = 1;
		for (int outputBinNumber = MINIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT; outputBinNumber <= MAXIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT; outputBinNumber++) {
			outputBinProduct *= factoryManager.getOutputBin(outputBinNumber).getValue();
		}
		System.out.format("The products of output bins %d to %d (inclusive) is %d%n", MINIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT, MAXIMUM_OUTPUT_BIN_NUMBER_FOR_PRODUCT, outputBinProduct);
	}

	private static void parseBotInstruction(Matcher matcher, Map<String, ReceiverHolder> receiverHolders, FactoryManager factoryManager, String prefix) {
		ReceiverHolder receiverHolder = receiverHolders.get(matcher.group(prefix + "Level"));
		if (receiverHolder == null) {
			throw new IllegalStateException("No such receiver holder.");
		}
		int outputNumber = Integer.parseInt(matcher.group(prefix + "OutputNumber"));
		Receiver receiver;
		switch (matcher.group(prefix + "OutputType")) {
			case "bot":
				receiver = new BotReceiver(factoryManager, outputNumber);
				break;
			case "output":
				factoryManager.registerOutputBin(outputNumber, new OutputBin());
				receiver = new OutputBinReceiver(factoryManager, outputNumber);
				break;
			default:
				throw new IllegalStateException("Unexpected output type");
		}
		receiverHolder.setReceiver(receiver);
	}
}