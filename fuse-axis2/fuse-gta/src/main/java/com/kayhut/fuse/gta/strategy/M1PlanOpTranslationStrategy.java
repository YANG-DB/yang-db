package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    private static class EntityOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityOpStrategies() {
            super(
                    new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                    new SelectionTranslationStrategy(EntityOp.class)
            );
        }
    }

    private static class EntityFilterOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityFilterOpStrategies() {
            super(
                    new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                    new SelectionTranslationStrategy(EntityFilterOp.class)
            );
        }
    }

    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(new EntityOpStrategies(),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpStrategies(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}
