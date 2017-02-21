package com.kayhut.fuse.model.execution.plan;

/**
 * Created by User on 20/02/2017.
 */
public class VertexGroupByOp extends PlanOp {
    //region Constructor
    public VertexGroupByOp() {

    }

    public VertexGroupByOp(String name, String vertexTag) {
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
