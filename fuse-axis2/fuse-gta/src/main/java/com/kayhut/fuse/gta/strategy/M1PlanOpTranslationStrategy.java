package com.kayhut.fuse.gta.strategy;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.none),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpTranslationStrategy(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}
