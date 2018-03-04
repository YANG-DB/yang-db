package com.kayhut.fuse.gta.strategy.promise;

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1FilterPlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1FilterPlanOpTranslationStrategy() {
        super(
                new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}

