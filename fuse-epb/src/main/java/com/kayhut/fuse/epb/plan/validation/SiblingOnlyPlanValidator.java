package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.queryAsg.AsgQuery;

/**
 * Created by moti on 3/1/2017.
 */
public class SiblingOnlyPlanValidator implements PlanValidator<Plan, AsgQuery>{


    @Override
    public boolean isPlanValid(Plan plan, AsgQuery query) {
        return false;
    }
}
