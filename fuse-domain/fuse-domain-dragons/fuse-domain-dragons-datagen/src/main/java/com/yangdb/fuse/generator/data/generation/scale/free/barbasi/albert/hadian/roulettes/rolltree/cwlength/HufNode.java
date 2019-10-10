package com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.cwlength;

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
 * Element of a Huffman tree, used for computing the Huffman Code word length 
 * @author Ali Hadian
 *
 */
public class HufNode implements Comparable<HufNode>{
	public static int nodeIDCounter = 0;

	public int nodeId;
	public HufNode rightChild, leftChild;
	public long freq;
	@Override
	public int compareTo(HufNode o) {
		if(freq != o.freq)
			return ((Long) freq).compareTo(o.freq);
		return ((Integer) nodeId).compareTo(o.nodeId);
	}
	public HufNode(long frequency) {
		this.nodeId = nodeIDCounter++;
		this.freq = frequency;
	}
	public HufNode(HufNode right, HufNode left) {
		this(right.freq + left.freq);
		this.rightChild = right;
		this.leftChild = left;
	}
}
