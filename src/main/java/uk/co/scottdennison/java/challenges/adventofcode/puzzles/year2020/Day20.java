package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day20 implements IPuzzle {
	private enum Edge {
		TOP(-1, 0) {
			@Override
			public long getIdentityOf(boolean[][] lines, int size, boolean inverse) {
				return createIdentity(lines, size, inverse, 0, 0, 0, 1);
			}
		},
		RIGHT(0, 1) {
			@Override
			public long getIdentityOf(boolean[][] lines, int size, boolean inverse) {
				return createIdentity(lines, size, inverse, 0, size - 1, 1, 0);
			}
		},
		BOTTOM(1, 0) {
			@Override
			public long getIdentityOf(boolean[][] lines, int size, boolean inverse) {
				return createIdentity(lines, size, inverse, size - 1, size - 1, 0, -1);
			}
		},
		LEFT(0, -1) {
			@Override
			public long getIdentityOf(boolean[][] lines, int size, boolean inverse) {
				return createIdentity(lines, size, inverse, size - 1, 0, -1, 0);
			}
		};

		static {
			TOP.oppositeEdge = BOTTOM;
			BOTTOM.oppositeEdge = TOP;
			LEFT.oppositeEdge = RIGHT;
			RIGHT.oppositeEdge = LEFT;
		}

		private Edge oppositeEdge;
		private final int yIncrement;
		private final int xIncrement;

		Edge(int yIncrement, int xIncrement) {
			this.yIncrement = yIncrement;
			this.xIncrement = xIncrement;
		}

		private static long createIdentity(boolean[][] lines, int size, boolean inverse, int yStart, int xStart, int yIncrement, int xIncrement) {
			int bitField = 0;
			int bitIndexStart = inverse ? 0 : (size - 1);
			int bitIndexIncrement = inverse ? 1 : -1;
			for (int bitNumber = 1, bitIndex = bitIndexStart, y = yStart, x = xStart; bitNumber <= size; bitNumber++, bitIndex += bitIndexIncrement, y += yIncrement, x += xIncrement) {
				if (lines[y][x]) {
					bitField |= 1 << bitIndex;
				}
			}
			return bitField;
		}

		public abstract long getIdentityOf(boolean[][] lines, int size, boolean inverse);

		public Edge getOppositeEdge() {
			return this.oppositeEdge;
		}

		public int getYIncrement() {
			return this.yIncrement;
		}

		public int getXIncrement() {
			return this.xIncrement;
		}
	}

	private static final class Tile {
		private final int id;
		private final boolean[][] data;
		private final transient Long[] edgeIdentities = new Long[EDGE_IDENTITY_COUNT];
		private transient int hashCode;

		private static final int EDGE_COUNT = Edge.values().length;
		private static final int EDGE_IDENTITY_COUNT = EDGE_COUNT * 2;

		public Tile(int id, boolean[][] data) {
			this.id = id;
			int height = data.length;
			if (height < 1) {
				throw new IllegalStateException("Tile has no height.");
			}
			int width = data[0].length;
			if (width != height) {
				throw new IllegalStateException("Tile is not a square.");
			}
			this.data = new boolean[height][width];
			for (int y = 0; y < height; y++) {
				if (data[y].length != width) {
					throw new IllegalStateException("Tile is jagged.");
				}
				System.arraycopy(data[y], 0, this.data[y], 0, width);
			}
		}

		public int getId() {
			return this.id;
		}

		public boolean[][] getDataCopy() {
			int size = data.length;
			boolean[][] dataCopy = new boolean[size][size];
			for (int y = 0; y < size; y++) {
				System.arraycopy(data[y], 0, dataCopy[y], 0, size);
			}
			return dataCopy;
		}

		public long getEdgeIdentity(Edge edge, boolean inverse) {
			int edgeIdentityIndex = (inverse ? EDGE_COUNT : 0) + edge.ordinal();
			Long edgeIdentity = this.edgeIdentities[edgeIdentityIndex];
			if (edgeIdentity == null) {
				edgeIdentity = edge.getIdentityOf(this.data, this.data.length, inverse);
				this.edgeIdentities[edgeIdentityIndex] = edgeIdentity;
			}
			return edgeIdentity;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject == this) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != Tile.class) {
				return false;
			}
			Tile otherTile = (Tile) otherObject;
			return (this.id == otherTile.id) && dataEquals(this.data, otherTile.data);
		}

		private static boolean dataEquals(boolean[][] leftData, boolean[][] rightData) {
			if (leftData == rightData) {
				return true;
			}
			int size = leftData.length;
			if (size != rightData.length) {
				return false;
			}
			// We know by the constructor that each data is square, so we can now just straight iterate
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					if (leftData[y][x] != rightData[y][x]) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = Arrays.deepHashCode(data);
				this.hashCode = hashCode;
			}
			return hashCode;
		}

		@Override
		public String toString() {
			return "Tile{id=" + this.id + "}";
		}
	}

	@SuppressWarnings("SuspiciousNameCombination")
	private static boolean[][] createModifiedBooleanGrid(boolean[][] sourceData, boolean flippedHorizontallyBeforeRotation, int clockwiseRotations) {
		int sourceHeight = sourceData.length;
		int sourceWidth = sourceData[0].length;
		int sourceXStart = flippedHorizontallyBeforeRotation ? (sourceWidth - 1) : 0;
		int sourceXIncrement = flippedHorizontallyBeforeRotation ? -1 : 1;
		boolean[][] destData;
		if (clockwiseRotations % 2 == 0) {
			destData = new boolean[sourceHeight][sourceWidth];
		}
		else {
			destData = new boolean[sourceWidth][sourceHeight];
		}
		for (int sourceY = 0; sourceY < sourceHeight; sourceY++) {
			for (int iterationX = 0, sourceX = sourceXStart; iterationX < sourceWidth; iterationX++, sourceX += sourceXIncrement) {
				int destY;
				int destX;
				switch (clockwiseRotations) {
					case 0:
						destY = sourceY;
						destX = iterationX;
						break;
					case 1:
						destY = iterationX;
						destX = sourceHeight - sourceY - 1;
						break;
					case 2:
						destY = sourceHeight - sourceY - 1;
						destX = sourceWidth - iterationX - 1;
						break;
					case 3:
						destY = sourceWidth - iterationX - 1;
						destX = sourceY;
						break;
					default:
						throw new IllegalStateException("Unexpected clockwise rotations");
				}
				destData[destY][destX] = sourceData[sourceY][sourceX];
			}
		}
		return destData;
	}

	private static final class ModifiedTile {
		private final Tile tile;
		private final int clockwiseRotations;
		private final boolean flippedHorizontallyBeforeRotation;
		private transient int hashCode;
		private final transient Long[] edgeIdentities = new Long[EDGE_IDENTITY_COUNT];

		private static final Edge[] EDGES = Edge.values();
		private static final int EDGE_COUNT = Edge.values().length;
		private static final int EDGE_IDENTITY_COUNT = EDGE_COUNT * 2;

		public ModifiedTile(Tile tile, int clockwiseRotations, boolean flippedHorizontallyBeforeRotation) {
			this.tile = tile;
			this.clockwiseRotations = clockwiseRotations;
			this.flippedHorizontallyBeforeRotation = flippedHorizontallyBeforeRotation;
		}

		public Tile getTile() {
			return this.tile;
		}

		public boolean[][] getDataCopy() {
			return createModifiedBooleanGrid(this.tile.getDataCopy(), this.flippedHorizontallyBeforeRotation, this.clockwiseRotations);
		}

		public long getEdgeIdentity(Edge edge, boolean inverse) {
			int edgeIdentityIndex = (inverse ? EDGE_COUNT : 0) + edge.ordinal();
			Long edgeIdentity = this.edgeIdentities[edgeIdentityIndex];
			if (edgeIdentity == null) {
				Edge modifiedEdge = EDGES[(edge.ordinal() + (EDGE_COUNT - this.clockwiseRotations)) % EDGE_COUNT];
				if (this.flippedHorizontallyBeforeRotation && modifiedEdge.getYIncrement() == 0) {
					modifiedEdge = modifiedEdge.getOppositeEdge();
				}
				edgeIdentity = this.tile.getEdgeIdentity(modifiedEdge, this.flippedHorizontallyBeforeRotation ^ inverse);
				this.edgeIdentities[edgeIdentityIndex] = edgeIdentity;
			}
			return edgeIdentity;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject == this) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != ModifiedTile.class) {
				return false;
			}
			ModifiedTile otherModifiedTile = (ModifiedTile) otherObject;
			return
				this.clockwiseRotations == otherModifiedTile.clockwiseRotations
					&&
					this.flippedHorizontallyBeforeRotation == otherModifiedTile.flippedHorizontallyBeforeRotation
					&&
					this.tile.equals(otherModifiedTile.tile);
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = 31 * hashCode + tile.hashCode();
				hashCode = 31 * hashCode + clockwiseRotations;
				hashCode = 31 * hashCode + (flippedHorizontallyBeforeRotation ? 1 : 0);
				this.hashCode = hashCode;
			}
			return hashCode;
		}

		@Override
		public String toString() {
			return "ModifiedTile{id=" + this.tile.getId() + ",clockwiseRotations=" + this.clockwiseRotations + ",flippedHorizontallyBeforeRotation=" + this.flippedHorizontallyBeforeRotation + "}";
		}
	}

	private static class TileBuildHelper {
		private static final Pattern PATTERN_TILE_ID = Pattern.compile("^Tile (?<id>[0-9]+):$");

		private final List<Tile> tiles = new ArrayList<>();
		private final List<boolean[]> currentTileData = new ArrayList<>();
		private int tileSize = -1;
		private boolean hasTileId = false;
		private int tileId;

		public void acceptLine(char[] inputLine) {
			if (inputLine.length != 0) {
				Matcher tileIdMatcher = PATTERN_TILE_ID.matcher(new String(inputLine));
				if (tileIdMatcher.matches()) {
					this.finishTile(this.tileSize == -1);
					this.tileId = Integer.parseInt(tileIdMatcher.group("id"));
					this.hasTileId = true;
				}
				else {
					int inputLineWidth = inputLine.length;
					if (this.tileSize == -1) {
						this.tileSize = inputLineWidth;
					}
					else if (inputLineWidth != this.tileSize) {
						throw new IllegalStateException("Unexpected line width.");
					}
					boolean[] inputLineTileData = new boolean[inputLineWidth];
					for (int inputCharacterIndex = 0; inputCharacterIndex < inputLineWidth; inputCharacterIndex++) {
						boolean value;
						switch (inputLine[inputCharacterIndex]) {
							case '#':
								value = true;
								break;
							case '.':
								value = false;
								break;
							default:
								throw new IllegalStateException("Unexpected character.");
						}
						inputLineTileData[inputCharacterIndex] = value;
					}
					this.currentTileData.add(inputLineTileData);
				}
			}
		}

		public Collection<Tile> produceTiles() {
			this.finishTile(true);
			return new ArrayList<>(this.tiles);
		}

		private void finishTile(boolean allowNothing) {
			if (allowNothing && !this.hasTileId && this.currentTileData.isEmpty()) {
				return;
			}
			if (!this.hasTileId) {
				throw new IllegalStateException("No tile id");
			}
			else if (this.currentTileData.isEmpty()) {
				throw new IllegalStateException("No tile data");
			}
			else if (this.currentTileData.size() != this.tileSize) {
				throw new IllegalStateException("Unexpected tile data height.");
			}
			this.tiles.add(new Tile(this.tileId, this.currentTileData.toArray(new boolean[0][])));
			this.currentTileData.clear();
			this.hasTileId = false;
		}
	}

	private static final boolean SMP = true; // Sea monster part.
	private static final boolean ___ = false; // Not a sea monster part
	private static final boolean[][] SEA_MONSTER = {
		{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, SMP, ___},
		{SMP, ___, ___, ___, ___, SMP, SMP, ___, ___, ___, ___, SMP, SMP, ___, ___, ___, ___, SMP, SMP, SMP},
		{___, SMP, ___, ___, SMP, ___, ___, SMP, ___, ___, SMP, ___, ___, SMP, ___, ___, SMP, ___, ___, ___}
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		TileBuildHelper tileBuildHelper = new TileBuildHelper();
		LineReader.charArraysIterator(inputCharacters).forEachRemaining(tileBuildHelper::acceptLine);
		Collection<Tile> tiles = tileBuildHelper.produceTiles();
		int tileCount = tiles.size();
		int tileTileSize = (int) Math.sqrt(tileCount);
		if ((tileTileSize * tileTileSize) != tileCount) {
			throw new IllegalStateException("There is an incorrect amount of tiles to be able to fit them in a square grid");
		}
		Map<Integer, Set<ModifiedTile>> modifiedTilesById = new HashMap<>();
		Set<ModifiedTile> allModifiedTiles = new HashSet<>();
		Map<Edge, Map<Long, Set<ModifiedTile>>> modifiedTilesByEdgeAndEdgeIdentity = new HashMap<>();
		Edge[] edges = Edge.values();
		for (Edge edge : edges) {
			modifiedTilesByEdgeAndEdgeIdentity.put(edge, new HashMap<>());
		}
		for (Tile tile : tiles) {
			Set<ModifiedTile> modifiedTiles = new HashSet<>();
			for (int flippedHorizontallyBeforeRotationInt = 0; flippedHorizontallyBeforeRotationInt < 2; flippedHorizontallyBeforeRotationInt++) {
				boolean flippedHorizontallyBeforeRotation = flippedHorizontallyBeforeRotationInt != 0;
				for (int clockwiseRotations = 0; clockwiseRotations <= 3; clockwiseRotations++) {
					ModifiedTile modifiedTile = new ModifiedTile(tile, clockwiseRotations, flippedHorizontallyBeforeRotation);
					for (Edge edge : edges) {
						modifiedTilesByEdgeAndEdgeIdentity.get(edge).computeIfAbsent(modifiedTile.getEdgeIdentity(edge, false), k -> new HashSet<>()).add(modifiedTile);
					}
					modifiedTiles.add(modifiedTile);
				}
			}
			if (modifiedTilesById.put(tile.getId(), modifiedTiles) != null) {
				throw new IllegalStateException("Duplicate tile id");
			}
			allModifiedTiles.addAll(modifiedTiles);
		}
		Map<Long, Integer> edgeIdentityCounts = new HashMap<>();
		for (Map<Long, Set<ModifiedTile>> modifiedTilesByEdgeIdentity : modifiedTilesByEdgeAndEdgeIdentity.values()) {
			for (Map.Entry<Long, Set<ModifiedTile>> modifiedTilesByEdgeEntry : modifiedTilesByEdgeIdentity.entrySet()) {
				edgeIdentityCounts.merge(modifiedTilesByEdgeEntry.getKey(), modifiedTilesByEdgeEntry.getValue().size(), Integer::sum);
			}
		}
		Map<Edge, Set<ModifiedTile>> unpairedModifiedTilesByEdge = new HashMap<>();
		for (Edge edge : edges) {
			unpairedModifiedTilesByEdge.put(edge, new HashSet<>());
		}
		for (Set<ModifiedTile> modifiedTilesForId : modifiedTilesById.values()) {
			for (ModifiedTile modifiedTile : modifiedTilesForId) {
				for (Edge edge : edges) {
					int edgeIdentifierCount = edgeIdentityCounts.get(modifiedTile.getEdgeIdentity(edge, false));
					switch (edgeIdentifierCount) {
						case 4:
							unpairedModifiedTilesByEdge.get(edge).add(modifiedTile);
							break;
						case 8:
							break;
						default:
							throw new IllegalStateException("Unexpected edge identity count.");
					}
				}
			}
		}
		ModifiedTile[][] modifiedTileGrid;
		if ((modifiedTileGrid = attemptTilePlacement(modifiedTilesByEdgeAndEdgeIdentity, unpairedModifiedTilesByEdge, tileTileSize)) == null) {
			// We assumed that edges without any other matches must be on the edge of the combined grid. If we have gotten this far, that assumption was off. Oh well, time to try the slower approach.
			for (Edge edge : edges) {
				unpairedModifiedTilesByEdge.put(edge, allModifiedTiles);
			}
			if ((modifiedTileGrid = attemptTilePlacement(modifiedTilesByEdgeAndEdgeIdentity, unpairedModifiedTilesByEdge, tileTileSize)) == null) {
				throw new IllegalStateException("Tiles could not be arranged to form a grid.");
			}
		}
		int strippedTileSize = tiles.iterator().next().getDataCopy().length - 2;
		int combinedGridSize = tileTileSize * strippedTileSize;
		boolean[][] combinedGridData = new boolean[combinedGridSize][combinedGridSize];
		for (int tileY = 0, baseGridY = 0; tileY < tileTileSize; tileY++, baseGridY += strippedTileSize) {
			for (int tileX = 0, baseGridX = 0; tileX < tileTileSize; tileX++, baseGridX += strippedTileSize) {
				boolean[][] modifiedTileData = modifiedTileGrid[tileY][tileX].getDataCopy();
				for (int sourceY = 1, gridY = baseGridY; sourceY <= strippedTileSize; sourceY++, gridY++) {
					for (int sourceX = 1, gridX = baseGridX; sourceX <= strippedTileSize; sourceX++, gridX++) {
						combinedGridData[gridY][gridX] = modifiedTileData[sourceY][sourceX];
					}
				}
			}
		}
		int waveCount = sumGridPositives(combinedGridData);
		int fakeWaveCountPerSeaMonster = sumGridPositives(SEA_MONSTER);
		Integer seaMonstersFound = null;
		for (int flippedHorizontallyBeforeRotationInt = 0; flippedHorizontallyBeforeRotationInt < 2; flippedHorizontallyBeforeRotationInt++) {
			boolean flippedHorizontallyBeforeRotation = flippedHorizontallyBeforeRotationInt != 0;
			for (int clockwiseRotations = 0; clockwiseRotations <= 3; clockwiseRotations++) {
				boolean[][] seaMonsterToSearchFor = createModifiedBooleanGrid(SEA_MONSTER, flippedHorizontallyBeforeRotation, clockwiseRotations);
				int seaMonsterHeight = seaMonsterToSearchFor.length;
				int seaMonsterWidth = seaMonsterToSearchFor[0].length;
				int lastSearchStartY = combinedGridSize - seaMonsterHeight - 1;
				int lastSearchStartX = combinedGridSize - seaMonsterWidth - 1;
				int seaMonstersFoundForThisModification = 0;
				// Find sea monsters. This code technically would count overlapped sea monsters, but the challenge is ambiguous if that can actually happen, so assume it can't.
				for (int searchStartY = 0; searchStartY <= lastSearchStartY; searchStartY++) {
					for (int searchStartX = 0; searchStartX <= lastSearchStartX; searchStartX++) {
						boolean possibleSeaMonster = true;
						for (int seaMonsterY = 0, searchY = searchStartY; seaMonsterY < seaMonsterHeight; seaMonsterY++, searchY++) {
							for (int seaMonsterX = 0, searchX = searchStartX; seaMonsterX < seaMonsterWidth; seaMonsterX++, searchX++) {
								if (seaMonsterToSearchFor[seaMonsterY][seaMonsterX] && !combinedGridData[searchY][searchX]) {
									possibleSeaMonster = false;
									break;
								}
							}
							if (!possibleSeaMonster) {
								break;
							}
						}
						if (possibleSeaMonster) {
							seaMonstersFoundForThisModification++;
						}
					}
				}
				if (seaMonstersFoundForThisModification > 0) {
					if (seaMonstersFound == null) {
						seaMonstersFound = seaMonstersFoundForThisModification;
					}
					else {
						throw new IllegalStateException("Multiple modifications of the sea monster result in sea monsters being found.");
					}
				}
			}
		}
		if (seaMonstersFound == null) {
			throw new IllegalStateException("No sea monsters found.");
		}
		long gridCornerIdProduct = 1;
		for (int tileY = 0; tileY < tileTileSize; tileY += (tileTileSize - 1)) {
			for (int tileX = 0; tileX < tileTileSize; tileX += (tileTileSize - 1)) {
				gridCornerIdProduct *= modifiedTileGrid[tileY][tileX].getTile().getId();
			}
		}
		return new BasicPuzzleResults<>(
			gridCornerIdProduct,
			waveCount - (seaMonstersFound * fakeWaveCountPerSeaMonster)
		);
	}

	private static int sumGridPositives(boolean[][] grid) {
		int count = 0;
		for (boolean[] subGrid : grid) {
			for (boolean entry : subGrid) {
				if (entry) {
					count++;
				}
			}
		}
		return count;
	}

	private static ModifiedTile[][] attemptTilePlacement(Map<Edge, Map<Long, Set<ModifiedTile>>> modifiedTilesByEdgeAndEdgeIdentity, Map<Edge, Set<ModifiedTile>> unpairedModifiedTilesByEdge, int tileTileSize) {
		ModifiedTile[][] modifiedTileGrid = new ModifiedTile[tileTileSize][tileTileSize];
		if (attemptTilePlacement(modifiedTilesByEdgeAndEdgeIdentity, unpairedModifiedTilesByEdge, modifiedTileGrid, 0, 0, tileTileSize, new HashSet<>())) {
			return modifiedTileGrid;
		}
		else {
			return null;
		}
	}

	private static boolean attemptTilePlacement(Map<Edge, Map<Long, Set<ModifiedTile>>> modifiedTilesByEdgeAndEdgeIdentity, Map<Edge, Set<ModifiedTile>> unpairedModifiedTilesByEdge, ModifiedTile[][] modifiedTileGrid, int newTileY, int newTileX, int tileTileSize, Set<Integer> usedTileIds) {
		if (newTileY >= tileTileSize) {
			// If we have gotten this far, we have our solution.
			return true;
		}
		final int nextNewTileY;
		int nextNewTileX = newTileX + 1;
		if (nextNewTileX >= tileTileSize) {
			nextNewTileX = 0;
			nextNewTileY = newTileY + 1;
		}
		else {
			nextNewTileY = newTileY;
		}
		Set<ModifiedTile> possibleModifiedTiles = null;
		for (Edge edge : Edge.values()) {
			Edge oppositeEdge = edge.getOppositeEdge();
			int relationTileY = newTileY + edge.getYIncrement();
			int relationTileX = newTileX + edge.getXIncrement();
			Set<ModifiedTile> possibleModifiedTilesForEdge;
			if (relationTileY < 0 || relationTileX < 0 || relationTileY >= tileTileSize || relationTileX >= tileTileSize) {
				possibleModifiedTilesForEdge = unpairedModifiedTilesByEdge.get(edge);
				if (possibleModifiedTilesForEdge == null) {
					throw new IllegalStateException("No edge possibilities");
				}
			}
			else {
				ModifiedTile relationTile = modifiedTileGrid[relationTileY][relationTileX];
				if (relationTile != null) {
					possibleModifiedTilesForEdge = modifiedTilesByEdgeAndEdgeIdentity.get(edge).get(relationTile.getEdgeIdentity(oppositeEdge, true));
					if (possibleModifiedTilesForEdge == null) {
						possibleModifiedTiles = Collections.emptySet();
						break;
					}
				}
				else {
					possibleModifiedTilesForEdge = null;
				}
			}
			if (possibleModifiedTilesForEdge != null) {
				if (possibleModifiedTiles == null) {
					possibleModifiedTiles = new HashSet<>(possibleModifiedTilesForEdge);
				}
				else {
					possibleModifiedTiles.retainAll(possibleModifiedTilesForEdge);
				}
			}
		}
		if (possibleModifiedTiles == null) {
			return false;
		}
		for (ModifiedTile modifiedTile : possibleModifiedTiles) {
			int tileId = modifiedTile.getTile().getId();
			if (usedTileIds.add(tileId)) {
				modifiedTileGrid[newTileY][newTileX] = modifiedTile;
				if (attemptTilePlacement(modifiedTilesByEdgeAndEdgeIdentity, unpairedModifiedTilesByEdge, modifiedTileGrid, nextNewTileY, nextNewTileX, tileTileSize, usedTileIds)) {
					return true;
				}
				modifiedTileGrid[newTileY][newTileX] = null;
				usedTileIds.remove(tileId);
			}
		}
		return false;
	}
}