package uk.co.scottdennison.java.challenges.adventofcode.framework;

public final class MultiPartPuzzleResults<PuzzlePartResultsType extends IPuzzlePartResults> implements IPuzzleResults {
	private final PuzzlePartResultsType partAPuzzlePartResults;
	private final PuzzlePartResultsType partBPuzzlePartResults;

	public MultiPartPuzzleResults(PuzzlePartResultsType partAPuzzlePartResults, PuzzlePartResultsType partBPuzzlePartResults) {
		this.partAPuzzlePartResults = partAPuzzlePartResults;
		this.partBPuzzlePartResults = partBPuzzlePartResults;
	}

	@ResultGetter
	public PuzzlePartResultsType getPartAPuzzlePartResults() {
		return this.partAPuzzlePartResults;
	}

	@ResultGetter
	public PuzzlePartResultsType getPartBPuzzlePartResults() {
		return this.partBPuzzlePartResults;
	}

	@Override
	public String getPartAAnswerString() {
		return this.partAPuzzlePartResults.getAnswerString();
	}

	@Override
	public String getPartBAnswerString() {
		return this.partBPuzzlePartResults.getAnswerString();
	}
}
