package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.Set;

/**
 * Created by roman.margolis on 26/11/2017.
 */
public class OptionalCompletePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp currentPlanOp = compositePlanOp.getOps().get(opIndex);
        if (!OptionalOp.class.isAssignableFrom(currentPlanOp.getClass())) {
            return ValidationResult.OK;
        }

        if (opIndex == compositePlanOp.getOps().size() - 1) {
            return ValidationResult.OK;
        }

        if (!isOptionalOpComplete((OptionalOp)currentPlanOp, query)) {
            return new ValidationResult(
                    false,this.getClass().getSimpleName(),
                    "OptionalOpValidation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Private Methods
    private boolean isOptionalOpComplete(OptionalOp optionalOp, AsgQuery query) {
        AsgEBase<OptionalComp> optionalComp = AsgQueryUtil.element$(query, optionalOp.getAsgEbase().geteNum());

        final Set<Class<? extends EBase>> classSet = Stream.of(ETyped.class, EConcrete.class, EUntyped.class, Rel.class,
                EProp.class, EPropGroup.class, RelProp.class, RelPropGroup.class)
                .toJavaSet();

        Set<Integer> optionalEnums =
                Stream.ofAll(AsgQueryUtil.descendantBDescendants(optionalComp, asgEBase -> classSet.contains(asgEBase.geteBase().getClass()), asgEBase -> true))
                        .map(asgEbase -> asgEbase.geteBase().geteNum())
                        .toJavaSet();

        Set<Integer> optionalOpEnums = Stream.ofAll(PlanUtil.flat(optionalOp).getOps())
                .filter(planOp -> !EntityNoOp.class.isAssignableFrom(planOp.getClass()))
                .filter(planOp -> AsgEBaseContainer.class.isAssignableFrom(planOp.getClass()))
                .map(planOp -> ((AsgEBaseContainer)planOp).getAsgEbase().geteBase().geteNum())
                .toJavaSet();

        return optionalEnums.equals(optionalOpEnums);
    }
    //endregion
}
