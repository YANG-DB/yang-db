package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.*;


public class LogicalModelPovReferenceAdder implements LogicalModelAdder {
    @Override
    public void addChild(LogicalElementBase parent, LogicalElementBase child) {
        PovLogical povLogical = (PovLogical) parent;
        ReferenceLogical referenceLogical = (ReferenceLogical) child;

        povLogical.getReferences().add(referenceLogical);
    }
}


