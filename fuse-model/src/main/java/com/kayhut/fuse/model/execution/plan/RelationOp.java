package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.Rel;

/**
 * Created by User on 20/02/2017.
 */
public class RelationOp extends TaggedOp {
    //region Constructors
    public RelationOp() {

    }

    public RelationOp(Rel relation) {
        super(null);
        this.relation = relation;
    }
    //endregion

    //region Properties
    public Rel getRelation() {
        return this.relation;
    }

    public void setRelation(Rel value) {
        this.relation = value;
    }
    //endregion

    //region Fields
    private Rel relation;
    //endregion
}
