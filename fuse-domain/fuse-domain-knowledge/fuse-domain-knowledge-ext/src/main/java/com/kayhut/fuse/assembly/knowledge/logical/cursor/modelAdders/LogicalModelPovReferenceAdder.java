package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.HashMap;


public class LogicalModelPovReferenceAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        PovLogical povLogical = (PovLogical) parent;
        ReferenceLogical referenceLogical = (ReferenceLogical) child;

        HashMap<String, ReferenceLogical> references = povLogical.getReferences();
        String referenceId = referenceLogical.getId();
        if(!references.containsKey(referenceId)){
            references.put(referenceId, referenceLogical);
        }
    }
}


