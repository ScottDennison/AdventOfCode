package uk.co.scottdennison.java.libs.math;

public class ChineseNumberTheorem {
    public static class Input {
        private final long n;
        private final long a;

        public Input(long n, long a) {
            this.n = n;
            this.a = a;
        }

        public long getN() {
            return this.n;
        }

        public long getA() {
            return this.a;
        }
    }

    // Based on https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Existence_(direct_construction) with sanity checks mentioned in https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Statement
    public static long solve(Input[] inputs) {
        int inputCount = inputs.length;
        long upperN = 1;
        for (int leftIndex = 0; leftIndex < inputCount; leftIndex++) {
            long nLeft = inputs[leftIndex].getN();
            upperN = Math.multiplyExact(upperN, nLeft);
            for (int rightIndex = leftIndex + 1; rightIndex < inputCount; rightIndex++) {
                long nRight = inputs[rightIndex].getN();
                long gcd = ExtendedEuclideanAlgorithm.solve(nLeft, nRight).getGcd();
                if (gcd != 1) {
                    throw new IllegalStateException("All n values must be pairwise coprime. " + nLeft + " and " + nRight + " have a gcd of " + gcd + ", and are therefore not pairwise coprime.");
                }
            }
        }
        long sum = 0;
        for (int i = 1; i <= inputCount; i++) {
            Input input = inputs[i - 1];
            long lowerNi = input.getN();
            if (lowerNi <= 1) {
                throw new IllegalArgumentException("All n values must be greater than 1. n value " + i + " is " + lowerNi);
            }
            long lowerAi = input.getA();
            while (lowerAi < 0) {
                lowerAi += lowerNi;
            }
            if (lowerAi >= lowerNi) {
                throw new IllegalArgumentException("All a values must be less than their equivalent n value. a value " + i + " is " + lowerAi);
            }
            long upperNi = upperN / lowerNi;
            ExtendedEuclideanAlgorithm.Result extendedEuclideanAlgorithmResult = ExtendedEuclideanAlgorithm.solve(upperNi, lowerNi);
            long s = extendedEuclideanAlgorithmResult.getBezoutCoefficientS();
            long t = extendedEuclideanAlgorithmResult.getBezoutCoefficientT();
            long mi;
            if (Math.abs(s) > Math.abs(t)) {
                mi = t;
            }
            else {
                mi = s;
            }
            sum = Math.addExact(sum, Math.multiplyExact(Math.multiplyExact(lowerAi, mi), upperNi));
        }
        while (sum < 0) {
            sum = Math.addExact(sum, upperN);
        }
        return sum % upperN;
    }
}
