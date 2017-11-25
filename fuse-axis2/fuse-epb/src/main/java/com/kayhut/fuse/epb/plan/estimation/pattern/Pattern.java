package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;

import java.util.Map;

import static com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.PatternPart.*;
import static com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER;

/**
 * Created by moti on 6/1/2017.
 */
public class Pattern {
   public static GoToEntityRelationEntityPattern buildGoToPattern(Plan plan, Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        GoToEntityOp startGoTo = (GoToEntityOp) patternParts.get(GOTO_ENTITY);

        EntityOp start = (EntityOp) plan.getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEbase().geteBase().equals(startGoTo.getAsgEbase().geteBase())).
                findFirst().get();
        EntityFilterOp startFilter = (EntityFilterOp) PlanUtil.adjacentNext(plan, start).get();

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEbase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEbase());

        return new GoToEntityRelationEntityPattern(startGoTo, start, startFilter, rel, relFilter, end, endFilter);
    }

    public static EntityPattern buildEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        EntityOp start = (EntityOp) patternParts.get(ENTITY_ONLY);

        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONLY_FILTER);
        startFilter.setEntity(start.getAsgEbase());
        return new EntityPattern(start, startFilter);
    }

    public static EntityRelationEntityPattern buildEntityRelationEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        //entity one
        EntityOp start = (EntityOp) patternParts.get(ENTITY_ONE);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONE_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONE_FILTER, new EntityFilterOp());
        }

        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONE_FILTER);
        startFilter.setEntity(start.getAsgEbase());

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEbase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEbase());

        return new EntityRelationEntityPattern(start, startFilter, rel, relFilter, end, endFilter);
    }
}
