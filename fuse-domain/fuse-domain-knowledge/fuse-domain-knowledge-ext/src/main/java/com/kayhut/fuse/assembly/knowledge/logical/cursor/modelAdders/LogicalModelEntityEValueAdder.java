package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;


public class LogicalModelEntityEValueAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        // TODO

        GlobalEntityLogical globalEntityLogical = (GlobalEntityLogical)parent;
        PovLogical povLogical = (PovLogical)child;

        globalEntityLogical.getPovs().add(povLogical);
    }
}