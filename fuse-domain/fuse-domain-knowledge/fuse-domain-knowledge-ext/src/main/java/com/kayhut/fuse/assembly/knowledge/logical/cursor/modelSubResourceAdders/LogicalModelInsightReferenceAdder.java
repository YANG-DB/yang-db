package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.HashMap;
import java.util.Map;


public class LogicalModelInsightReferenceAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        InsightLogical insightLogical = (InsightLogical) parent;
        ReferenceLogical referenceLogical = (ReferenceLogical) child;

        HashMap<String, ReferenceLogical> references = insightLogical.getReferences();
        String referenceId = referenceLogical.getId();
        if (!references.containsKey(referenceId)) {
            references.put(referenceId, referenceLogical);
        }
    }
}


