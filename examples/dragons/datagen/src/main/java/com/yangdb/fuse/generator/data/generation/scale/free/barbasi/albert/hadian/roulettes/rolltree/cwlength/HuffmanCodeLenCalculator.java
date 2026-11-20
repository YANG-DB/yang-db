package com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.cwlength;

/*-
 * #%L
 * fuse-domain-dragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
 * #L%
 */



import com.google.common.collect.TreeMultiset;

/**
 * Computes the code word length for a list of items (item_1, item_2, ..., item_n) with weights (w_1, w_2, ..., w_n).
 * @author Ali Hadian
 *
 */
public class HuffmanCodeLenCalculator {

	public static double computeHufmanLen(TreeMultiset<Long> weights) {
		TreeMultiset<HufNode> freqHufNodeSet = TreeMultiset.create();
		for(long  f : weights)
			freqHufNodeSet.add(new HufNode(f));
		
		while(freqHufNodeSet.size() > 1){
			HufNode right = freqHufNodeSet.firstEntry().getElement();
			freqHufNodeSet.remove(right);
			HufNode left = freqHufNodeSet.firstEntry().getElement();
			freqHufNodeSet.remove(left);
			freqHufNodeSet.add(new HufNode(right, left));
		}
		HufNode root = freqHufNodeSet.firstEntry().getElement();
		long totalFreq = root.freq;
		long codeLen = computeCodeLen(root,0);
		return 1.0 * codeLen / totalFreq;
	}
	
	private static long computeCodeLen(HufNode hufNode, int depth) {
		if(hufNode.rightChild == null && hufNode.leftChild == null)
			return hufNode.freq * depth;
		else{
			long leftL = computeCodeLen(hufNode.leftChild, depth+1);
			long rightL = computeCodeLen(hufNode.rightChild, depth+1);
			return leftL + rightL;
		}
		
	}
	
	

}
