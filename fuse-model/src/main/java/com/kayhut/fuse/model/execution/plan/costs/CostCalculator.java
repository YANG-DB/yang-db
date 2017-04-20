package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.Plan;

/**
 * Created by moti on 4/20/2017.
 */
public interface CostCalculator {
    Cost calculateCost(Plan<Cost> plan);

    class Cost {
        public Cost(double cost, long total, long cardinality) {
            this.cost = cost;
            this.total = total;
            this.cardinality = cardinality;
        }

        public double cost;
        public long total;
        public long cardinality;


    }
}
