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


import com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes.Bucket;

/**
 * ROLL-tree with reduced insertions. Plz refer to the ROLL paper, under section 4.2.2 -> "Decreasing tree operations"
 * @author Ali Hadian
 *
 */
public class RollTreeNodeList_WithReducedInsertions extends RollTreeNodeList implements NodesList {
	@Override
	public void updateRouletteWheel(int nodeID, int nodeIndex, int degreeBefore, int degreeAfter) {
		Bucket oldBucket = buckets.get(degreeBefore);
		
		//*******************************************************************************
		if(oldBucket.getSize() == 1 && !(buckets.containsKey(degreeAfter))){
			// If the old bucket contains a single element AND there is no new bucket, then instead of
			// removing B_{old} from the tree and inserting B_{new}, we simply rename its index from B_{old} to B_{new}
			oldBucket.setDegree(degreeAfter);
			buckets.remove(degreeBefore);
			buckets.put(degreeAfter, oldBucket);
			updateTree(oldBucket.correspondingTreeNode.getParent());
    	//*******************************************************************************
		}else{
			oldBucket.removeNodeAt(nodeIndex);
			updateTree(oldBucket.correspondingTreeNode.getParent());

			if(oldBucket.getSize() == 0)
				removeBucket(oldBucket);
			addNode(nodeID, degreeAfter);
		}
	}

}
