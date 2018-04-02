package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 20/02/2017.
 */
public class EntityGroupByOp extends AsgEBasePlanOp<AggBase> {
    //region Constructor
    public EntityGroupByOp() {
        super(new AsgEBase<>());
    }

    public EntityGroupByOp(String name, String vertexTag, AsgEBase<AggBase> agg) {
        super(agg);
        this.name = name;
        this.vertexTag = vertexTag;
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

    //endregion

    //region Fields
    private String vertexTag;
    private String name;
    //endregion
}
