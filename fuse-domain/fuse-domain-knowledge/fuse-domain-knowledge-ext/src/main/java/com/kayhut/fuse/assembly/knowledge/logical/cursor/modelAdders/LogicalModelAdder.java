package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalItemBase;


public interface LogicalModelAdder {
    void addChild(LogicalItemBase parent, LogicalItemBase child);
}
