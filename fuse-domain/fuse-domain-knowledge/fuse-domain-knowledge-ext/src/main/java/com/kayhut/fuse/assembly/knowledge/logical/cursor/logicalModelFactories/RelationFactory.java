package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalInsightProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalRelationProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.RelationLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class RelationFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public RelationFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        VertexProperty<String> contextPropery = vertex.property(PhysicalElementProperties.CONTEXT);
        VertexProperty<String> categoryProperty = vertex.property(PhysicalRelationProperties.RELATION_CATEGORY);

        return new RelationLogical(
                vertex.id().toString(),
                contextPropery == (VertexProperty.<String>empty()) ? null : contextPropery.value(),
                categoryProperty == (VertexProperty.<String>empty()) ? null : categoryProperty.value(),
                this.metadataFactory.createMetadata(vertex));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        return null;
    }
}
