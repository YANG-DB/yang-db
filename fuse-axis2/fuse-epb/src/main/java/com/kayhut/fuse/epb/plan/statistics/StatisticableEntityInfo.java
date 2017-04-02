package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by moti on 4/2/2017.
 */
public class StatisticableEntityInfo implements StatisticableItemInfo{
    private EEntityBase entity;

    public StatisticableEntityInfo(EEntityBase entity) {
        this.entity = entity;
    }

    public EEntityBase getEntity() {
        return entity;
    }
}
