package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by moti on 7/3/2017.
 */
public class JoinOp extends EntityOp {
    private Plan leftBranch;
    private Plan rightBranch;

    public JoinOp() {
    }

    public JoinOp(Plan leftBranch, Plan rightBranch) {
        this.leftBranch = leftBranch;
        this.rightBranch = rightBranch;
    }

    public JoinOp(AsgEBase<EEntityBase> asgEBase, Plan leftBranch, Plan rightBranch) {
        super(asgEBase);
        this.leftBranch = leftBranch;
        this.rightBranch = rightBranch;
    }

    public Plan getLeftBranch() {
        return leftBranch;
    }

    public Plan getRightBranch() {
        return rightBranch;
    }
}
