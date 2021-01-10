package uk.co.scottdennison.java.challenges.adventofcode.framework;

public class BasicPuzzleResults<PartAResultType, PartBResultType> implements IPuzzleResults {
	private final PartAResultType partAResult;
	private final PartBResultType partBResult;

	public BasicPuzzleResults(PartAResultType partAResult, PartBResultType partBResult) {
		this.partAResult = partAResult;
		this.partBResult = partBResult;
	}

	@ResultGetter
	public PartAResultType getPartAResult() {
		return this.partAResult;
	}

	@ResultGetter
	public PartBResultType getPartBResult() {
		return this.partBResult;
	}

	@Override
	public String getPartAAnswerString() {
		if (this.partAResult == null) {
			return null;
		}
		else {
			return this.partAResult.toString();
		}
	}

	@Override
	public String getPartBAnswerString() {
		if (this.partBResult == null) {
			return null;
		}
		else {
			return this.partBResult.toString();
		}
	}
}
