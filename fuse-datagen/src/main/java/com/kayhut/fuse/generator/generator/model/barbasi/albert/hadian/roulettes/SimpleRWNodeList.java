package com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.roulettes;

import com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.generators.BAGraphGenerator;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Simple roulette wheel, which performs linear scans
 * 
 * @author Ali Hadian
 *
 */
public class SimpleRWNodeList implements NodesList {
	public static
	Random random = new Random();
	int[] degrees;

	public SimpleRWNodeList() {
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
		//System.out.println();
		degrees[m] = m;
	}

	@Override
	public void connectMRandomNodeToThisNewNode(int m, int numNodes) {
		long t = System.nanoTime();
		ArrayList<Integer> candidateNodes = new ArrayList<Integer>();	//m nodes to be selected

		for (int mCount=0; mCount<m; mCount++){  //selecting candidateNodes[mCount]
			int selectedNode = -1;
			do{
				long randNum = ThreadLocalRandom.current().nextLong(BAGraphGenerator.numEdges * 2);

				long cumSum = 0;
				//select corresponding node
				for(int i=0; i<numNodes; i++){
					cumSum += degrees[i];
					BAGraphGenerator.numComparisons++;
					if(cumSum > randNum){
						selectedNode = i;
						break;
					}
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
		}
		BAGraphGenerator.maintenanceTime += System.nanoTime() - t;
	}
}

