package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.Rel;

/**
 * Created by User on 20/02/2017.
 */
public class RelationOp extends TaggedOp {
    //region Constructors
    public RelationOp() {

    }

    public RelationOp(AsgEBase<Rel> relation) {
        super(null);
        this.relation = relation;
    }
    //endregion

    //region Properties

    public AsgEBase<Rel> getRelation() {
        return relation;
    }

    public void setRelation(AsgEBase<Rel> relation) {
        this.relation = relation;
    }

    //endregion

    //region Methods

    @Override
    public int geteNum() {
        return this.relation.geteNum();
    }

    //endregion

    //region Fields
    private AsgEBase<Rel> relation;
    //endregion
}
