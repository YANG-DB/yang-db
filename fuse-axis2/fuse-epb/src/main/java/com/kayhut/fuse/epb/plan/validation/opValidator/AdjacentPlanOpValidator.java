package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.Plan.toPattern;

/**
 * Created by Roman on 25/04/2017.
 */
public class AdjacentPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private Trace<String> trace = Trace.build(AdjacentPlanOpValidator.class.getSimpleName());

    @Override
    public void log(String event, Level level) {
        trace.log(event,level);
    }

    @Override
    public List<Tuple2<String,String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public String who() {
        return trace.who();
    }


    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            if (!(compositePlanOp.getOps().get(0) instanceof EntityOp)) {
                log("Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
                return false;
            } else
                return true;
        }

        PlanOpBase currentPlanOp = compositePlanOp.getOps().get(opIndex);
        PlanOpBase previousPlanOp = compositePlanOp.getOps().get(opIndex - 1);

        if (currentPlanOp instanceof EntityFilterOp) {
            if (!(previousPlanOp instanceof EntityOp)) {
                log("Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query, currentPlanOp.geteNum(), previousPlanOp.geteNum());
            return areFilterAndEntityAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof RelationFilterOp) {
            if (!(previousPlanOp instanceof RelationOp)) {
                log("Adjacent:Validation failed on:" + toPattern(compositePlanOp) +"<"+opIndex+">", Level.INFO);
                return false;

            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query, currentPlanOp.geteNum(), previousPlanOp.geteNum());
            return areFilterAndRelationAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof EntityOp) {
            Optional<RelationOp> previousRelationOp = getPreviousOp(compositePlanOp, opIndex, RelationOp.class);
            if (!previousRelationOp.isPresent()) {
                log("Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query, currentPlanOp.geteNum(), previousRelationOp.get().geteNum());
            return areEntityAndRelationAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof RelationOp) {
            Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
            if (!previousEntityOp.isPresent()) {
                log("Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
                return false;
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query, currentPlanOp.geteNum(), previousEntityOp.get().geteNum());
            return areEntityAndRelationAdjacent(path, compositePlanOp);
        }

        return true;
    }
    //endregion

    //region Private Methods
    private boolean areFilterAndEntityAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOpBase compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                EPropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            log("Adjacent:Validation failed on:" + toPattern(compositePlanOp), Level.INFO);
        }
        return b;

    }

    private boolean areFilterAndRelationAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOpBase compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> Rel.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                RelPropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            log("Adjacent:Validation failed on:" + toPattern(compositePlanOp), Level.INFO);
        }
        return b;
    }

    private boolean areEntityAndRelationAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOpBase compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                Rel.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            log("Adjacent:Validation failed on:" + toPattern(compositePlanOp), Level.INFO);
        }
        return b;
    }

    private <T extends PlanOpBase> Optional<T> getPreviousOp(CompositePlanOpBase compositePlanOp, int opIndex, Class<?> klass) {
        while (opIndex > 0) {
            PlanOpBase planOp = compositePlanOp.getOps().get(--opIndex);
            if (klass.isAssignableFrom(planOp.getClass())) {
                return Optional.of((T) planOp);
            }
        }

        return Optional.empty();
    }
    //endregion
}
