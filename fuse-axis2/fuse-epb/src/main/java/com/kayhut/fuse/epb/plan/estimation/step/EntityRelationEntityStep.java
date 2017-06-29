package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.model.execution.plan.*;

import java.util.Map;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityRelationEntityStep extends Step {
    //region Constructors
    public EntityRelationEntityStep(
            EntityOp start,
            EntityFilterOp startFilter,
            RelationOp rel,
            RelationFilterOp relFilter,
            EntityOp end,
            EntityFilterOp endFilter) {
        this.start = start;
        this.startFilter = startFilter;
        this.rel = rel;
        this.relFilter = relFilter;
        this.end = end;
        this.endFilter = endFilter;
    }
    //endregion

    //region Properties
    public EntityOp getStart() {
        return start;
    }

    public EntityFilterOp getStartFilter() {
        return startFilter;
    }

    public RelationOp getRel() {
        return rel;
    }

    public RelationFilterOp getRelFilter() {
        return relFilter;
    }

    public EntityOp getEnd() {
        return end;
    }

    public EntityFilterOp getEndFilter() {
        return endFilter;
    }
    //endregion

    //region Fields
    private EntityOp start;
    private EntityFilterOp startFilter;
    private RelationOp rel;
    private RelationFilterOp relFilter;
    private EntityOp end;
    private EntityFilterOp endFilter;
    //endregion
}
