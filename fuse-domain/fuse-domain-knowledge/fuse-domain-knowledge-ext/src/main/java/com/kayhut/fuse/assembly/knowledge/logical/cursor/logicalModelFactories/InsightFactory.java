package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalInsightProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalReferenceProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ReferenceLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class InsightFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public InsightFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        VertexProperty<String> contextPropery = vertex.property(PhysicalElementProperties.CONTEXT);
        VertexProperty<String> contentProperty = vertex.property(PhysicalInsightProperties.CONTENT);

        return new InsightLogical(
                vertex.id().toString(),
                contextPropery == (VertexProperty.<String>empty()) ? null : contextPropery.value(),
                contentProperty == (VertexProperty.<String>empty()) ? null : contentProperty.value(),
                this.metadataFactory.createMetadata(vertex));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        return null;
    }
}
