package com.kayhut.fuse.model.execution.plan.costs;

/*-
 * #%L
 * JoinCost.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

import java.util.Stack;

/**
 * Created by mordechaic on 11/14/2017.
 */
public class JoinCost extends CountEstimatesCost {
    public JoinCost(double cost, double countEstimate, PlanDetailedCost leftBranchCost, PlanDetailedCost rightBranchCost) {
        super(cost, countEstimate);
        this.leftBranchCost = leftBranchCost;
        this.rightBranchCost = rightBranchCost;
    }

    public JoinCost(double cost, Stack<Double> countEstimates, PlanDetailedCost leftBranchCost, PlanDetailedCost rightBranchCost) {
        super(cost, countEstimates);
        this.leftBranchCost = leftBranchCost;
        this.rightBranchCost = rightBranchCost;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new JoinCost(this.getCost(), (Stack<Double>) this.getCountEstimates().clone(), this.getLeftBranchCost(), this.getRightBranchCost());
    }

    @Override
    public void applyCountsUpdateFactor(double countUpdateFactor){
        super.applyCountsUpdateFactor(countUpdateFactor);
        applyBranchCUF(countUpdateFactor, this.leftBranchCost);
        applyBranchCUF(countUpdateFactor, this.rightBranchCost);
    }

    public void applyCountsUpdateFactorOnLeftBranch(double countUpdateFactor){
        applyBranchCUF(countUpdateFactor, this.leftBranchCost);
    }

    public void applyCountsUpdateFactorOnRightBranch(double countUpdateFactor){
        applyBranchCUF(countUpdateFactor, this.rightBranchCost);
    }

    private void applyBranchCUF(double countUpdateFactor, PlanDetailedCost branch){
        branch.getPlanStepCosts().forEach(op -> {
            if(op.getPlan().getOps().get(0) instanceof EntityOp){
                op.getCost().applyCountsUpdateFactor(countUpdateFactor);
            }
        });
    }

    public PlanDetailedCost getLeftBranchCost() {
        return leftBranchCost;
    }

    public PlanDetailedCost getRightBranchCost() {
        return rightBranchCost;
    }

    private PlanDetailedCost leftBranchCost;
    private PlanDetailedCost rightBranchCost;

}
