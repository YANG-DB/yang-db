package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.Tuple2;

import java.util.*;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.composite.Plan.toPattern;

/**
 * Created by Roman on 30/04/2017.
 */
public class ReverseRelationOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return ValidationContext.OK;
        }

        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationContext.OK;
        }

        Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return ValidationContext.OK;
        }

        AsgEBase<EEntityBase> previousEntityAsg = previousEntityOp.get().getAsgEbase();
        AsgEBase<Rel> relAsg = ((RelationOp)planOp).getAsgEbase();

        ValidationContext context = ValidationContext.OK;
        boolean result = areEntityAndRelationReversed(query, previousEntityAsg, relAsg);
        if(!result) {
            context = new ValidationContext(result,"Reverse:Validation failed on:"+toPattern(compositePlanOp) +"<"+opIndex+">");
        }
        return context;
    }
    //endregion

    //region Private Methods
    private <T extends PlanOp> Optional<T> getPreviousOp(CompositePlanOp compositePlanOp, int opIndex, Class<?> klass) {
        while(opIndex > 0) {
            PlanOp planOp = compositePlanOp.getOps().get(--opIndex);
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
