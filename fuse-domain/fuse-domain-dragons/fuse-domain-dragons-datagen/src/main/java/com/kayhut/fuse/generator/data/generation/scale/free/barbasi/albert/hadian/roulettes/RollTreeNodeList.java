package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;

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

import com.google.common.collect.TreeMultiset;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.cwlength.HuffmanCodeLenCalculator;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes.Bucket;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes.TreeNode;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.util.XOrShiftRandomGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.Map;
import java.util.Random;

/**
 * ROLL-tree data structure for generating Barabasi-Albert graphs. See the ROLL paper, section 4.2
 * @author Ali Hadian
 *
 */
public class RollTreeNodeList implements NodesList {
	public long numDeletes, numInserts;
	protected static Random random = new Random();
	protected TreeNode root = new TreeNode(true);
	protected Int2ObjectOpenHashMap<Bucket> buckets = new Int2ObjectOpenHashMap<Bucket>();

	public RollTreeNodeList() {
		root = new TreeNode(true);
	}

	public void addNode(int nodeId, int nodeDegree) {
		TreeNode currentHufNode = root;
		Bucket bucket = buckets.get(nodeDegree);

		if(bucket != null){
			bucket.addNode(nodeId);
			updateTree(bucket.correspondingTreeNode.getParent());
			return;
		}
		numInserts++;
		//finding position of the nodes
		bucket = new Bucket(nodeDegree);
		bucket.addNode(nodeId);
		buckets.put(bucket.getDegree(), bucket);
		TreeNode newHufNode = new TreeNode(bucket);
		//node.setRouletteWheelIndex(node.getDegree());
		while(true){ //while it is not a leaf
			if(currentHufNode.isDataNode()){
				TreeNode midNode = new TreeNode(false);
				midNode.setParent(currentHufNode.getParent());
				midNode.setLchild(currentHufNode);
				currentHufNode.setParent(midNode);
				midNode.setRchild(newHufNode);
				newHufNode.setParent(midNode);
				if(midNode.getParent().getRchild() == currentHufNode)
					midNode.getParent().setRchild(midNode);
				else if(midNode.getParent().getLchild() == currentHufNode)
					midNode.getParent().setLchild(midNode);
				else 
					System.err.println("WHERE DOES THIS CurrentNode came from?");
				updateTree(midNode);
				break;
			}
			if(currentHufNode.getLchild() == null){
				currentHufNode.setLchild(newHufNode);
				newHufNode.setParent(currentHufNode);
				break;
			} 
			if(currentHufNode.getRchild() == null){
				currentHufNode.setRchild(newHufNode);
				newHufNode.setParent(currentHufNode);
				break;
			}
			// else select one of the leafs which is not 
			if(currentHufNode.getLchildWeight() <= currentHufNode.getRchildWeight())
				currentHufNode = currentHufNode.getLchild();
			else
				currentHufNode = currentHufNode.getRchild();
		}

		updateTree(newHufNode);
	}

	public void updateTree(TreeNode node) {
		node.setWeight(node.getLchildWeight() + node.getRchildWeight());
		if(node.getParent() != null){
			updateTree(node.getParent());
		}
	}

	public void updateRouletteWheel(int nodeID, int nodeIndex, int degreeBefore, int degreeAfter) {
		Bucket oldBucket = buckets.get(degreeBefore);
		oldBucket.removeNodeAt(nodeIndex);
		updateTree(oldBucket.correspondingTreeNode.getParent());

		if(oldBucket.getSize() == 0)
			removeBucket(oldBucket);

		addNode(nodeID, degreeAfter);
	}

