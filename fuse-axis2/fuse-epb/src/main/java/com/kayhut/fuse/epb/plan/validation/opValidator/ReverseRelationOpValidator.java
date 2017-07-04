package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.Tuple2;

import java.util.*;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.Plan.toPattern;

/**
 * Created by Roman on 30/04/2017.
 */
public class ReverseRelationOpValidator implements ChainedPlanValidator.PlanOpValidator {

    private Trace<String> trace = Trace.build(ReverseRelationOpValidator.class.getSimpleName());

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

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

    @Override
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return ValidationContext.OK;
        }

        PlanOpBase planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationContext.OK;
        }

        Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return ValidationContext.OK;
        }

        AsgEBase<EEntityBase> previousEntityAsg = previousEntityOp.get().getAsgEBase();
        AsgEBase<Rel> relAsg = ((RelationOp)planOp).getAsgEBase();

        ValidationContext context = ValidationContext.OK;
        boolean result = areEntityAndRelationReversed(query, previousEntityAsg, relAsg);
        if(!result) {
            log("Reverse:Validation failed on:"+toPattern(compositePlanOp) +"<"+opIndex+">", Level.INFO);
            context = new ValidationContext(result,"Reverse:Validation failed on:"+toPattern(compositePlanOp) +"<"+opIndex+">");
        }
        return context;
    }
    //endregion

    //region Private Methods
    private <T extends PlanOpBase> Optional<T> getPreviousOp(CompositePlanOpBase compositePlanOp, int opIndex, Class<?> klass) {
        while(opIndex > 0) {
            PlanOpBase planOp = compositePlanOp.getOps().get(--opIndex);
            if (klass.isAssignableFrom(planOp.getClass())) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }

    private boolean areEntityAndRelationReversed(AsgQuery query, AsgEBase<EEntityBase> asgEntity, AsgEBase<Rel> asgRelation) {
        Set<Integer> entityAndRelationEnums = new HashSet<>(Arrays.asList(asgEntity.geteNum(), asgRelation.geteNum()));

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> entityAndRelationEnums.contains(asgEBase.geteNum()));

        boolean isReversed = Rel.class.isAssignableFrom(elements.get(0).geteBase().getClass());

        return isReversed ? ((Rel)(elements.get(0).geteBase())).getDir() != asgRelation.geteBase().getDir() :
                ((Rel)(elements.get(1).geteBase())).getDir() == asgRelation.geteBase().getDir();
    }
    //endregion
}
