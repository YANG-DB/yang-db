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
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * A relation carrying a count filter ({@code WHERE count(r) > n}) must be traversed in its query direction. The count
 * is grouped by the edge's source entity, i.e. the entity the traversal comes from, so reversing the relation would
 * silently count the other endpoint's edges instead.
 * <p>
 * The plan search keeps only the cheapest partial plan per step, so rejecting the reversed relation op alone could
 * leave no plan at all (the search would already be committed to a seed beyond the relation). Plans that start on the
 * far (target) side of a count-filtered relation are therefore rejected from their first op.
 * <p>
 * Only count constraints pin a relation: other function props such as {@code distinct(r)} don't group by an endpoint.
 */
public class AggregatedRelationDirectionOpValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (opIndex == 0 && planOp instanceof EntityOp) {
            return validateSeed(query, compositePlanOp, ((EntityOp) planOp).getAsgEbase().geteNum());
        }
        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        AsgEBase<Rel> planRel = ((RelationOp) planOp).getAsgEbase();
        Optional<AsgEBase<Rel>> queryRel = AsgQueryUtil.element(query, planRel.geteNum());
        if (!queryRel.isPresent() || !hasCountFilter(queryRel.get())) {
            return ValidationResult.OK;
        }

        if (planRel.geteBase().getDir() == queryRel.get().geteBase().getDir()) {
            return ValidationResult.OK;
        }

        return invalid("Count-filtered relation must keep its query direction:" + compositePlanOp + "<" + opIndex + ">");
    }

    private ValidationResult validateSeed(AsgQuery query, CompositePlanOp compositePlanOp, int seedENum) {
        for (AsgEBase<Rel> rel : AsgQueryUtil.elements(query, Rel.class)) {
            if (!hasCountFilter(rel)) {
                continue;
            }
            boolean seedOnTargetSide = AsgQueryUtil.nextDescendants(rel, EEntityBase.class).stream()
                    .anyMatch(entity -> entity.geteNum() == seedENum);
            if (seedOnTargetSide) {
                return invalid("Plan starts beyond count-filtered relation " + rel + ":" + compositePlanOp);
            }
        }
        return ValidationResult.OK;
    }

    private ValidationResult invalid(String message) {
        return new ValidationResult(false, this.getClass().getSimpleName(), message);
    }

    private static boolean hasCountFilter(AsgEBase<Rel> rel) {
        List<AsgEBase<RelPropGroup>> groups = AsgQueryUtil.bDescendants(rel, RelPropGroup.class);
        return groups.stream().anyMatch(group -> hasCountFilter(group.geteBase()));
    }

    private static boolean hasCountFilter(RelPropGroup group) {
        // Constraint.isAggregation() is true exactly when a count operator is set
        return group.getProps().stream().anyMatch(prop -> prop.getCon() != null && prop.getCon().isAggregation())
                || group.getGroups().stream().anyMatch(AggregatedRelationDirectionOpValidator::hasCountFilter);
    }
}
