package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.dispatcher.context.QueryExecutionContext;
import com.kayhut.fuse.model.process.QueryResourceResult;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.typesafe.config.Config;

import java.util.Optional;
import java.util.UUID;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseQueryDispatcherDriver implements QueryDispatcherDriver {

    private Config conf;
    private EventBus eventBus;
    private final ResourceStore resourceStore;

    @Inject
    public BaseQueryDispatcherDriver(Config conf, EventBus eventBus, ResourceStore resourceStore) {
        this.conf = conf;
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.resourceStore = resourceStore;
    }


    /**
     * fuse query proccess starts here
     *
     * @param query
     * @return
     */
    @Override
    public QueryResourceResult process(QueryMetadata metadata, Query query) {
        String port = conf.getString("application.port");
        //As the flow starts -> setting the initial response
        //sequence.containsKey(id) ? sequence.put(id,Integer.valueOf(sequence.get(id))+1) : sequence.put(id,0);
        String sequence = UUID.randomUUID().toString();//running number
        //build response metadata
        String host = baseUrl(port);
        QueryResourceResult queryResourceResult = QueryResourceResult.ResultMetadataBuilder.build(String.valueOf(sequence))
                .cursorUrl(host + "/query/" + metadata.getId())
                .compose();

        submit(eventBus, new QueryExecutionContext(metadata, query));
        return queryResourceResult;
    }

    @Override
    public Optional<Plan> explain(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getExecutionPlan());
    }

    @Subscribe
    public void persistQueryResource(QueryExecutionContext context) {
        if (!context.phase(QueryExecutionContext.Phase.cursor)) {
            return;
        }

        QueryResource queryResource = new QueryResource(context.getQuery(), context.getQueryMetadata());
        queryResource.addCursor(1, new CursorResource<>(context.getCursor()));
        resourceStore.addQueryResource(queryResource);
    }
}
