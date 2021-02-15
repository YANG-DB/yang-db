package com.yangdb.fuse.model.execution.plan.costs;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * CountEstimatesCost.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import java.util.Stack;

/**
 * Created by Roman on 27/06/2017.
 */
public class CountEstimatesCost implements Cost, Cloneable {
    //region Constructors

    public CountEstimatesCost() {}

    public CountEstimatesCost(double cost, double countEstimate) {
        this.cost = cost;
        this.countEstimates = new Stack<>();
        this.countEstimates.push(countEstimate);
    }

    public CountEstimatesCost(double cost, Stack<Double> countEstimates) {
        this.cost = cost;
        this.countEstimates = (Stack<Double>) countEstimates.clone();
    }
    //endregion

    //region Public Methods
    public double push(double value) {
        return countEstimates.push(value);
    }

    public double peek() {
        return countEstimates.peek();
    }

    public void applyCountsUpdateFactor(double countUpdateFactor){
        push(Math.ceil(countUpdateFactor * peek()));
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return "[estimation = " + cost + ", countEstimate = " + this.countEstimates.peek() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CountEstimatesCost other = (CountEstimatesCost) o;

        return cost == other.getCost() && countEstimates.equals(other.getCountEstimates());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(cost);

        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Object clone() throws CloneNotSupportedException {
        return new CountEstimatesCost(this.getCost(), this.getCountEstimates());
    }

    //endregion

    //region Properties
    public double getCost() {
        return cost;
    }

    public Stack<Double> getCountEstimates() {
        return countEstimates;
    }
    //endregion

    //region Fields
    private double cost;
    private Stack<Double> countEstimates;
    //endregion
}
