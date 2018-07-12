package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalReferenceProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ReferenceLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class ReferenceFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public ReferenceFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        VertexProperty<String> titleProperty = vertex.property(PhysicalReferenceProperties.TITLE);
        VertexProperty<String> urlProperty = vertex.property(PhysicalReferenceProperties.URL);
        VertexProperty<String> systemProperty = vertex.property(PhysicalReferenceProperties.SYSTEM);
        VertexProperty<String> contentProperty = vertex.property(PhysicalReferenceProperties.CONTENT);

        return new ReferenceLogical(
                titleProperty == (VertexProperty.<String>empty()) ? null : titleProperty.value(),
                urlProperty == (VertexProperty.<String>empty()) ? null : urlProperty.value(),
                systemProperty == (VertexProperty.<String>empty()) ? null : systemProperty.value(),
                contentProperty == (VertexProperty.<String>empty()) ? null : contentProperty.value(),
                this.metadataFactory.createMetadata(vertex));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        return null;
    }
}
