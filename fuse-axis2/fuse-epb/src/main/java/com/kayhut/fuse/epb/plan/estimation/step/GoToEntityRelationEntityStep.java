package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.model.execution.plan.*;

/**
 * Created by Roman on 29/06/2017.
 */
public class GoToEntityRelationEntityStep extends EntityRelationEntityStep {
    //region Constructors
    public GoToEntityRelationEntityStep(GoToEntityOp startGoTo, EntityOp start, EntityFilterOp startFilter, RelationOp rel, RelationFilterOp relFilter, EntityOp end, EntityFilterOp endFilter) {
        super(start, startFilter, rel, relFilter, end, endFilter);
        this.startGoTo = startGoTo;
    }
    //endregion

    //region Properties
    public GoToEntityOp getStartGoTo() {
        return startGoTo;
    }
    //endregion

    //region Fields
    private GoToEntityOp startGoTo;
    //endregion
}
