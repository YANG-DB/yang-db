package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.util.function.TriFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 25/04/2017.
 */
public class AdjacentPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return compositePlanOp.getOps().get(0) instanceof EntityOp;
        }

        PlanOpBase currentPlanOp = compositePlanOp.getOps().get(opIndex);
        PlanOpBase previousPlanOp = compositePlanOp.getOps().get(opIndex - 1);

        if (currentPlanOp instanceof EntityFilterOp) {
            if (!(previousPlanOp instanceof EntityOp)) {
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtils.findPath(query, currentPlanOp.geteNum(), previousPlanOp.geteNum());
            return areFilterAndEntityAdjacent(path);
        }

        if (currentPlanOp instanceof RelationFilterOp) {
            if (!(previousPlanOp instanceof RelationOp)) {
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtils.findPath(query, currentPlanOp.geteNum(), previousPlanOp.geteNum());
            return areFilterAndRelationAdjacent(path);
        }

        if (currentPlanOp instanceof EntityOp) {
            Optional<RelationOp> previousRelationOp = getPreviousOp(compositePlanOp, opIndex, RelationOp.class);
            if (!previousRelationOp.isPresent()) {
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtils.findPath(query, currentPlanOp.geteNum(), previousRelationOp.get().geteNum());
            return areEntityAndRelationAdjacent(path);
        }

        if (currentPlanOp instanceof RelationOp) {
            Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
            if (!previousEntityOp.isPresent()) {
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtils.findPath(query, currentPlanOp.geteNum(), previousEntityOp.get().geteNum());
            return areEntityAndRelationAdjacent(path);
        }

        return false;
    }
    //endregion

    //region Private Methods
    private boolean areFilterAndEntityAdjacent(List<AsgEBase<? extends EBase>> path) {
        return Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                EProp.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
    }

    private boolean areFilterAndRelationAdjacent(List<AsgEBase<? extends EBase>> path) {
        return Stream.ofAll(path).count(asgEBase -> Rel.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                RelProp.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
    }

    private boolean areEntityAndRelationAdjacent(List<AsgEBase<? extends EBase>> path) {
        return Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                Rel.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
    }

    private <T extends PlanOpBase> Optional<T> getPreviousOp(CompositePlanOpBase compositePlanOp, int opIndex, Class<?> klass) {
        while(opIndex > 0) {
            PlanOpBase planOp = compositePlanOp.getOps().get(--opIndex);
            if (planOp.getClass().equals(klass)) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }
    //endregion
}
