package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleCatalogController implements CatalogController {
    //region Constructors
    @Inject
    public SimpleCatalogController(EventBus eventBus, OntologyProvider provider) {
        this.eventBus = eventBus;
        this.provider = provider;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> get(String id) {
        return Builder.<Ontology>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(provider.get(id))
                .compose();
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private OntologyProvider provider;
    //endregion
}
