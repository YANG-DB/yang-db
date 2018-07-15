package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalFileProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalInsightProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.FileLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class FileFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public FileFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        VertexProperty<String> pathPropery = vertex.property(PhysicalFileProperties.PATH);
        VertexProperty<String> displayNamePropery = vertex.property(PhysicalFileProperties.DISPLAY_NAME);
        VertexProperty<String> mimeTypePropery = vertex.property(PhysicalFileProperties.MIME_TYPE);
        VertexProperty<String> categoryPropery = vertex.property(PhysicalFileProperties.CATEGORY);
        VertexProperty<String> descriptionPropery = vertex.property(PhysicalFileProperties.DESCRIPTION);


        return new FileLogical(
                vertex.id().toString(),
                pathPropery == (VertexProperty.<String>empty()) ? null : pathPropery.value(),
                displayNamePropery == (VertexProperty.<String>empty()) ? null : displayNamePropery.value(),
                mimeTypePropery == (VertexProperty.<String>empty()) ? null : mimeTypePropery.value(),
                categoryPropery == (VertexProperty.<String>empty()) ? null : categoryPropery.value(),
                descriptionPropery == (VertexProperty.<String>empty()) ? null : descriptionPropery.value(),
                this.metadataFactory.createMetadata(vertex));
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        return null;
    }
}
