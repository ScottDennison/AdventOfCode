package uk.co.scottdennison.java.libs.math;

public class ExtendedEuclideanAlgorithm {
    private ExtendedEuclideanAlgorithm() {}

    public static class Result {
        private final long gcd;
        private final long bezoutCoefficientS;
        private final long bezoutCoefficientT;

        public Result(long gcd, long bezoutCoefficientS, long bezoutCoefficientT) {
            this.gcd = gcd;
            this.bezoutCoefficientS = bezoutCoefficientS;
            this.bezoutCoefficientT = bezoutCoefficientT;
        }

        public long getGcd() {
            return this.gcd;
        }

        public long getBezoutCoefficientS() {
            return this.bezoutCoefficientS;
        }

        public long getBezoutCoefficientT() {
            return this.bezoutCoefficientT;
        }
    }

    // Based on https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode
    public static Result solve(long a, long b) {
        long s = 0;
        long oldS = 1;
        long r = b;
        long oldR = a;
        while (r != 0) {
            long quotient = oldR / r;
            long temp;
            temp = oldR;
            oldR = r;
            r = temp - quotient * r;
            temp = oldS;
            oldS = s;
            s = temp - quotient * s;
        }
        long bezoutT;
        if (b != 0) {
            bezoutT = (oldR - oldS * a) / b;
        }
        else {
            bezoutT = 0;
        }
        return new Result(oldR, oldS, bezoutT);
    }
}
