package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;


public interface LogicalModelAdder {
    void addChild(ElementBaseLogical parent, ElementBaseLogical child);
}
