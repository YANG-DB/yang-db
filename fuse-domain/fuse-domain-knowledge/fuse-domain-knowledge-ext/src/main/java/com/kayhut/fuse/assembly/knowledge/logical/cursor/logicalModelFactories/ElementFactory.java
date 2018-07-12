package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public interface ElementFactory {
    ElementBaseLogical createElement(Vertex vertex);
    ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement);
}
