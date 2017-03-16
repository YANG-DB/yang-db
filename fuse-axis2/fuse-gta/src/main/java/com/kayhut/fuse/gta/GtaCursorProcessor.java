package com.kayhut.fuse.gta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.Cursor;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class GtaCursorProcessor implements
        CursorCreationOperationContext.Processor {

    private final EventBus eventBus;

    @Inject
    public GtaCursorProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);

    }

    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }

        return submit(eventBus, context.of(new Cursor() {
            @Override
            public String toString() {
                return "{\"data\":\"data\"}";
            }
        }));

    }

}
