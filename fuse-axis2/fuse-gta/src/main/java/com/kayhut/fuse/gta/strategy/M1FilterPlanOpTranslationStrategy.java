package com.kayhut.fuse.gta.strategy;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1FilterPlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1FilterPlanOpTranslationStrategy() {
        super(new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.filterEntity),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpTranslationStrategy(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}

