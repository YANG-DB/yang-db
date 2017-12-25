package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;

public class EntityJoinEntityPattern extends EntityRelationEntityPattern {
    public EntityJoinEntityPattern(EntityOp start, EntityFilterOp startFilter, RelationOp rel, RelationFilterOp relFilter, EntityOp end, EntityFilterOp endFilter, EntityJoinOp entityJoinOp) {
        super(start, startFilter, rel, relFilter, end, endFilter);
        this.entityJoinOp = entityJoinOp;
    }

    public EntityJoinOp getEntityJoinOp() {
        return entityJoinOp;
    }

    private EntityJoinOp entityJoinOp;
}
