package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.EntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalItemBase;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;


public class LogicalModelEntityPovAdder implements LogicalModelAdder {
    @Override
    public void addChild(LogicalItemBase parent, LogicalItemBase child) {
        EntityLogical entityLogical = (EntityLogical)parent;
        PovLogical povLogical = (PovLogical)child;

        entityLogical.getPovs().add(povLogical);
    }
}