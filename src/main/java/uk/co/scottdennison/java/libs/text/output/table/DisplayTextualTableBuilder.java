package uk.co.scottdennison.java.libs.text.output.table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DisplayTextualTableBuilder extends BaseTextualTableBuilder<DisplayTextualTableBuilder.AlignedValue> {
	public enum HorizontalAlignment {
		LEFT,
		CENTER,
		CENTER_BLOCK,
		RIGHT,
		RIGHT_BLOCK
	}

	public enum VerticalAlignment {
		TOP,
		MIDDLE,
		BOTTOM
	}

	protected static class AlignedValue {
		private final String value;
		private final HorizontalAlignment horizontalAlignment;
		private final VerticalAlignment verticalAlignment;

		private AlignedValue(String value, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
			this.value = value;
			this.horizontalAlignment = horizontalAlignment;
			this.verticalAlignment = verticalAlignment;
		}

		public String getValue() {
			return this.value;
		}

		public HorizontalAlignment getHorizontalAlignment() {
			return this.horizontalAlignment;
		}

		public VerticalAlignment getVerticalAlignment() {
			return this.verticalAlignment;
		}
	}

	private static class Cell {
		private final int width;
		private final int height;
		private final String[] content;
		private final HorizontalAlignment horizontalAlignment;
		private final VerticalAlignment verticalAlignment;

		private Cell(int width, int height, String[] content, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
			this.width = width;
			this.height = height;
			this.content = Arrays.copyOf(content, content.length);
			this.horizontalAlignment = horizontalAlignment;
			this.verticalAlignment = verticalAlignment;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}

		public String getContentLine(int index) {
			return this.content[index];
		}

		public HorizontalAlignment getHorizontalAlignment() {
			return this.horizontalAlignment;
		}

		public VerticalAlignment getVerticalAlignment() {
			return this.verticalAlignment;
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

	private static final Pattern PATTERN_NEWLINE = Pattern.compile("\\R");

	private HorizontalAlignment defaultHorizontalAlignment = HorizontalAlignment.LEFT;
	private VerticalAlignment defaultVerticalAlignment = VerticalAlignment.TOP;

	public HorizontalAlignment getDefaultHorizontalAlignment() {
		return this.defaultHorizontalAlignment;
	}

	public void setDefaultHorizontalAlignment(HorizontalAlignment defaultHorizontalAlignment) {
		if (defaultHorizontalAlignment == null) {
			throw new IllegalArgumentException("Alignment cannot be NULL.");
		}
		this.defaultHorizontalAlignment = defaultHorizontalAlignment;
	}

	public VerticalAlignment getDefaultVerticalAlignment() {
		return this.defaultVerticalAlignment;
	}

	public void setDefaultVerticalAlignment(VerticalAlignment defaultVerticalAlignment) {
		if (defaultVerticalAlignment == null) {
			throw new IllegalArgumentException("Alignment cannot be NULL.");
		}
		this.defaultVerticalAlignment = defaultVerticalAlignment;
	}

	public void addEntry(String header, String value) {
		this.addEntry(header, value, null);
	}

	public void addEntry(String header, String value, HorizontalAlignment horizontalAlignment) {
		this.addEntry(header, value, horizontalAlignment, null);
	}

	public void addEntry(String header, String value, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		this.addEntry(header, value == null ? null : new AlignedValue(value, horizontalAlignment, verticalAlignment));
	}

	private void appendLinesOutputRow(StringBuilder stringBuilder, int[] maximumSizesPerColumn, char outerLeftChar, char innerChar, char joinChar, char outerRightChar) {
		stringBuilder.append(outerLeftChar);
		int columnCount = maximumSizesPerColumn.length;
		boolean first = true;
		for (int columnIndex=0; columnIndex<columnCount; columnIndex++) {
			if (first) {
				first = false;
			}
			else {
				stringBuilder.append(joinChar);
			}
			int maximumSizeForColumn = maximumSizesPerColumn[columnIndex] + 2; // One space either side
			for (int innerNumber = 0; innerNumber < maximumSizeForColumn; innerNumber++) {
				stringBuilder.append(innerChar);
			}
		}
		stringBuilder.append(outerRightChar);
	}

	@Override
	public String toString() {
		return this.build(null, CharacterSet.ASCII, true, null);
	}

	private void addCell(Cell[][] cells, int[] maximumWidthsPerColumn, int[] maximumHeightsPerRow, int columnIndex, int rowIndex, String value, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		Cell cell;
		if (value == null) {
			cell = new Cell(0, 0, new String[0], this.defaultHorizontalAlignment, this.defaultVerticalAlignment);
		} else {
			String[] cellContentLines = PATTERN_NEWLINE.split(value);
			int cellHeight = cellContentLines.length;
			int cellWidth = 0;
			for (String line : cellContentLines) {
				cellWidth = Math.max(cellWidth, calculateLength(line));
			}
			HorizontalAlignment horizontalAlignmentToUse = horizontalAlignment;
			if (horizontalAlignmentToUse == null) {
				horizontalAlignmentToUse = this.defaultHorizontalAlignment;
			}
			VerticalAlignment verticalAlignmentToUse = verticalAlignment;
			if (verticalAlignmentToUse == null) {
				verticalAlignmentToUse = this.defaultVerticalAlignment;
			}
			cell = new Cell(cellWidth, cellHeight, cellContentLines, horizontalAlignmentToUse, verticalAlignmentToUse);
			maximumWidthsPerColumn[columnIndex] = Math.max(maximumWidthsPerColumn[columnIndex],cellWidth);
			maximumHeightsPerRow[rowIndex] = Math.max(maximumHeightsPerRow[rowIndex],cellHeight);
		}
		cells[rowIndex][columnIndex] = cell;
	}

	private void addCell(Cell[][] cells, int[] maximumWidthsPerColumn, int[] maximumHeightsPerRow, int columnIndex, int rowIndex, AlignedValue alignedValue) {
		if (alignedValue == null) {
			addCell(cells,maximumWidthsPerColumn,maximumHeightsPerRow,columnIndex,rowIndex,null,null,null);
		} else {
			addCell(cells,maximumWidthsPerColumn,maximumHeightsPerRow,columnIndex,rowIndex,alignedValue.getValue(),alignedValue.getHorizontalAlignment(),alignedValue.getVerticalAlignment());
		}
	}

	public String build(String linePrefix, CharacterSet characterSet, boolean includeRowDivider, Integer maximumDataRows) {
		List<String> headers = this.getKnownHeaders();
		List<Map<String, AlignedValue>> dataRows = this.getRows();
		int headerCount = headers.size();
		int dataRowsCount = dataRows.size();
		if (maximumDataRows != null) {
			dataRowsCount = Math.max(Math.min(dataRowsCount,maximumDataRows),0);
		}
		if (headerCount < 1 || dataRowsCount < 1) {
			return "";
		}
		else {
			String linePrefixToUse = linePrefix;
			if (linePrefixToUse == null) {
				linePrefixToUse = "";
			}
			int allRowsCount = dataRowsCount+1;
			Cell[][] cells = new Cell[allRowsCount][headerCount];
			int[] maximumWidthsPerColumn = new int[headerCount];
			int[] maximumHeightsPerRow = new int[allRowsCount];
			for (int columnIndex=0; columnIndex<headerCount; columnIndex++) {
				String header = headers.get(columnIndex);
				addCell(cells, maximumWidthsPerColumn, maximumHeightsPerRow, columnIndex, 0, header, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
				for (int dataRowIndex=0; dataRowIndex<dataRowsCount; dataRowIndex++) {
					addCell(cells,maximumWidthsPerColumn,maximumHeightsPerRow,columnIndex,dataRowIndex+1,dataRows.get(dataRowIndex).get(header));
				}
			}
			StringBuilder tableStringBuilder = new StringBuilder();
			String rowDivider;
			if (includeRowDivider) {
				StringBuilder rowDividerStringBuilder = new StringBuilder();
				appendLinesOutputRow(rowDividerStringBuilder, maximumWidthsPerColumn, characterSet.outerVerticalLeftJoinToInner, characterSet.innerHorizontal, characterSet.innerCrossJoin, characterSet.outerVerticalRightJoinToInner);
				rowDivider = rowDividerStringBuilder.toString();
			}
			else {
				rowDivider = null;
			}
			for (int rowIndex=0; rowIndex<allRowsCount; rowIndex++) {
				switch (rowIndex) {
					case 0:
						tableStringBuilder.append(linePrefixToUse);
						appendLinesOutputRow(tableStringBuilder, maximumWidthsPerColumn, characterSet.outerTopLeft, characterSet.outerHorizontalNoJoin, characterSet.outerHorizontalDownJoin, characterSet.outerTopRight);
						tableStringBuilder.append('\n');
						break;
					case 1:
						tableStringBuilder.append(linePrefixToUse);
						appendLinesOutputRow(tableStringBuilder, maximumWidthsPerColumn, characterSet.outerVerticalLeftJoinToDivider, characterSet.dividerHorizontal, characterSet.dividerCrossJoin, characterSet.outerVerticalRightJoinToDivider);
						tableStringBuilder.append('\n');
						break;
					default:
						if (includeRowDivider) {
							tableStringBuilder.append(linePrefixToUse).append(rowDivider).append('\n');
						}
				}
				int maximumHeight = maximumHeightsPerRow[rowIndex];
				for (int outputRowOfCellIndex=0; outputRowOfCellIndex<maximumHeight; outputRowOfCellIndex++) {
					tableStringBuilder.append(linePrefixToUse);
					for (int columnIndex=0; columnIndex<headerCount; columnIndex++) {
						tableStringBuilder.append(characterSet.outerVerticalNoJoin);
						Cell cell = cells[rowIndex][columnIndex];
						int cellHeight = cell.getHeight();
						int topPadding;
						switch (cell.getVerticalAlignment()) {
							case TOP:
								topPadding = 0;
								break;
							case MIDDLE:
								topPadding = (maximumHeight - cellHeight) / 2;
								break;
							case BOTTOM:
								topPadding = maximumHeight - cellHeight;
								break;
							default:
								throw new IllegalStateException("Impossible vertical alginment.");
						}
						int contentLineIndex = outputRowOfCellIndex-topPadding;
						boolean hasContentLine;
						String contentLine;
						int contentLineWidth;
						if (contentLineIndex >= 0 && contentLineIndex < cellHeight) {
							hasContentLine = true;
							contentLine = cell.getContentLine(contentLineIndex);
							contentLineWidth = BaseTextualTableBuilder.calculateLength(contentLine);
						} else {
							hasContentLine = false;
							contentLine = null;
							contentLineWidth = 0;
						}
						int maximumWidth = maximumWidthsPerColumn[columnIndex];
						int cellWidth = cell.getWidth();
						int leftPadding;
						switch (cell.getHorizontalAlignment()) {
							case LEFT:
								leftPadding = 0;
								break;
							case CENTER:
								leftPadding = (maximumWidth - contentLineWidth) / 2;
								break;
							case CENTER_BLOCK:
								leftPadding = (maximumWidth - cellWidth) / 2;
								break;
							case RIGHT:
								leftPadding = maximumWidth - contentLineWidth;
								break;
							case RIGHT_BLOCK:
								leftPadding = maximumWidth - cellWidth;
								break;
							default:
								throw new IllegalStateException("Impossible horizontal alignment.");
						}
						int rightPadding = maximumWidth - contentLineWidth - leftPadding;
						leftPadding += 1;
						rightPadding += 1;
						for (int spaceNumber = 0; spaceNumber < leftPadding; spaceNumber++) {
							tableStringBuilder.append(characterSet.space);
						}
						if (hasContentLine) {
							tableStringBuilder.append(contentLine);
						}
						for (int spaceNumber = 0; spaceNumber < rightPadding; spaceNumber++) {
							tableStringBuilder.append(characterSet.space);
						}
					}
					tableStringBuilder.append(characterSet.outerVerticalNoJoin).append('\n');
				}
			}
			tableStringBuilder.append(linePrefixToUse);
			appendLinesOutputRow(tableStringBuilder, maximumWidthsPerColumn, characterSet.outerBottomLeft, characterSet.outerHorizontalNoJoin, characterSet.outerHorizontalUpJoin, characterSet.outerBottomRight);
			tableStringBuilder.append('\n');
			return tableStringBuilder.toString();
		}
	}
}