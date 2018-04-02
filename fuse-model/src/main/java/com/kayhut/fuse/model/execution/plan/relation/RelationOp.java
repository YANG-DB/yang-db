package com.kayhut.fuse.model.execution.plan.relation;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.query.Rel;

/**
 * Created by User on 20/02/2017.
 */
public class RelationOp extends AsgEBasePlanOp<Rel> {
    //region Constructors
    public RelationOp() {
        super(new AsgEBase<>());
    }

    public RelationOp(AsgEBase<Rel> relation) {
        super(relation);
    }

    public RelationOp(AsgEBase<Rel> relation, Rel.Direction direction) {
        super(new AsgEBase<>(relation.geteBase().clone()));
        getAsgEbase().geteBase().setDir(direction);
    }
    //endregion
}
