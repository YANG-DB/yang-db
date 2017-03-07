package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.context.CursorExecutionContext;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.process.CursorResourceResult;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.typesafe.config.Config;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseCursorDispatcherDriver implements CursorDispatcherDriver{
    protected EventBus eventBus;
    protected Config conf;
    protected ResourceStore resourceStore;

    @Inject
    public BaseCursorDispatcherDriver(Config conf, EventBus eventBus, ResourceStore resourceStore) {
        this.conf = conf;
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.resourceStore = resourceStore;
    }


    @Override
    public Optional<CursorResourceResult> fetch(String queryId, int cursorId, long fetchSize) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursor(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        String port = conf.getString("application.port");
        //build response metadata
        String host = baseUrl(port);
        int sequence = cursorResource.get().getNextSequence();
        submit(eventBus, new CursorExecutionContext(cursorResource.get(), sequence, fetchSize));
        return Optional.of(new CursorResourceResult(host + "/query/" + queryId + "/cursor/" + cursorId + "/result/" + sequence));
    }

    @Subscribe
    public void persistResultResource(CursorExecutionContext context) {
        if (context.getResult() == null) {
            return;
        }

        context.getCursorResource().addResultResource(context.getResultId(),
                ContentResponse.ResponseBuilder.<QueryResult>builder("1").data(context.getResult()).compose());
    }
}
