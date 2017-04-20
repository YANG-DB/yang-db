package com.kayhut.fuse.epb.plan.cost.calculation;

import java.util.Optional;

/**
 * Created by moti on 31/03/2017.
 */
public interface OpCostCalculator<C, I, CX> {
    C calculateCost(I item, Optional<CX> context);
}
