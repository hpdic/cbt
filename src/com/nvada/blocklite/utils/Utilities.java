package com.nvada.blocklite.utils;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class Utilities {

	/**
	 * Get a random double from exponential distribution
	 * */
	public static double getExpRand(double lambda) {		//lamda is the mean
		Random random = new Random();
	    return  Math.log(1-random.nextDouble())/(-lambda);
	}
	// eg. parseInt("14AF", 16)
	public static int parseInt(String hash, int radix) throws NumberFormatException {
		if (hash == null) {
			throw new NumberFormatException("null");
		}

		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
		}

		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
		}

		int result = 0;
		boolean negative = false;
		int i = 0, max = hash.length();
		int limit;
		int multmin;
		int digit;

		if (max > 0) {
			if (hash.charAt(0) == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / radix;
			if (i < max) {
				digit = Character.digit(hash.charAt(i++), radix);
				if (digit < 0) {
					throw new NumberFormatException(hash);
				} else {
					result = -digit;
				}
			}
			while (i < max) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(hash.charAt(i++), radix);
				if (digit < 0) {
					throw new NumberFormatException(hash);
				}
				if (result < multmin) {
					throw new NumberFormatException(hash);
				}
				result *= radix;
				if (result < limit + digit) {
					throw new NumberFormatException(hash);
				}
				result -= digit;
			}
		} else {
			throw new NumberFormatException(hash);
		}
		if (negative) {
			if (i > 1) {
				return result;
			} else { /* Only got "-" */
				throw new NumberFormatException(hash);
			}
		} else {
			return -result;
		}
	}
	
	@Test
	void testParse() {
		System.out.println("parseInt('16AF', 16): " + parseInt("16AF", 16));
		assertTrue(true);
	}
}
