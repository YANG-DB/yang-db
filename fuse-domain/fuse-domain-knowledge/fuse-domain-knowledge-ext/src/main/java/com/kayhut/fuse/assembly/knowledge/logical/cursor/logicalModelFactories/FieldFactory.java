package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEValueProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.FieldLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ValueLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Set;

public class FieldFactory extends ElementFactoryBase implements ElementFactory {
    //region Constructors
    public FieldFactory() {
        super();
    }

    //endregion

    @Override
    public ElementBaseLogical createElement(Vertex vertex) {
        ValueLogical valueLogical = new ValueLogical(vertex.id().toString(), getValueContent(vertex), this.metadataFactory.createMetadata(vertex));
        String fieldType = getFieldType(vertex);
        VertexProperty<String> bdtProperty = vertex.property(PhysicalEValueProperties.BDT);
        return new FieldLogical(
                vertex.value(PhysicalEValueProperties.FIELD_ID),
                fieldType,
                (bdtProperty == VertexProperty.<String>empty()) ? null : bdtProperty.value(),
                valueLogical);
    }

    private String getFieldType(Vertex vertex) {
        VertexProperty<String> stringProperty = vertex.property(PhysicalEValueProperties.STRING_VALUE);
        VertexProperty<String> intProperty = vertex.property(PhysicalEValueProperties.INT_VALUE);

        return !(stringProperty == VertexProperty.<String>empty()) ? "string" :
                !(intProperty == VertexProperty.<String>empty()) ? "integer" : "date";
    }

    private String getValueContent(Vertex vertex) {
        VertexProperty<String> stringProperty = vertex.property(PhysicalEValueProperties.STRING_VALUE);
        VertexProperty<String> intProperty = vertex.property(PhysicalEValueProperties.INT_VALUE);
        VertexProperty<String> dateProperty = vertex.property(PhysicalEValueProperties.DATE_VALUE);

        return !(stringProperty == VertexProperty.<String>empty()) ? stringProperty.value() :
                !(intProperty == VertexProperty.<String>empty()) ? intProperty.value() : dateProperty.value();
    }

    @Override
    public ElementBaseLogical mergeElement(Vertex vertex, ElementBaseLogical logicalElement) {
        ValueLogical valueLogical = new ValueLogical(vertex.id().toString(), getValueContent(vertex), this.metadataFactory.createMetadata(vertex));
        ((FieldLogical) logicalElement).addValue(valueLogical);
        return null;
    }
}
