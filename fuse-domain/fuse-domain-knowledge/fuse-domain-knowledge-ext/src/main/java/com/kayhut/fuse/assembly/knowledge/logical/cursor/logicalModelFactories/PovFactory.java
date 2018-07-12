package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class PovFactory extends ElementFactoryBase implements ElementFactory{
    //region Constructors
    public PovFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        return new PovLogical(
                vertex.value(PhysicalElementProperties.CONTEXT),
                vertex.value(PhysicalEntityProperties.CATEGORY),
                this.metadataFactory.createMetadata(vertex));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        return null;
    }
}
