package uk.co.scottdennison.java.soft.challenges.adventofcode.framework;

public class BasicPuzzlePartResults<PartResultType> implements IPuzzlePartResults {
	private final PartResultType partResult;

	public BasicPuzzlePartResults(PartResultType partResult) {
		this.partResult = partResult;
	}

	@ResultGetter
	public PartResultType getPartResult() {
		return this.partResult;
	}

	@Override
	public String getAnswerString() {
		if (this.partResult == null) {
			return null;
		}
		else {
			return this.partResult.toString();
		}
	}
}
