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
    private static class EntityOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityOpStrategies() {
            super(
                    new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                    new SelectionTranslationStrategy(EntityOp.class)
            );
        }
    }

    private static class EntityFilterOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityFilterOpStrategies() {
            super(
                    new EntityFilterOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                    new SelectionTranslationStrategy(EntityFilterOp.class)
            );
        }
    }

    //region Constructors
    public M1FilterPlanOpTranslationStrategy() {
        super(new EntityOpStrategies(),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpStrategies(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}

