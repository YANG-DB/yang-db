package com.kayhut.fuse.model.execution.plan;

/**
 * Created by mordechaic on 11/14/2017.
 */
public class JoinOp extends EntityOp {

    public JoinOp(Plan leftBranch, Plan rightBranch) {
        this.leftBranch = leftBranch;
        this.rightBranch = rightBranch;
    }

    public Plan getLeftBranch() {
        return leftBranch;
    }

    public Plan getRightBranch() {
        return rightBranch;
    }

    private Plan leftBranch;
    private Plan rightBranch;
}
