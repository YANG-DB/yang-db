package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;

import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * RollBucket roulette wheel. The nodes are stored in simple `IntArrayList` lists.
 * @author Ali Hadian
 *
 */
public class RollBucketNodeList implements NodesList {
	public static final Random random = new Random();
	
	/**
	 * Map of all groups (buckets). KEYs = available degrees, value(key) = {node | deg(node) == key}
	 */
	private Int2ObjectRBTreeMap<IntArrayList> groups = new Int2ObjectRBTreeMap<IntArrayList>();

	public int getNumBuckets(){
		return groups.size();
	}

	@Override
	public void createInitNodes(int m) {
		groups.put(1, new IntArrayList());
		if(m > 1)
			groups.put(m, new IntArrayList());

		//System.out.print("+Node: \t");
		for(int i = 0; i< BAGraphGenerator.m; i++){
			groups.get(1).add(i);
			BAGraphGenerator.addEdge(i, BAGraphGenerator.m);
		}
		groups.get(m).add(m);
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
				for (int i : groups.keySet()) {
					//System.out.print(i  + " (" + groups.get(i).size() + ")\t");
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
		
		groups.get(m).add(numNodes);		
		for(int nodeID : allSelectedNodes)
			BAGraphGenerator.addEdge(nodeID, numNodes);
			//System.out.printf("(%d,%d) \t", nodeID, numNodes);
		//System.out.println();
		BAGraphGenerator.maintenanceTime += System.nanoTime() - t;
	}

	private void moveNodeToHigherBucket(int selectedNodeId, int selectedNodePositionInBucket, int bucketId) {
		if(!groups.containsKey(bucketId+1))
			groups.put(bucketId+1, new IntArrayList());
		groups.get(bucketId+1).add(selectedNodeId);
		
		//removing node in the prev. bucket. (By replacing it with the last element in the list and deleting the list
		IntArrayList prevBuket = groups.get(bucketId);
		prevBuket.set(selectedNodePositionInBucket, prevBuket.get(prevBuket.size()-1));
		prevBuket.remove(prevBuket.size()-1);		
	}

}
