package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.Path;
import com.kayhut.fuse.model.process.ExecutionContext;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.process.command.CursorCommand;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.ResultMetadata;
import com.kayhut.fuse.model.transport.Response;

import java.util.UUID;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.submit;
import static com.kayhut.fuse.model.process.ProcessElement.ProcessContext.set;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseCursorDispatcherDriver extends BaseDispatcherDriver implements CursorDispatcherDriver{
    private EventBus eventBus;

    @Inject
    public BaseCursorDispatcherDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }


    @Override
    public CursorCommand fetch(String cursorId, long fetchSize) {
        //As the flow starts -> setting the initial response
        return submit(eventBus, new CursorCommand(cursorId,"fetch", String.valueOf(fetchSize)));
    }

    @Override
    public CursorCommand plan(String cursorId) {
        //As the flow starts -> setting the initial response
        return submit(eventBus, new CursorCommand(cursorId,"plan"));
    }

    @Override
    public CursorCommand delete(String cursorId) {
        //As the flow starts -> setting the initial response
        return submit(eventBus, new CursorCommand(cursorId,"delete"));
    }


}
