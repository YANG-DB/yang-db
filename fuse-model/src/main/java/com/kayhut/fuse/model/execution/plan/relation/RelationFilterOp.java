package com.kayhut.fuse.model.execution.plan.relation;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.Filter;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

/**
 * Created by User on 22/02/2017.
 */
public class RelationFilterOp extends AsgEBasePlanOp<RelPropGroup> implements Filter {
    //region Constructors
    public RelationFilterOp() {
        super(new AsgEBase<>());
    }

    public RelationFilterOp(AsgEBase<RelPropGroup> relPropGroup) {
        super(relPropGroup);
    }
    //endregion

    //region Properties
    public AsgEBase<Rel> getRel() {
        return rel;
    }

    public void setRel(AsgEBase<Rel> rel) {
        this.rel = rel;
    }
    //endregion

    //region Fields
    private AsgEBase<Rel> rel;
    //endregion
}
