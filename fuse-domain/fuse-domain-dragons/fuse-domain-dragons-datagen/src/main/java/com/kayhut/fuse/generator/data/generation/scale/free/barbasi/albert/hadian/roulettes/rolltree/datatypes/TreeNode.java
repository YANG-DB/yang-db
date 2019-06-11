package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

public class TreeNode {
	private long weight;
	private boolean root = false;
	private Bucket bucket;
	private TreeNode lchild = null;
	private TreeNode rchild = null;
	private TreeNode parent = null;
	
	@Override
	public String toString() {
		String result = "{";
		if(isRoot())
			result += "ROOT, ";
		if(isDataNode())
			result += bucket.toString();
		else{
			if(lchild != null)
				result += " L:" + lchild.toString();
			if(rchild != null)
				result += " R:" + rchild.toString();
		}
		result += "}";
		return result;
	}
	
	public double getCodeWordLength(long numEdges){
		int depth = 0; 
		double codeLength = getCodeLength(depth);
		double totalWeights = 2 * numEdges;
		return codeLength / totalWeights ;	
	}
	
	private long getCodeLength(int depth) {
		long codeLen = 0;
		if(isDataNode())
			codeLen = depth * this.getBucket().getWeight();
		else{
			if(lchild != null)
				codeLen += lchild.getCodeLength(depth+1);
			if(rchild != null)
				codeLen += rchild.getCodeLength(depth+1);
		}
		return codeLen;
	}

	public Bucket getBucket() {
		return bucket;
	}
	
	public TreeNode(boolean isRoot) {
		super();
		this.root = isRoot;
	}
	
	public TreeNode(Bucket newBucket) {
		super();
		this.bucket = newBucket;
		this.bucket.correspondingTreeNode = this;
	}
	
	public boolean isDataNode(){
		return (this.bucket != null);
	}
	
	public long getWeight() {
		if(isDataNode())
			return bucket.getWeight();
		return weight;	
	}
	public void setWeight(long weight) {
		if(weight < 0){
			System.err.println("Integer overflow!!!");
			System.exit(1);
		}
		this.weight = weight;
	}
	
	public boolean isRoot() {
		return root;
	}

	public TreeNode getLchild() {
		if(isDataNode()){
//			System.err.println("ERROR: access to child of a data node! ");
			return null;
		}
		return lchild;
	}
	
	public void setLchild(TreeNode lchild) {
		if(isDataNode()){
			System.err.println("This is a data node!");
			return;
		}
		this.lchild = lchild;
	}
	
	public TreeNode getRchild() {
		if(isDataNode()){
//			System.err.println("ERROR: access to child of a data node! ");
			return null;
		}
		return rchild;
	}
	
	public void setRchild(TreeNode rchild) {
		if(isDataNode()){
			System.err.println("This is a data node!");
			return;
		}
		this.rchild = rchild;
	}
	
	public long getLchildWeight(){
		if(this.getLchild() != null)
			return this.getLchild().getWeight();
		return 0;
	}
	
	public long getRchildWeight(){
		if(this.getRchild() != null)
			return this.getRchild().getWeight();
		return 0;
	}
	
	
	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
		//System.err.println("TODO: weight of the parent should be updated and propagated!");
	}
}
