package com.yangdb.fuse.model.execution.plan;

/*-
 * #%L
 * PlanWithCost.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.model.descriptors.ToStringDescriptor;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.Collections;

/**
 * Created by Roman on 20/04/2017.
 */

public class PlanWithCost<P, C> implements IPlan {
    public static EmptyPlanWithCost EMPTY_PLAN =  new EmptyPlanWithCost();


    public final static class ErrorPlanWithCost extends PlanWithCost {
        private FuseError error;

        public ErrorPlanWithCost(FuseError error) {
            super(new Plan(Collections.emptyList()),null);
            this.error = error;
        }

        public String toString() {
            return error.toString();
        }
    }

    public final static class EmptyPlanWithCost extends PlanWithCost {
        private EmptyPlanWithCost() {
            super(new Plan(Collections.emptyList()),null);
        }
    }

    //region Constructors
    public PlanWithCost(P plan, C cost) {
        this.plan = plan;
        this.cost = cost;
    }

    public PlanWithCost(PlanWithCost<P, C> planWithCost) {
        this.cost = planWithCost.getCost();
        this.plan = planWithCost.getPlan();
    }
    //endregion

    //region Properties

    public P getPlan() {
        return plan;
    }

    public C getCost() {
        return cost;
    }

    public void setPlan(P plan) {
        this.plan = plan;
    }

    public void setCost(C cost) {
        this.cost = cost;
    }

    public PlanWithCost<P, C> withCost(C cost) {
        setCost(cost);
        return this;
    }

    public PlanWithCost<P, C> withPlan(P Plan) {
        setPlan(plan);
        return this;
    }

    //endregion

    //region Fields
    private P plan;
    private C cost;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanWithCost<?, ?> that = (PlanWithCost<?, ?>) o;

        if (plan != null ? !plan.equals(that.plan) : that.plan != null) return false;
        return cost != null ? cost.equals(that.cost) : that.cost == null;
    }

    @Override
    public int hashCode() {
        int result = plan != null ? plan.hashCode() : 0;
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new PlanWithCostDescriptor<>(new ToStringDescriptor<P>(), new ToStringDescriptor<C>()).describe(this);
    }
}
