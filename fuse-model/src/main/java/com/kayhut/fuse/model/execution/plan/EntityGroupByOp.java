package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 20/02/2017.
 */
public class EntityGroupByOp extends PlanOpBase {
    //region Constructor
    public EntityGroupByOp() {

    }

    public EntityGroupByOp(String name, String vertexTag, AggBase agg) {
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

    public AggBase getAgg() {
        return this.agg;
    }

    public void setAgg(AggBase value) {
        this.agg = value;
    }
    //endregion

    //region Fields
    private String vertexTag;
    private String name;
    private AggBase agg;
    //endregion
}
