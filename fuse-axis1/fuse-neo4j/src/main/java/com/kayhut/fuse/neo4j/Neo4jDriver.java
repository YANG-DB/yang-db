package com.kayhut.fuse.neo4j;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.QueryData;

/**
 * Created by EladW on 22/02/2017.
 */
public interface Neo4jDriver {
    @Subscribe
    GtaData process(QueryData input);
}
