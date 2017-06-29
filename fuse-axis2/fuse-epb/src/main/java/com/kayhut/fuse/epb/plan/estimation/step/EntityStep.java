package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityStep extends Step {
    //region Constructors
    public EntityStep(EntityOp start, EntityFilterOp startFilter) {
        this.start = start;
        this.startFilter = startFilter;
    }
    //endregion

    //region Properties
    public EntityOp getStart() {
        return start;
    }

    public EntityFilterOp getStartFilter() {
        return startFilter;
    }
    //endregion

    //region Fields
    private EntityOp start;
    private EntityFilterOp startFilter;
    //endregion
}
