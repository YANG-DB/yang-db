package com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes;


import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.roulettes.rolltree.datatypes.Bucket;

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
