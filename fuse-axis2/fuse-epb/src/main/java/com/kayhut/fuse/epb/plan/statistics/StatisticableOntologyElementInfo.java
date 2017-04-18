package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.EBase;

/**
 * Created by moti on 4/2/2017.
 */
public class StatisticableOntologyElementInfo implements StatisticableQueryItemInfo {
    private EBase eBase;

    public StatisticableOntologyElementInfo(EBase eBase) {
        this.eBase = eBase;
    }

    public EBase geteBase() {
        return eBase;
    }
}
