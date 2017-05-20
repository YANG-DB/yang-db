package com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.roulettes.rolltree.datatypes;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class Bucket {
	private IntArrayList nodeIDs;
	private int degree;
	public TreeNode correspondingTreeNode = null;
	
	@Override
	public String toString() {
		String result = "(D:" + degree + ", S:" + nodeIDs.size() + ")"; 
		return result;
	}

	public Bucket(int bdegree) {
		degree = bdegree;
		nodeIDs = new IntArrayList();
	}
	
	public int getDegree() {
		return degree;
	}
	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public long getWeight(){
		return degree * nodeIDs.size();
	}
	
	public int getSize(){
		return nodeIDs.size();
	}
	
	public void addNode(int newNodeID){
		nodeIDs.add(newNodeID);
	}
	
	public void removeNodeAt(int index){
		nodeIDs.set(index, nodeIDs.get(nodeIDs.size()-1));
		nodeIDs.remove(nodeIDs.size()-1);		
	}
	
	public int getNodeAt(int index){
		return nodeIDs.get(index);
	}
	
	
	
}
