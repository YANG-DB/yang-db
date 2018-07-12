package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;


public interface LogicalModelAdder {
    void addChild(ElementBaseLogical parent, ElementBaseLogical child);
}
