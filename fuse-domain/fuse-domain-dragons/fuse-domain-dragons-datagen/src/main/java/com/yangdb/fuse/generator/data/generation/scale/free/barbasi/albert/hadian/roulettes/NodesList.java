package com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;

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
