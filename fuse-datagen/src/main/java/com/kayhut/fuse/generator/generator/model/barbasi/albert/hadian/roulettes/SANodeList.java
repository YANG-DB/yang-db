package com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.roulettes;

import com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.generators.BAGraphGenerator;

import java.util.ArrayList;
import java.util.Random;


/**
 * Compared method: Roulette wheel using stochastic acceptance (Lipowski & Lipowska, 2012)
 * @author Ali Hadian
 *
 */
public class SANodeList implements NodesList {
	Random random = new Random();
	int[] degrees;
	int maxDeg;

	public SANodeList() {
		this.degrees = new int[BAGraphGenerator.numNodesFinal];
	}

	@Override
	public void createInitNodes(int m) {	
		//System.out.print("+Node: \t");
		for(int i=0; i<m; i++){
			degrees[i] = 1;
			BAGraphGenerator.addEdge(i, m);
			//System.out.printf("(%d,%d)\t", i, m);
		}
		//		System.out.println();
		degrees[m] = m;
		maxDeg = m;
	}

	@Override
	public void connectMRandomNodeToThisNewNode(int m, int numNodes) {
		long t = System.nanoTime();
		ArrayList<Integer> candidateNodes = new ArrayList<Integer>();	//m nodes to be selected

		for (int mCount=0; mCount<m; mCount++){  //selecting candidateNodes[mCount]
			int selectedNode = -1;
			do{
				while(true){
					selectedNode = random.nextInt(numNodes);
					BAGraphGenerator.numComparisons++;
					if(random.nextDouble() < ((double) degrees[selectedNode]) / maxDeg)
						break;
				}
			}while(candidateNodes.contains(selectedNode));	//no double-links
			candidateNodes.add(selectedNode);
		}
		
		BAGraphGenerator.samplingTime += System.nanoTime() - t;
		t = System.nanoTime();
		
		degrees[numNodes] += m;	//degree of the current node
		for(int nodeID : candidateNodes){
			BAGraphGenerator.addEdge(nodeID, numNodes);
			//System.out.printf("(%d,%d) \t", nodeID, numNodes);
			degrees[nodeID]++;
			maxDeg = Math.max(degrees[nodeID],maxDeg); 
		}
		
		BAGraphGenerator.maintenanceTime += System.nanoTime() - t;
	}
}

