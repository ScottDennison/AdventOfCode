package uk.co.scottdennison.java.soft.challenges.adventofcode.utils;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;

public class TestBed {
	public static void printCombinePhasedRotations(int period1, int phase1, int period2, int phase2) {
		System.out.print("combine phase rotations of {" + period1 + " with phase " + phase1 + "} and {" + period2 + " with phase " + phase2 + "} is: ");
		int phase1m = phase1 % period1;
		int phase2m = phase2 % period2;
		ExtendedEuclideanAlgorithm.Result extendedGCD = ExtendedEuclideanAlgorithm.solve(period1, period2);
		int gcd = (int)extendedGCD.getGcd();
		int lcm = (period1*period2)/gcd;
		int s = (int)extendedGCD.getBezoutCoefficientS();
		int t = (int)extendedGCD.getBezoutCoefficientT();
		int phaseDifference = phase1m - phase2m;
		int pdRemainder = Math.floorMod(phaseDifference, gcd);
		int pdMult = (phaseDifference-pdRemainder)/gcd;
		if (pdRemainder != 0) {
			System.out.println("No solvable");
		}
		int combinedPeriod = lcm;
		int minFirstX = Math.max(Math.abs(phase1),Math.abs(phase2));
		int firstX = ((pdMult * s * period1) - phase1m) % combinedPeriod;
		while (firstX < minFirstX) {
			firstX += combinedPeriod;
		}
		System.out.println("Period of " + lcm + " with first x of " + firstX + " and second x of " + (firstX+combinedPeriod));
	}

	public static void main(String[] args) {
		printCombinePhasedRotations(50,-162,11,-50); // dc and a
		printCombinePhasedRotations(50,-100,11,-50); // e and a
		printCombinePhasedRotations(50,-162,11,-59); // dc and b
		printCombinePhasedRotations(50,-100,11,-59); // e ad b
	}
}
