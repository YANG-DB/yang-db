package com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.roulettes.rolltree.cwlength;

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
