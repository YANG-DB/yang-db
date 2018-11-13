package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class Bucket {
	private IntArrayList nodeIDs;
	private int degree;
	public TreeNode correspondingTreeNode = null;
	
	@Override
	public String toString() {
		return "(D:" + degree + ", S:" + nodeIDs.size() + ")";
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
