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

import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.epb.plan.validation.ChainedPlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 30/04/2017.
 */
public class NoRedundantRelationOpValidator implements ChainedPlanValidator.PlanOpValidator{
    //region Constructors
    public NoRedundantRelationOpValidator() {
        this.relationEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator Implementation
    @Override
    public void reset() {
        this.relationEnums.clear();
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            boolean isOk = recursiveJoinTraversal((EntityJoinOp) planOp);
            if(isOk) {
                return ValidationResult.OK;
            }
            else{
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "NoRedundant:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            }
        }

        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        if (!this.relationEnums.contains(((RelationOp) planOp).getAsgEbase().geteNum())){
            this.relationEnums.add(((RelationOp) planOp).getAsgEbase().geteNum());
            return ValidationResult.OK;
        }

        return new ValidationResult(
                false,this.getClass().getSimpleName(),
                "NoRedundant:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
    }

    private boolean recursiveJoinTraversal(EntityJoinOp planOp) {
        boolean isOk = true;
        for (PlanOp op : planOp.getLeftBranch().getOps()) {
            if(op instanceof EntityJoinOp){
                isOk &= recursiveJoinTraversal((EntityJoinOp) op);
            }
            if(op instanceof RelationOp) {
                if (!this.relationEnums.contains(((RelationOp) op).getAsgEbase().geteNum())) {
                    this.relationEnums.add(((RelationOp) op).getAsgEbase().geteNum());
                } else {
                    isOk = false;
                }
            }
        }
        for (PlanOp op : planOp.getRightBranch().getOps()) {
            if(op instanceof EntityJoinOp){
                isOk &= recursiveJoinTraversal((EntityJoinOp) op);
            }
            if(op instanceof RelationOp) {
                if (!this.relationEnums.contains(((RelationOp) op).getAsgEbase().geteNum())) {
                    this.relationEnums.add(((RelationOp) op).getAsgEbase().geteNum());
                } else {
                    isOk = false;
                }
            }
        }
        return isOk;
    }
    //endregion

    //region Fields
    private Set<Integer> relationEnums;
    //endregion
}
