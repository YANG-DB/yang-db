package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;

/**
 * Created by moti on 4/12/2017.
 */
public class RawGraphStatisticableItemInfo {
    private GraphElementSchema graphElementSchema;
    private Condition[] conditions;

    public RawGraphStatisticableItemInfo(GraphElementSchema graphElementSchema, Condition[] conditions) {
        this.graphElementSchema = graphElementSchema;
        this.conditions = conditions;
    }

    public GraphElementSchema getGraphElementSchema() {
        return graphElementSchema;
    }

    public Condition[] getConditions() {
        return conditions;
    }
}
