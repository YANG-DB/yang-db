package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalElementBase;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.HashMap;
import java.util.Map;

public class LogicalElementFactory {
    //region Constructors
    public LogicalElementFactory() {
        this.logicalElementFactories = new HashMap<>();
        GlobalEntityFactory globalEntityFactory = new GlobalEntityFactory();
        ReferenceFactory referenceFactory = new ReferenceFactory();
        PovFactory povFactory = new PovFactory();
        logicalElementFactories.put(String.format("%s.%s", ETypes.ENTITY, "global"), globalEntityFactory);
        logicalElementFactories.put(ETypes.ENTITY, povFactory);
        logicalElementFactories.put(ETypes.LOGICAL_ENTITY, globalEntityFactory);
        logicalElementFactories.put(ETypes.REFERENCE, referenceFactory);
    }
    //endregion

    public LogicalElementBase createLogicalItem(Vertex vertex) {
        String elementFactoryKey = vertex.label();
        VertexProperty<String> contextProperty = vertex.property(PhysicalElementProperties.CONTEXT);
        String context = (contextProperty == VertexProperty.<String>empty()) ? null : contextProperty.value();

        if (context != null && context.equals("global")) {
            elementFactoryKey = String.format("%s.%s", elementFactoryKey, context);
        }

        ElementFactory elementFactory = logicalElementFactories.get(elementFactoryKey);
        return elementFactory.createElement(vertex);


//        switch (vertex.label()) {
//            case ETypes.ENTITY: {
//                String context = vertex.value(PhysicalElementProperties.CONTEXT);
//                if (context.equals("global")) {
//                    return globalEntityFactory.createEntity(vertex);
//                } else {
//                    return povFactory.createPov(vertex);
//                }
//            }
//            case ETypes.LOGICAL_ENTITY: {
//                return globalEntityFactory.createEntity(vertex);
//            }
//            case ETypes.REFERENCE: {
//                return this.referenceFactory.createReference(vertex);
//            }
//            default:
//                return null;
//        }
    }


    public LogicalElementBase mergeLogicalItemWithVertex(Vertex vertex, LogicalElementBase logicalItemBase) {
        return null;
    }

    private Map<String, ElementFactory> logicalElementFactories;
}
