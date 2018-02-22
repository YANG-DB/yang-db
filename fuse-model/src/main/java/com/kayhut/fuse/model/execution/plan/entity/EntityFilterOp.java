package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.Filter;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;

/**
 * Created by User on 20/02/2017.
 */
public class EntityFilterOp extends AsgEBasePlanOp<EPropGroup> implements Filter {
    //region Constructors
    public EntityFilterOp() {
        super(new AsgEBase<>());
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase) {
        super(asgEBase);
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase, AsgEBase<EEntityBase> entity) {
        super(asgEBase);
        this.entity = entity;
    }
    //endregion

    //region Properties
    public AsgEBase<EEntityBase> getEntity() {
        return entity;
    }

    public void setEntity(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }
    //endregion

    //region Fields
    private AsgEBase<EEntityBase> entity;
    //endregion
}
