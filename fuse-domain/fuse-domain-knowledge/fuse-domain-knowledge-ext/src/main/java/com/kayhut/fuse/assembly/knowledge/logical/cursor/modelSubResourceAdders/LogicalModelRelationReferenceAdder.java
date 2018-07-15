package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ReferenceLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.RelationLogical;

import java.util.HashMap;


public class LogicalModelRelationReferenceAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        RelationLogical relationLogical = (RelationLogical) parent;
        ReferenceLogical referenceLogical = (ReferenceLogical) child;

        HashMap<String, ReferenceLogical> references = relationLogical.getReferences();
        String referenceId = referenceLogical.getId();
        if (!references.containsKey(referenceId)) {
            references.put(referenceId, referenceLogical);
        }
    }
}


