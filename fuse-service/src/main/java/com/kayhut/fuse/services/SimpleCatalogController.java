package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;

import java.io.IOException;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.asObject;
import static com.kayhut.fuse.model.Utils.readJsonFile;
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
    public SimpleCatalogController(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> get(String id) {
        try {
            return Builder.<Ontology>builder(randomUUID().toString(),OK, NOT_FOUND)
                    .data(Optional.of(asObject(readJsonFile(id), Ontology.class)))
                    .compose();
        } catch (IOException e) {
            return ContentResponse.NOT_FOUND;
        }
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    //endregion
}
