package com.yangdb.fuse.epb.plan.validation.opValidator;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.epb.plan.validation.ChainedPlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * A relation carrying an aggregation filter ({@code WHERE count(r) > n}) must be traversed in its query direction.
 * The count is grouped by the edge's source entity, i.e. the entity the traversal comes from, so reversing the
 * relation would silently count the other endpoint's edges instead.
 */
public class AggregatedRelationDirectionOpValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        AsgEBase<Rel> planRel = ((RelationOp) planOp).getAsgEbase();
        Optional<AsgEBase<Rel>> queryRel = AsgQueryUtil.element(query, planRel.geteNum());
        if (!queryRel.isPresent() || !hasAggregation(queryRel.get())) {
            return ValidationResult.OK;
        }

        if (planRel.geteBase().getDir() == queryRel.get().geteBase().getDir()) {
            return ValidationResult.OK;
        }

        return new ValidationResult(false, this.getClass().getSimpleName(),
                "Aggregated relation must keep its query direction:" + compositePlanOp + "<" + opIndex + ">");
    }

    private static boolean hasAggregation(AsgEBase<Rel> rel) {
        List<AsgEBase<RelPropGroup>> groups = AsgQueryUtil.bDescendants(rel, RelPropGroup.class);
        return groups.stream().anyMatch(group -> hasAggregation(group.geteBase()));
    }

    private static boolean hasAggregation(RelPropGroup group) {
        return group.getProps().stream().anyMatch(prop -> prop.isAggregation()
                || (prop.getCon() != null && prop.getCon().isAggregation()))
                || group.getGroups().stream().anyMatch(AggregatedRelationDirectionOpValidator::hasAggregation);
    }
}
