package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.HashMap;
import java.util.Map;

public class LogicalElementFactoryProvider {
    //region Constructors
    public LogicalElementFactoryProvider() {
        this.logicalElementFactories = new HashMap<>();
        GlobalEntityFactory globalEntityFactory = new GlobalEntityFactory();
        ReferenceFactory referenceFactory = new ReferenceFactory();
        PovFactory povFactory = new PovFactory();
        FieldFactory fieldFactory = new FieldFactory();
        FileFactory fileFactory = new FileFactory();
        InsightFactory insightFactory = new InsightFactory();
        RelationFactory relationFactory = new RelationFactory();
        logicalElementFactories.put(String.format("%s.%s", ETypes.ENTITY, "global"), globalEntityFactory);
        logicalElementFactories.put(ETypes.ENTITY, povFactory);
        logicalElementFactories.put(ETypes.ENTITY_VALUE, fieldFactory);
        logicalElementFactories.put(ETypes.RELATION_VALUE, fieldFactory);
        logicalElementFactories.put(ETypes.LOGICAL_ENTITY, globalEntityFactory);
        logicalElementFactories.put(ETypes.REFERENCE, referenceFactory);
        logicalElementFactories.put(ETypes.FILE, fileFactory);
        logicalElementFactories.put(ETypes.INSIGHT, insightFactory);
        logicalElementFactories.put(ETypes.RELATION, relationFactory);
    }
    //endregion

    public ElementBaseLogical createLogicalItem(Vertex vertex) {
        ElementFactory elementFactory = logicalElementFactories.get(getFactoryKey(vertex));
        return elementFactory.createElement(vertex);
    }


    public ElementBaseLogical mergeLogicalItemWithVertex(Vertex vertex, ElementBaseLogical logicalElement) {
        ElementFactory elementFactory = logicalElementFactories.get(getFactoryKey(vertex));
        return elementFactory.mergeElement(vertex, logicalElement);
    }

    private String getFactoryKey(Vertex vertex) {
        String elementFactoryKey = vertex.label();
        VertexProperty<String> contextProperty = vertex.property(PhysicalElementProperties.CONTEXT);
        String context = (contextProperty == VertexProperty.<String>empty()) ? null : contextProperty.value();

        if (vertex.label().equals(ETypes.ENTITY) && context.equals("global")) {
            elementFactoryKey = String.format("%s.%s", elementFactoryKey, context);
        }
        return elementFactoryKey;
    }

    private Map<String, ElementFactory> logicalElementFactories;
}
