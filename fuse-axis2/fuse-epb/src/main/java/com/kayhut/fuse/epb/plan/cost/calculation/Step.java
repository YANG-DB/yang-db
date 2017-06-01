package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.model.execution.plan.*;
import javaslang.Tuple2;

import java.util.Map;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.*;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_TWO_FILTER;

/**
 * Created by moti on 6/1/2017.
 */
public final class Step {
    private EntityOp start;
    private EntityFilterOp startFilter;
    private RelationOp rel;
    private RelationFilterOp relFilter;
    private EntityOp end;
    private EntityFilterOp endFilter;

    private Step() {}

    public static Step buildGoToStep(Plan plan, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts) {
        Step step = new Step();

        GoToEntityOp gotoOp = (GoToEntityOp) patternParts.get(GOTO_ENTITY);

        step.start = (EntityOp) plan.getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEBase().geteBase().equals(gotoOp.getAsgEBase().geteBase())).
                findFirst().get();
        step.startFilter = (EntityFilterOp) PlanUtil.adjacentNext(plan, step.start).get();

        //relation
        step.rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        step.relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        //set entity type on this kaka
        step.relFilter.setRel(step.rel.getAsgEBase());

        //entity
        step.end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        step.endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        //set entity type on this kaka
        step.endFilter.setEntity(step.end.getAsgEBase());

        return step;
    }

    public static Step buildFullStep(Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts) {
        Step step = new Step();
        //entity one
        step.start = (EntityOp) patternParts.get(ENTITY_ONE);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONE_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONE_FILTER, new EntityFilterOp());
        }

        step.startFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONE_FILTER);
        //set entity type on this kaka
        step.startFilter.setEntity(step.start.getAsgEBase());

        //relation
        step.rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        step.relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        //set entity type on this kaka
        step.relFilter.setRel(step.rel.getAsgEBase());

        //entity
        step.end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        step.endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        //set entity type on this kaka
        step.endFilter.setEntity(step.end.getAsgEBase());
        return step;
    }


    public Tuple2<EntityOp,EntityFilterOp> start() {
        return new Tuple2<>(start,startFilter);
    }

    public Tuple2<RelationOp,RelationFilterOp> rel() {
        return new Tuple2<>(rel,relFilter);
    }

    public Tuple2<EntityOp,EntityFilterOp> end() {
        return new Tuple2<>(end,endFilter);
    }



}
