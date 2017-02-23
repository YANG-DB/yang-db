package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.command.CursorCommand;

import static com.kayhut.fuse.model.Utils.submit;

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
