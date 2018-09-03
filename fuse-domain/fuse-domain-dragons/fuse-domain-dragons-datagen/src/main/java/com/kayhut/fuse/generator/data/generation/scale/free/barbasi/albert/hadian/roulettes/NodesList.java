package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;

/**
 * The general interface for roulette wheel selection for generating Barabasi-Albert Graphs.
 * @author Ali Hadian
 *
 */
public interface NodesList {
	
	/**
	 * When a node is created, this function connects the newly connected node (id=numNodes) to m distinct nodes in the model
	 * @param m
	 * @param numNodes
	 */
	void connectMRandomNodeToThisNewNode(int m, int numNodes);
	
	/**
	 * adds (m_0 + 1) nodes and connects the last m_0 nodes to the first nodes. 
	 * @param m
	 */
	void createInitNodes(int m);
}