package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityPattern extends Pattern {
    //region Constructors
    public EntityPattern(EntityOp start, EntityFilterOp startFilter) {
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
