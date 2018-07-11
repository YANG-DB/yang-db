package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalElementBase;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public interface ElementFactory {
    LogicalElementBase createElement(Vertex vertex);
}
