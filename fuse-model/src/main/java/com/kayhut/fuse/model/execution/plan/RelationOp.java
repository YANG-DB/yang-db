package com.kayhut.fuse.model.execution.plan;

/**
 * Created by User on 20/02/2017.
 */
public class RelationOp extends TaggedOp {
    //region Constructors
    public RelationOp() {

    }

    public RelationOp(String tag, String label, Direction direction) {
        super(tag);
        this.label = label;
        this.direction = direction;
    }
    //endregion

    //region Properties
    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction value) {
        this.direction = value;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    //endregion

    //region Fields
    private String label;
    private Direction direction;
    //endregion
}
