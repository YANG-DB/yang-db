package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.command.ExecutionCompleteCommand;

/**
 * Created by lior on 21/02/2017.
 */
public interface Neo4jExecutorDriver {
    @Subscribe
    ExecutionCompleteCommand process(QueryCursorData input);
}
