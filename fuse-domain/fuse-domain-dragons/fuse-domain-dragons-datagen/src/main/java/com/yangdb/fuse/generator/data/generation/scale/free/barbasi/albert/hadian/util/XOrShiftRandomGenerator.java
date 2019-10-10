package com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.util;

/*-
 *
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
