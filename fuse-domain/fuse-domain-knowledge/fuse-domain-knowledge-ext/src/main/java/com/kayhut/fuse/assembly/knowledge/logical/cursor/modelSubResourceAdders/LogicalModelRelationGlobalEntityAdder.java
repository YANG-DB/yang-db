package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.RelationLogical;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.HashMap;


public class LogicalModelRelationGlobalEntityAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        GlobalEntityLogical globalEntityLogical = (GlobalEntityLogical) parent;
        RelationLogical relationLogical = (RelationLogical) child;
    //TODO
//        if(relationLogical.getEntityA().getId().equals(globalEntityLogical.getId())){
//            if(relationLogical.getEntityA().)
//        }
    }
}


