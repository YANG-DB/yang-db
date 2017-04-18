package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

/**
 * Created by moti on 12/04/2017.
 */
public class GraphVertexItemInfo implements RawGraphStatisticableItemInfo {
    private GraphVertexSchema graphVertexSchema;

    public GraphVertexItemInfo(GraphVertexSchema graphVertexSchema) {
        this.graphVertexSchema = graphVertexSchema;
    }
}
