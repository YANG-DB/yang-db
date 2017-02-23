package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.process.command.CursorCommand;
import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 21/02/2017.
 */
public interface QueryDispatcherDriver {
    QueryCursorData process(QueryData input);

}
