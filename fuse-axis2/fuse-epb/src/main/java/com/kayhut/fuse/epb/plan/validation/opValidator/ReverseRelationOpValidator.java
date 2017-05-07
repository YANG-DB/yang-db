package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.*;

/**
 * Created by Roman on 30/04/2017.
 */
public class ReverseRelationOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return true;
        }

        PlanOpBase planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return true;
        }

        Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return false;
        }

        AsgEBase<EEntityBase> previousEntityAsg = previousEntityOp.get().getAsgEBase();
        AsgEBase<Rel> relAsg = ((RelationOp)planOp).getAsgEBase();

        return areEntityAndRelationReversed(query, previousEntityAsg, relAsg);
    }
    //endregion

    //region Private Methods
    private <T extends PlanOpBase> Optional<T> getPreviousOp(CompositePlanOpBase compositePlanOp, int opIndex, Class<?> klass) {
        while(opIndex > 0) {
            PlanOpBase planOp = compositePlanOp.getOps().get(--opIndex);
            if (planOp.getClass().equals(klass)) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }

    private boolean areEntityAndRelationReversed(AsgQuery query, AsgEBase<EEntityBase> asgEntity, AsgEBase<Rel> asgRelation) {
        Set<Integer> entityAndRelationEnums = new HashSet<>(Arrays.asList(asgEntity.geteNum(), asgRelation.geteNum()));

        List<AsgEBase<EBase>> elements = AsgQueryUtils.getElements(query, asgEBase -> entityAndRelationEnums.contains(asgEBase.geteNum()));

        boolean isReversed = Rel.class.isAssignableFrom(elements.get(0).geteBase().getClass());

        return isReversed ? ((Rel)(elements.get(0).geteBase())).getDir() != asgRelation.geteBase().getDir() :
                ((Rel)(elements.get(1).geteBase())).getDir() == asgRelation.geteBase().getDir();
    }
    //endregion
}
