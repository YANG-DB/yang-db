package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalElementBase;


public interface LogicalModelAdder {
    void addChild(LogicalElementBase parent, LogicalElementBase child);
}
