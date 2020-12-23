package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day19Temp {
	private static final class Coordinate {
		private int i;
		private int j;

		public Coordinate() {
		}

		public Coordinate(int i, int j) {
			this.i = i;
			this.j = j;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}

		public int getJ() {
			return j;
		}

		public void setJ(int j) {
			this.j = j;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Coordinate that = (Coordinate) o;

			if (i != that.i) {
				return false;
			}
			return j == that.j;
		}

		@Override
		public int hashCode() {
			int result = i;
			result = 31 * result + j;
			return result;
		}

		@Override
		public String toString() {
			return "Coordinate{" +
				"i=" + i +
				", j=" + j +
				'}';
		}
	}

	private static final class Coordinates {
		@JsonProperty("changed_cell")
		private Coordinate changedCell;

		@JsonProperty("context_cells")
		private List<Coordinate> contextCells;

		public Coordinate getChangedCell() {
			return changedCell;
		}

		public void setChangedCell(Coordinate changedCell) {
			this.changedCell = changedCell;
		}

		public List<Coordinate> getContextCells() {
			return contextCells;
		}

		public void setContextCells(List<Coordinate> contextCells) {
			this.contextCells = contextCells;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Coordinates that = (Coordinates) o;

			if (changedCell != null ? !changedCell.equals(that.changedCell) : that.changedCell != null) {
				return false;
			}
			return contextCells != null ? contextCells.equals(that.contextCells) : that.contextCells == null;
		}

		@Override
		public int hashCode() {
			int result = changedCell != null ? changedCell.hashCode() : 0;
			result = 31 * result + (contextCells != null ? contextCells.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "Coordinates{" +
				"changedCell=" + changedCell +
				", contextCells=" + contextCells +
				'}';
		}
	}

	public static final class Entry {
		@JsonProperty("rule_matched")
		private List<String> ruleMatched;

		@JsonProperty("coordinates")
		private Coordinates coordinates;

		public List<String> getRuleMatched() {
			return ruleMatched;
		}

		public void setRuleMatched(List<String> ruleMatched) {
			this.ruleMatched = ruleMatched;
		}

		public Coordinates getCoordinates() {
			return coordinates;
		}

		public void setCoordinates(Coordinates coordinates) {
			this.coordinates = coordinates;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Entry entry = (Entry) o;

			if (ruleMatched != null ? !ruleMatched.equals(entry.ruleMatched) : entry.ruleMatched != null) {
				return false;
			}
			return coordinates != null ? coordinates.equals(entry.coordinates) : entry.coordinates == null;
		}

		@Override
		public int hashCode() {
			int result = ruleMatched != null ? ruleMatched.hashCode() : 0;
			result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "Entry{" +
				"ruleMatched=" + ruleMatched +
				", coordinates=" + coordinates +
				'}';
		}
	}

	private static void recurse(Map<Coordinate, List<Entry>> entriesGroupedByChangedCellCoordinate, List<String> dotGraphs, StringBuilder dotNodeDefinitionsGraphBuilder, StringBuilder dotEdgesGraphBuilder, String parentName, Coordinate thisCoordinate, boolean addToOutput) {
		List<Entry> entries = entriesGroupedByChangedCellCoordinate.get(thisCoordinate);
		if (entries == null || entries.isEmpty()) {
			throw new IllegalStateException("Something went wrong.");
		}
		else {
			String thisLabel = null;
			for (Entry entry : entries) {
				List<String> entryNames = entry.getRuleMatched();
				if (entryNames == null || entryNames.size() != 1) {
					throw new IllegalStateException("Entry doesn't have one name");
				}
				String entryName = entryNames.get(0);
				if (thisLabel == null) {
					thisLabel = entryName;
				}
				else if (!thisLabel.equals(entryName)) {
					throw new IllegalStateException("Entries have conflicting names");
				}
			}
			String thisName = "node_" + thisCoordinate.getI() + "_" + thisCoordinate.getJ();
			String nameToPass;
			switch (thisLabel) {
				case "Temp_00":
					thisLabel = "out_Ar";
					break;
				case "Temp_30":
					thisLabel = "out_Rn";
					break;
				case "Temp_31":
					thisLabel = "out_Y";
					break;
				case "Temp_32":
					thisLabel = "out_C";
					break;
			}
			if (thisLabel.startsWith("Temp_")) {
				nameToPass = parentName;
			}
			else {
				if (entries.size() == 1 && entries.get(0).getCoordinates().getContextCells().size() == 0) {
					if (thisLabel.startsWith("Rule_")) {
						thisLabel = "out_" + thisLabel.substring(5);
					}
					else if (!thisLabel.startsWith("out_")) {
						throw new IllegalStateException("Unexpected label");
					}
				}
				nameToPass = thisName;
				dotNodeDefinitionsGraphBuilder.append(thisName).append(" [label=\"").append(thisLabel).append("\"]\n");
				dotEdgesGraphBuilder.append(parentName).append(" -> ").append(thisName).append(";\n");
			}
			int baseDotNodeDefinitionsGraphBuilderLength = dotNodeDefinitionsGraphBuilder.length();
			int baseDotEdgesGraphBuilderLength = dotEdgesGraphBuilder.length();
			for (Entry entry : entries) {
				List<Coordinate> entryContextCoordinates = entry.getCoordinates().getContextCells();
				int lastEntryContextCoordinateIndex = entryContextCoordinates.size() - 1;
				if (lastEntryContextCoordinateIndex < 0) {
					if (addToOutput) {
						dotGraphs.add("digraph G {\n" + dotNodeDefinitionsGraphBuilder.toString() + dotEdgesGraphBuilder.toString() + "}");
					}
				}
				else {
					for (int entryContextCoordinateIndex = 0; entryContextCoordinateIndex <= lastEntryContextCoordinateIndex; entryContextCoordinateIndex++) {
						recurse(entriesGroupedByChangedCellCoordinate, dotGraphs, dotNodeDefinitionsGraphBuilder, dotEdgesGraphBuilder, nameToPass, entryContextCoordinates.get(entryContextCoordinateIndex), addToOutput && (entryContextCoordinateIndex == lastEntryContextCoordinateIndex));
					}
					//dotNodeDefinitionsGraphBuilder.setLength(baseDotNodeDefinitionsGraphBuilderLength);
					//dotEdgesGraphBuilder.setLength(baseDotEdgesGraphBuilderLength);
					break;
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		// Load data captured from lxmls.it.pt/2015/cky.html
		List<Entry> entries = objectMapper.readValue(new FileInputStream("data/year2015/day19/temp/lxmls_it_pt_cyk/captured_output.json"), objectMapper.getTypeFactory().constructCollectionType(List.class, Entry.class));
		Map<Coordinate, List<Entry>> entriesGroupedByChangedCellCoordinate =
			entries
				.stream()
				.collect(
					Collectors.groupingBy(
						entry -> entry.getCoordinates().getChangedCell()
					)
				);
		List<String> dotGraphs = new ArrayList<>();
		recurse(entriesGroupedByChangedCellCoordinate, dotGraphs, new StringBuilder("root\n"), new StringBuilder(), "root", new Coordinate(284, 0), true);
		System.out.println(dotGraphs);
	}
}
