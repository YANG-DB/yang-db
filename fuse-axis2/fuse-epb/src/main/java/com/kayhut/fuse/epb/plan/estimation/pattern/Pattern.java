package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.*;

import java.util.Map;

import static com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.PatternPart.*;
import static com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER;

/**
 * Created by moti on 6/1/2017.
 */
public class Pattern {
   public static GoToEntityRelationEntityPattern buildGoToPattern(Plan plan, Map<RegexPatternCostEstimator.PatternPart, PlanOpBase> patternParts) {
        GoToEntityOp startGoTo = (GoToEntityOp) patternParts.get(GOTO_ENTITY);

        EntityOp start = (EntityOp) plan.getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEBase().geteBase().equals(startGoTo.getAsgEBase().geteBase())).
                findFirst().get();
        EntityFilterOp startFilter = (EntityFilterOp) PlanUtil.adjacentNext(plan, start).get();

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEBase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEBase());

        return new GoToEntityRelationEntityPattern(startGoTo, start, startFilter, rel, relFilter, end, endFilter);
    }

    public static EntityPattern buildEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOpBase> patternParts) {
        EntityOp start = (EntityOp) patternParts.get(ENTITY_ONLY);

        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONLY_FILTER);
        startFilter.setEntity(start.getAsgEBase());
        return new EntityPattern(start, startFilter);
    }

    public static EntityRelationEntityPattern buildEntityRelationEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOpBase> patternParts) {
        //entity one
        EntityOp start = (EntityOp) patternParts.get(ENTITY_ONE);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONE_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONE_FILTER, new EntityFilterOp());
        }

        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONE_FILTER);
        startFilter.setEntity(start.getAsgEBase());

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RELATION);

        if (!patternParts.containsKey(OPTIONAL_REL_FILTER)) {
            patternParts.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEBase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(ENTITY_TWO);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEBase());

        return new EntityRelationEntityPattern(start, startFilter, rel, relFilter, end, endFilter);
    }

    public static EntityJoinPattern buildEntityJoinPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOpBase> patternParts) {
        EntityJoinOp entityJoinOp = (EntityJoinOp) patternParts.get(JOIN);
        return new EntityJoinPattern(entityJoinOp);
    }
}
