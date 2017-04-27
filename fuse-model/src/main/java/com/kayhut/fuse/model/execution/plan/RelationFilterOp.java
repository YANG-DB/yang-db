package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

/**
 * Created by User on 22/02/2017.
 */
public class RelationFilterOp extends PlanOpBase implements Filter{
    //region Constructors
    public RelationFilterOp() {

    }

    public RelationFilterOp(AsgEBase<RelPropGroup> relProp) {
        this.relProp = relProp;
    }
    //endregion

    //region Properties

    public AsgEBase<RelPropGroup> getRelProp() {
        return relProp;
    }

    public void setRelProp(AsgEBase<RelPropGroup> relProp) {
        this.relProp = relProp;
    }
    //endregion

    //region Methods

    @Override
    public int geteNum() {
        return this.relProp.geteNum();
    }

    public AsgEBase<Rel> getRel() {
        return rel;
    }

    public void setRel(AsgEBase<Rel> rel) {
        this.rel = rel;
    }
//endregion

    //region Fields
    private AsgEBase<RelPropGroup> relProp;
    private AsgEBase<Rel> rel;
    //endregion
}
