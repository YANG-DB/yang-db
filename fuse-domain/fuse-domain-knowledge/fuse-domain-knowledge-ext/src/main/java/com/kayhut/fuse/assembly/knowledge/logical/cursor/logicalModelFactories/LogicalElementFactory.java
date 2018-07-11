package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalItemBase;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class LogicalElementFactory {
    //region Constructors
    public LogicalElementFactory() {
        this.entityFactory = new EntityFactory();
        this.referenceFactory = new ReferenceFactory();
    }
    //endregion

    public LogicalItemBase createLogicalItem(Vertex vertex) {
        switch (vertex.label()) {
            case ETypes.ENTITY: {
                String context = vertex.value(PhysicalElementProperties.CONTEXT);
                if (context.equals("global")) {
                    return entityFactory.createEntity(vertex);
                } else {
                    return entityFactory.createPov(vertex);
                }
            }
            case ETypes.LOGICAL_ENTITY:{
                return entityFactory.createEntity(vertex);
            }
            case ETypes.REFERENCE: {
                return this.referenceFactory.createReference(vertex);
            }
            default:
                return null;
        }
    }

    // TODO: implement
    public void putLogicalItem(Vertex vertex, LogicalItemBase itemToUpdate) {
        switch (vertex.label()) {
            case ETypes.ENTITY: {
                String context = vertex.value(PhysicalElementProperties.CONTEXT);
                if (context.equals("global")) {
                     entityFactory.createEntity(vertex);
                } else {
                     entityFactory.createPov(vertex);
                }
            }
            case ETypes.LOGICAL_ENTITY:{
                 entityFactory.createEntity(vertex);
            }
            case ETypes.REFERENCE: {
                 this.referenceFactory.createReference(vertex);
            }
        }
    }

    private EntityFactory entityFactory;
    private ReferenceFactory referenceFactory;

}