	protected void removeBucket(Bucket oldBucket) {
		numDeletes++;
		buckets.remove(oldBucket.getDegree());
		TreeNode oldNode = oldBucket.correspondingTreeNode;
		TreeNode father = oldNode.getParent();
		TreeNode sibling = null;
		if(father.getLchild() == oldNode){
			sibling = father.getRchild();
			if(father.isRoot())
				father.setLchild(null);
		}else if(father.getRchild() == oldNode){
			sibling = father.getLchild();
			if(father.isRoot())
				father.setRchild(null);
		}else
			System.out.println("Sibling is a mother fucker."); //It should not happen, just an integrity check.

		if(!father.isRoot()){
			TreeNode grandFather = father.getParent();
			if(grandFather.getLchild() == father){
				grandFather.setLchild(sibling);
			}else if(grandFather.getRchild() == father){
				grandFather.setRchild(sibling);
			}
			sibling.setParent(grandFather);
		}

		updateTree(father);
	}


	public int sampleBucket() {
		TreeNode sampleNode = root;
		do{
			long r = XOrShiftRandomGenerator.randomLong();

			//if( r < (1.0 * sampleNode.getLchildWeight() / (sampleNode.getLchildWeight() + sampleNode.getRchildWeight())))

			if( ( r % (sampleNode.getLchildWeight() + sampleNode.getRchildWeight()) ) < sampleNode.getLchildWeight())
				sampleNode = sampleNode.getLchild();
			else
				sampleNode = sampleNode.getRchild();
			BAGraphGenerator.numComparisons++;
		}while(!sampleNode.isDataNode());
		return sampleNode.getBucket().getDegree();
	}

	public Map<Integer, Bucket> getBuckets() {
		return buckets;
	}
	
	public void printDegreeDistribution(){
		for(int deg : buckets.keySet())
			System.out.println(deg + "\t" + buckets.get(deg).getWeight());
	}

	public TreeNode getRoot() {
		return root;
	}
	
	public double getHuffmanCodeWordLength(long numEdges){
		TreeMultiset<Long> weights = TreeMultiset.create();
		for(int deg : buckets.keySet())
			weights.add(buckets.get(deg).getWeight());
		
		return HuffmanCodeLenCalculator.computeHufmanLen(weights);
	}
	
	@Override
	public void connectMRandomNodeToThisNewNode(int m, int numNodes) {
		long t = System.nanoTime();
//		System.out.println("Linking nodes to node:" + numNodes);
		IntOpenHashSet allSelectedNodes = new IntOpenHashSet(m);	//m nodes to be selected

		//selecting the node
		for (int mCount=0; mCount<m; mCount++){  //selecting candidateNodes[mCount]
			boolean foundUniqueRandomNode = false;
			while(!foundUniqueRandomNode){
				int randBuckDegree = sampleBucket();
				int selectedNodePositionInBucket = random.nextInt(buckets.get(randBuckDegree).getSize());
				int selectedNodeId = buckets.get(randBuckDegree).getNodeAt(selectedNodePositionInBucket);
				
				if(!allSelectedNodes.contains(selectedNodeId)){
					allSelectedNodes.add(selectedNodeId);
					foundUniqueRandomNode = true;
					long t2 = System.nanoTime();
					updateRouletteWheel(selectedNodeId, selectedNodePositionInBucket, randBuckDegree, randBuckDegree+1);
					long moveNodeToHigherBucket_Time = System.nanoTime() - t2;
					BAGraphGenerator.maintenanceTime += moveNodeToHigherBucket_Time;
					t += moveNodeToHigherBucket_Time; //do not count this time in sampling time
				}
			}
		}

		BAGraphGenerator.samplingTime += System.nanoTime() - t;
		t = System.nanoTime();
		//updating weights
		//insert the new node in the wheel (other nodes are inserted to the wheel inside 'removeNodeAndUpdateGraph'
		addNode(numNodes,m);		
		for(int nodeID : allSelectedNodes)
			BAGraphGenerator.addEdge(nodeID, numNodes);
		BAGraphGenerator.maintenanceTime += System.nanoTime() - t;

	}

	@Override
	public void createInitNodes(int m) {
		for(int i=0; i<m; i++){
			addNode(i, 1);
			BAGraphGenerator.addEdge( i, BAGraphGenerator.m);
		}
		addNode(m, m);
	}
}
