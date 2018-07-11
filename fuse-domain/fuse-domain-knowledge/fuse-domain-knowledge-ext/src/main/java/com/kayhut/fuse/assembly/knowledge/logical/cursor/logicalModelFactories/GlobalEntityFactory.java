package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.EntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalElementBase;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GlobalEntityFactory extends  ElementFactoryBase implements ElementFactory {
    //region Constructors
    public GlobalEntityFactory() {
        super();
    }

    //endregion

    @Override
    public LogicalElementBase createElement(Vertex vertex) {
        return new EntityLogical(
                vertex.value(PhysicalEntityProperties.LOGICAL_ID));
    }
}
