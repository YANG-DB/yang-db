package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;

import java.util.HashMap;


public class LogicalModelInsightGlobalEntityAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        GlobalEntityLogical globalEntityLogical = (GlobalEntityLogical) parent;
        InsightLogical insightLogical = (InsightLogical) child;

        HashMap<String, GlobalEntityLogical> entities = insightLogical.getEntities();
        String entityId = globalEntityLogical.getId();
        if (!entities.containsKey(entityId)) {
            entities.put(entityId, globalEntityLogical);
        }
    }
}


