package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ReferenceLogical;

import java.util.HashMap;


public class LogicalModelPovInsightAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        PovLogical povLogical = (PovLogical) parent;
        InsightLogical insightLogical = (InsightLogical) child;

        HashMap<String, InsightLogical> insights = povLogical.getInsights();
        String insightId = insightLogical.getId();
        if(!insights.containsKey(insightId)){
            insights.put(insightId, insightLogical);
        }
    }
}


