package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalReferenceProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ReferenceLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class ReferenceFactory extends BaseElementFactory {
    //region Constructors
    public ReferenceFactory() {
        super();
    }

    public ReferenceFactory(Vertex vertex) {
        super();
    }

    //endregion

    public ReferenceLogical createReference(Vertex vertex) {
        return new ReferenceLogical(
                vertex.value(PhysicalReferenceProperties.TITLE),
                vertex.value(PhysicalReferenceProperties.URL),
                vertex.value(PhysicalReferenceProperties.SYSTEM),
                vertex.value(PhysicalReferenceProperties.CONTENT),
                this.createMetadata(vertex));
    }
}
