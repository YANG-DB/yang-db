package com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.util;

/**
 * XOrShift is a very fast random number generator, used in ROLL-tree.
 * but not in SimpleRW and ROLL-bucket, since it might not have enough accuracy for them.
 * The reason is that ROLL-tree performs binary selections among two items with nearly equal probabilities,
 * but ROLL-bucket and SimpleRW need very accurate random numbers (selection among |E| items) this algorithm does not guarantee such accuracy.
 * 
 * More info: https://en.wikipedia.org/wiki/Xorshift
 * 
 * @author Ali Hadian
 *
 */
public class XOrShiftRandomGenerator {
	//seed
	static long x = Math.abs(System.nanoTime());

	/**
	 * Returns the next random number in range [Long.MIN_VALUE, Long.MAX_VALUE
	 * @return the next random number
     */
	public static long randomLong() {
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		if(x < 0) x *= -1;
		return x;
	}
}
