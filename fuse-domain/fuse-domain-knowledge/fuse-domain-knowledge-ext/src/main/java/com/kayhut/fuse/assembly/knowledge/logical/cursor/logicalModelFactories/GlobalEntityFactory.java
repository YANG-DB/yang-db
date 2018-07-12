package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class GlobalEntityFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public GlobalEntityFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        VertexProperty<String> categoryProperty = vertex.property(PhysicalEntityProperties.CATEGORY);
        String category = (categoryProperty == VertexProperty.<String>empty()) ? null : categoryProperty.value();
        if (category != null) {
            return new GlobalEntityLogical(vertex.value(PhysicalEntityProperties.LOGICAL_ID), category, this.metadataFactory.createMetadata(vertex));
        }
        return new GlobalEntityLogical(
                vertex.value(PhysicalEntityProperties.LOGICAL_ID));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        // Merge only if vertex is of type entity in order to get category and metadata. If vertex is LogicalEntity, it has nothing new to add.
        if (vertex.label().equals(ETypes.ENTITY)) {
            ((GlobalEntityLogical) logicalElement).setCategory(PhysicalEntityProperties.CATEGORY);
            logicalElement.setMetadata(this.metadataFactory.createMetadata(vertex));
        }
        return logicalElement;
    }
}
