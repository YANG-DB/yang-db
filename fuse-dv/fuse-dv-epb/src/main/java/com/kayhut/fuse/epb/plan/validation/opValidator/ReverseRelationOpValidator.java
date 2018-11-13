package com.kayhut.fuse.epb.plan.validation.opValidator;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
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
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return ValidationResult.OK;
        }

        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return ValidationResult.OK;
        }

        AsgEBase<EEntityBase> previousEntityAsg = previousEntityOp.get().getAsgEbase();
        AsgEBase<Rel> relAsg = ((RelationOp)planOp).getAsgEbase();

        ValidationResult context = ValidationResult.OK;
        boolean result = areEntityAndRelationReversed(query, previousEntityAsg, relAsg);
        if(!result) {
            context = new ValidationResult(
                    result,this.getClass().getSimpleName(),
                    "Reverse:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
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
