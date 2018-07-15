package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.RelationLogical;

import java.util.HashMap;


public class LogicalModelPovRelationAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        PovLogical povLogical = (PovLogical) parent;
        RelationLogical relationlogical = (RelationLogical) child;

        HashMap<String, RelationLogical> relations = povLogical.getRelations();
        String relationId = relationlogical.getId();
        if (!relations.containsKey(relationId)) {
            relations.put(relationId, relationlogical);
        }
    }
}


