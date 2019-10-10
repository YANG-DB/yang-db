package com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;

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

import com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiValueMap;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is a specific implementation of {@link RollBucketNodeList} in which the buckets are sorted by their weight (not by their bucket degree).
 * As shown in the ROLL paper (Fig 3), the sorting overhead is too much.
 * RollBucket roulette wheel. The nodes are stored in simple `IntArrayList` lists.
 * @author Ali Hadian
 *
 */

public class RollBucketNodeList_SORTED implements NodesList {
	public static final Random random = new Random();
	
	/**
	 * Map of all groups (buckets). KEYs = available degrees, value(key) = {node | deg(node) == key}
	 */
	private Int2ObjectRBTreeMap<IntArrayList> groups = new Int2ObjectRBTreeMap<IntArrayList>();
	MultiValueMap<Long, Integer> sortedMinusWeightsMap = MapUtils.multiValueMap(new TreeMap(), LinkedHashSet.class);

	@Override
	public void createInitNodes(int m) {
		groups.put(1, new IntArrayList());
		if(m > 1)
			groups.put(m, new IntArrayList());

		// System.out.print("+Node: \t");
		for(int i = 0; i< BAGraphGenerator.m; i++){
			groups.get(1).add(i);
			BAGraphGenerator.addEdge(i, BAGraphGenerator.m);
		}
		groups.get(m).add(m);
		sortedMinusWeightsMap.put(-1l * m, m);
		sortedMinusWeightsMap.put(-1l * m, 1);
	}

	@Override
	public void connectMRandomNodeToThisNewNode(int m, int numNodes) {
		long t = System.nanoTime();
		IntOpenHashSet allSelectedNodes = new IntOpenHashSet(m);	//m nodes to be selected

		//selecting the node
		for (int mCount=0; mCount<m; mCount++){  //selecting candidateNodes[mCount]
			//meanwhile in this loop, some nodes have already been selected and stored in allSelectedNodes. The model is also updated according to these nodes,
			//  so total weights in the roulette wheel is increased after selecting each node, therefore SUM(degrees) > (#edges*2). Therefore we should increase the Max weight in the roulette wheel to compensate it.
			long effectiveRouletteWheelTotalWeight =  BAGraphGenerator.numEdges * 2 + allSelectedNodes.size();
			boolean foundUniqueRandomNode = false;
			while(!foundUniqueRandomNode){
				long randNum = ThreadLocalRandom.current().nextLong(effectiveRouletteWheelTotalWeight);
				long cumSum = 0;
				//select corresponding node
				for (Iterator iterator = sortedMinusWeightsMap.iterator(); iterator.hasNext();){
					Entry<Long,Integer> e = (Entry<Long, Integer>) iterator.next();
					int i = e.getValue();
					cumSum += i * groups.get(i).size();					
					BAGraphGenerator.numComparisons++;
					if(cumSum > randNum){	//data is in the current bucket
						int selectedNodePositionInBucket = random.nextInt(groups.get(i).size());
						int selectedNodeId = groups.get(i).get(selectedNodePositionInBucket);
						if(!allSelectedNodes.contains(selectedNodeId)){
							allSelectedNodes.add(selectedNodeId);
							foundUniqueRandomNode = true;
							long t2 = System.nanoTime();
							moveNodeToHigherBucket(selectedNodeId, selectedNodePositionInBucket, i);
							long moveNodeToHigherBucket_Time = System.nanoTime() - t2;
							BAGraphGenerator.maintenanceTime += moveNodeToHigherBucket_Time;
							t += moveNodeToHigherBucket_Time; //do not count this time in sampling time
						}
						break;
					}
				}
			}
		}

		BAGraphGenerator.samplingTime += System.nanoTime() - t;
		t = System.nanoTime();
		//updating weights
		//insert the new node in the wheel (other nodes are inserted to the wheel inside 'removeNodeAndUpdateGraph'
		if(!groups.containsKey(m)) 
			groups.put(m, new IntArrayList());
		updateSortedMinusWeightsMap(groups.get(m).size() * m, m, m);
		groups.get(m).add(numNodes);		
		for(int nodeID : allSelectedNodes){
			BAGraphGenerator.addEdge(nodeID, numNodes);
		}
		BAGraphGenerator.maintenanceTime += System.nanoTime() - t;
	}

	private void moveNodeToHigherBucket(int selectedNodeId, int selectedNodePositionInBucket, int bucketId) {
		if(!groups.containsKey(bucketId+1)){
			groups.put(bucketId+1, new IntArrayList());
		}
		updateSortedMinusWeightsMap(groups.get(bucketId+1).size() * (bucketId+1), (bucketId+1), (bucketId+1));
		groups.get(bucketId+1).add(selectedNodeId);
		
		//removing node in the prev. bucket. (By replacing it with the last element in the list and deleting the list
		IntArrayList prevBuket = groups.get(bucketId);
		updateSortedMinusWeightsMap(groups.get(bucketId).size() * bucketId, -1 * bucketId, bucketId);
		prevBuket.set(selectedNodePositionInBucket, prevBuket.get(prevBuket.size()-1));
		prevBuket.remove(prevBuket.size()-1); 	
	}
	
	private void updateSortedMinusWeightsMap(long prevWeight, long increment, int deg){
		if(prevWeight != 0)
			if(!sortedMinusWeightsMap.removeMapping(-1l * prevWeight, deg)) 
				System.err.println("wasn't there!");
		long newWeight = prevWeight + increment;
		if(newWeight != 0)
			sortedMinusWeightsMap.put(-1l * newWeight, deg);
	}


}
