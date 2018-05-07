package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Eprop with a boost to rank the query results according to the desired boost
 */
public class ScoreEProp extends EProp implements RankingEProp {
    //region Constructors
    public ScoreEProp(EProp eProp, long boost) {
        this(eProp.geteNum(),eProp.getpType(),eProp.getCon(),boost);
    }

    public ScoreEProp(int eNum, String pType, Constraint con, long boost) {
        super(eNum, pType, con);
        this.boost = boost;
    }

    public ScoreEProp(int eNum, String pType, Projection proj, long boost) {
        super(eNum, pType, proj);
        this.boost = boost;
    }
    //endregion
    public long getBoost() {
        return boost;
    }
    //region Properties

    //region Fields
    private long boost;
    //endregion

}
