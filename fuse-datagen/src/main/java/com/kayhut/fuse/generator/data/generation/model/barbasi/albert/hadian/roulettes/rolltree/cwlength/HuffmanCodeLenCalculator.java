package com.kayhut.fuse.generator.data.generation.model.barbasi.albert.hadian.roulettes.rolltree.cwlength;

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
		HufNode root = (HufNode) freqHufNodeSet.firstEntry().getElement();
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
