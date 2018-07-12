package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;


public class LogicalModelEntityPovAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        if(child instanceof GlobalEntityLogical){
            return;
        }
        GlobalEntityLogical globalEntityLogical = (GlobalEntityLogical)parent;
        PovLogical povLogical = (PovLogical)child;
        globalEntityLogical.getPovs().add(povLogical);
    }
}