package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 20/02/2017.
 */
public class EntityGroupByOp extends PlanOpBase {
    //region Constructor
    public EntityGroupByOp() {

    }

    public EntityGroupByOp(String name, String vertexTag, AsgEBase<AggBase> agg) {
        this.name = name;
        this.vertexTag = vertexTag;
        this.agg = agg;
    }
    //endregion

    //region Properties
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getVertexTag() {
        return this.vertexTag;
    }

    public void setVertexTag(String value) {
        this.vertexTag = value;
    }

    public AsgEBase<AggBase> getAgg() {
        return agg;
    }

    public void setAgg(AsgEBase<AggBase> agg) {
        this.agg = agg;
    }

    //endregion

    //region Methods

    @Override
    public int geteNum() {
        return this.agg.geteNum();
    }

    //endregion

    //region Fields
    private String vertexTag;
    private String name;
    private AsgEBase<AggBase> agg;
    //endregion
}
