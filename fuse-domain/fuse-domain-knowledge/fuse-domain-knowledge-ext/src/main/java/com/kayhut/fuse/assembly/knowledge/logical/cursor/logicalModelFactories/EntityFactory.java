package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.EntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;

public class EntityFactory extends BaseElementFactory {
    //region Constructors
    public EntityFactory() {
        super();
    }

    public EntityFactory(Vertex vertex) {
        super();
    }

    //endregion

    public EntityLogical createEntity(Vertex vertex) {
        return new EntityLogical(
                vertex.value(PhysicalEntityProperties.LOGICAL_ID));
    }

    public PovLogical createPov(Vertex vertex) {
        return new PovLogical(
                vertex.value(PhysicalElementProperties.CONTEXT),
                vertex.value(PhysicalEntityProperties.CATEGORY),
                this.createMetadata(vertex));
    }
}
