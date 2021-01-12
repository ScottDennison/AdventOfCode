package uk.co.scottdennison.java.libs.text.output.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayTextualTableBuilder extends BaseTextualTableBuilder<DisplayTextualTableBuilder.AlignedValue> {
	public enum Alignment {
		LEFT,
		CENTER,
		RIGHT
	}

	protected static class AlignedValue {
		private final String value;
		private final Alignment alignment;

		private AlignedValue(String value, Alignment alignment) {
			this.value = value;
			this.alignment = alignment;
		}

		public String getValue() {
			return value;
		}

		public Alignment getAlignment() {
			return alignment;
		}
	}

	public static final class CharacterSet {
		public static final CharacterSet ASCII = new CharacterSet('/', '\\', '\\', '/', '-', '+', '+', '|', '+', '+', '+', '+', '-', '+', '|', '-', '+', '|', ' ');
		public static final CharacterSet BASIC_BOX_DRAWING_SINGLE_WIDTH_OUTER = new CharacterSet('┌', '┐', '└', '┘', '─', '┬', '┴', '│', '├', '┤', '├', '┤', '─', '─', '│', '─', '┼', '│', ' ');
		public static final CharacterSet BASIC_BOX_DRAWING_DOUBLE_WIDTH_OUTER = new CharacterSet('╔', '╗', '╚', '╝', '═', '╦', '╩', '║', '╠', '╣', '╟', '╢', '─', '╫', '║', '═', '╬', '║', ' ');

		private final char outerTopLeft;
		private final char outerTopRight;
		private final char outerBottomLeft;
		private final char outerBottomRight;
		private final char outerHorizontalNoJoin;
		private final char outerHorizontalDownJoin;
		private final char outerHorizontalUpJoin;
		private final char outerVerticalNoJoin;
		private final char outerVerticalLeftJoinToDivider;
		private final char outerVerticalRightJoinToDivider;
		private final char outerVerticalLeftJoinToInner;
		private final char outerVerticalRightJoinToInner;
		private final char innerHorizontal;
		private final char innerCrossJoin;
		private final char innerVertical;
		private final char dividerHorizontal;
		private final char dividerCrossJoin;
		private final char headerVertical;
		private final char space;

		public CharacterSet(char outerTopLeft, char outerTopRight, char outerBottomLeft, char outerBottomRight, char outerHorizontalNoJoin, char outerHorizontalDownJoin, char outerHorizontalUpJoin, char outerVerticalNoJoin, char outerVerticalLeftJoinToDivider, char outerVerticalRightJoinToDivider, char outerVerticalLeftJoinToInner, char outerVerticalRightJoinToInner, char innerHorizontal, char innerCrossJoin, char innerVertical, char dividerHorizontal, char dividerCrossJoin, char headerVertical, char space) {
			this.outerTopLeft = outerTopLeft;
			this.outerTopRight = outerTopRight;
			this.outerBottomLeft = outerBottomLeft;
			this.outerBottomRight = outerBottomRight;
			this.outerHorizontalNoJoin = outerHorizontalNoJoin;
			this.outerHorizontalDownJoin = outerHorizontalDownJoin;
			this.outerHorizontalUpJoin = outerHorizontalUpJoin;
			this.outerVerticalNoJoin = outerVerticalNoJoin;
			this.outerVerticalLeftJoinToDivider = outerVerticalLeftJoinToDivider;
			this.outerVerticalRightJoinToDivider = outerVerticalRightJoinToDivider;
			this.outerVerticalLeftJoinToInner = outerVerticalLeftJoinToInner;
			this.outerVerticalRightJoinToInner = outerVerticalRightJoinToInner;
			this.innerHorizontal = innerHorizontal;
			this.innerCrossJoin = innerCrossJoin;
			this.innerVertical = innerVertical;
			this.dividerHorizontal = dividerHorizontal;
			this.dividerCrossJoin = dividerCrossJoin;
			this.headerVertical = headerVertical;
			this.space = space;
		}

		public char getOuterTopLeft() {
			return outerTopLeft;
		}

		public char getOuterTopRight() {
			return outerTopRight;
		}

		public char getOuterBottomLeft() {
			return outerBottomLeft;
		}

		public char getOuterBottomRight() {
			return outerBottomRight;
		}

		public char getOuterHorizontalNoJoin() {
			return outerHorizontalNoJoin;
		}

		public char getOuterHorizontalDownJoin() {
			return outerHorizontalDownJoin;
		}

		public char getOuterHorizontalUpJoin() {
			return outerHorizontalUpJoin;
		}

		public char getOuterVerticalNoJoin() {
			return outerVerticalNoJoin;
		}

		public char getOuterVerticalLeftJoinToDivider() {
			return outerVerticalLeftJoinToDivider;
		}

		public char getOuterVerticalRightJoinToDivider() {
			return outerVerticalRightJoinToDivider;
		}

		public char getOuterVerticalLeftJoinToInner() {
			return outerVerticalLeftJoinToInner;
		}

		public char getOuterVerticalRightJoinToInner() {
			return outerVerticalRightJoinToInner;
		}

		public char getInnerHorizontal() {
			return innerHorizontal;
		}

		public char getInnerCrossJoin() {
			return innerCrossJoin;
		}

		public char getInnerVertical() {
			return innerVertical;
		}

		public char getDividerHorizontal() {
			return dividerHorizontal;
		}

		public char getDividerCrossJoin() {
			return dividerCrossJoin;
		}

		public char getHeaderVertical() {
			return headerVertical;
		}

		public char getSpace() {
			return space;
		}
	}

	public void addEntry(String header, String value) {
		this.addEntry(header, value, Alignment.RIGHT);
	}

	public void addEntry(String header, String value, Alignment alignment) {
		this.addEntry(header, value == null ? null : new AlignedValue(value, alignment));
	}

	private void appendPaddedValue(StringBuilder stringBuilder, String value, Alignment alignment, CharacterSet characterSet, int size) {
		String valueForUse = value;
		Alignment alignmentForUse = alignment;
		if (valueForUse == null) {
			valueForUse = "";
		}
		if (alignmentForUse == null) {
			alignmentForUse = Alignment.CENTER;
		}
		int totalPadding = size - BaseTextualTableBuilder.calculateLength(valueForUse) - 2;
		int leftPadding;
		int rightPadding;
		switch (alignmentForUse) {
			case LEFT:
				leftPadding = 0;
				rightPadding = totalPadding;
				break;
			case CENTER:
				rightPadding = totalPadding / 2;
				leftPadding = totalPadding - rightPadding;
				break;
			case RIGHT:
				leftPadding = totalPadding;
				rightPadding = 0;
				break;
			default:
				throw new IllegalStateException("Unexpected alignment.");
		}
		leftPadding += 1;
		rightPadding += 1;
		for (int spaceNumber = 0; spaceNumber < leftPadding; spaceNumber++) {
			stringBuilder
				.append(characterSet.space);
		}
		stringBuilder
			.append(valueForUse);
		for (int spaceNumber = 0; spaceNumber < rightPadding; spaceNumber++) {
			stringBuilder
				.append(characterSet.space);
		}
	}

	private void appendLinesRow(StringBuilder stringBuilder, List<String> knownHeadersList, Map<String, Integer> maximumSizesPerColumn, char outerLeftChar, char innerChar, char joinChar, char outerRightChar) {
		stringBuilder.append(outerLeftChar);
		boolean first = true;
		for (String header : knownHeadersList) {
			if (first) {
				first = false;
			}
			else {
				stringBuilder.append(joinChar);
			}
			int maximumSizeForColumn = maximumSizesPerColumn.get(header);
			for (int innerNumber = 0; innerNumber < maximumSizeForColumn; innerNumber++) {
				stringBuilder.append(innerChar);
			}
		}
		stringBuilder.append(outerRightChar);
		stringBuilder.append('\n');
	}

	@Override
	public String toString() {
		return this.build(null, CharacterSet.ASCII, true, null);
	}

	public String build(String linePrefix, CharacterSet characterSet, boolean includeRowDivider, Integer maximumDataRows) {
		List<String> knownHeadersList = this.getKnownHeaders();
		List<Map<String, AlignedValue>> rows = this.getRows();
		if (knownHeadersList.isEmpty() || rows.isEmpty()) {
			return "";
		}
		else {
			String linePrefixToUse = linePrefix;
			if (linePrefixToUse == null) {
				linePrefixToUse = "";
			}
			Integer maximumDataRowsToUse = maximumDataRows;
			if (maximumDataRowsToUse != null && maximumDataRowsToUse < 0) {
				maximumDataRowsToUse = 0;
			}
			StringBuilder tableStringBuilder = new StringBuilder();
			Map<String, Integer> maximumSizesPerColumn = new HashMap<>();
			for (String header : knownHeadersList) {
				int maximumSize = BaseTextualTableBuilder.calculateLength(header);
				for (Map<String, AlignedValue> row : rows) {
					AlignedValue alignedValue = row.get(header);
					if (alignedValue != null) {
						int valueSize = BaseTextualTableBuilder.calculateLength(alignedValue.getValue());
						if (valueSize > maximumSize) {
							maximumSize = valueSize;
						}
					}
				}
				maximumSizesPerColumn.put(header, maximumSize + 2); // At least 1 space of padding.
			}
			String rowDivider;
			if (includeRowDivider) {
				StringBuilder rowDividerStringBuilder = new StringBuilder();
				appendLinesRow(rowDividerStringBuilder, knownHeadersList, maximumSizesPerColumn, characterSet.outerVerticalLeftJoinToInner, characterSet.innerHorizontal, characterSet.innerCrossJoin, characterSet.outerVerticalRightJoinToInner);
				rowDivider = rowDividerStringBuilder.toString();
			}
			else {
				rowDivider = null;
			}
			tableStringBuilder.append(linePrefixToUse);
			appendLinesRow(tableStringBuilder, knownHeadersList, maximumSizesPerColumn, characterSet.outerTopLeft, characterSet.outerHorizontalNoJoin, characterSet.outerHorizontalDownJoin, characterSet.outerTopRight);
			tableStringBuilder.append(linePrefixToUse).append(characterSet.outerVerticalNoJoin);
			boolean firstColumn = true;
			for (String header : knownHeadersList) {
				if (firstColumn) {
					firstColumn = false;
				}
				else {
					tableStringBuilder.append(characterSet.headerVertical);
				}
				appendPaddedValue(tableStringBuilder, header, Alignment.CENTER, characterSet, maximumSizesPerColumn.get(header));
			}
			tableStringBuilder.append(characterSet.outerVerticalNoJoin).append('\n').append(linePrefixToUse);
			appendLinesRow(tableStringBuilder, knownHeadersList, maximumSizesPerColumn, characterSet.outerVerticalLeftJoinToDivider, characterSet.dividerHorizontal, characterSet.dividerCrossJoin, characterSet.outerVerticalRightJoinToDivider);
			int dataRowNumber = 0;
			for (Map<String, AlignedValue> row : rows) {
				dataRowNumber++;
				if (maximumDataRowsToUse != null && dataRowNumber > maximumDataRowsToUse) {
					break;
				}
				if (dataRowNumber > 1 && includeRowDivider) {
					tableStringBuilder.append(linePrefixToUse).append(rowDivider);
				}
				tableStringBuilder.append(linePrefixToUse).append(characterSet.outerVerticalNoJoin);
				firstColumn = true;
				for (String header : knownHeadersList) {
					if (firstColumn) {
						firstColumn = false;
					}
					else {
						tableStringBuilder.append(characterSet.innerVertical);
					}
					AlignedValue alignedValue = row.get(header);
					String value;
					Alignment alignment;
					if (alignedValue == null) {
						value = null;
						alignment = null;
					}
					else {
						value = alignedValue.getValue();
						alignment = alignedValue.getAlignment();
					}
					appendPaddedValue(tableStringBuilder, value, alignment, characterSet, maximumSizesPerColumn.get(header));
				}
				tableStringBuilder.append(characterSet.outerVerticalNoJoin).append('\n');
			}
			tableStringBuilder.append(linePrefixToUse);
			appendLinesRow(tableStringBuilder, knownHeadersList, maximumSizesPerColumn, characterSet.outerBottomLeft, characterSet.outerHorizontalNoJoin, characterSet.outerHorizontalUpJoin, characterSet.outerBottomRight);
			return tableStringBuilder.toString();
		}
	}
}