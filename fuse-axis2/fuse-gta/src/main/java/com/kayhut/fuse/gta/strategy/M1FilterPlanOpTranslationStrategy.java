package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1FilterPlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    private static class EntityOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityOpStrategies() {
            super(
                    new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.filterEntity)//,
                    //new SelectionTranslationStrategy(EntityOp.class)
            );
        }
    }

    private static class EntityFilterOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityFilterOpStrategies() {
            super(
                    new EntityFilterOpTranslationStrategy()//,
                    //new SelectionTranslationStrategy(EntityFilterOp.class)
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

