package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.OntologyResponse;

import java.io.IOException;

import static com.kayhut.fuse.model.Utils.asObject;
import static com.kayhut.fuse.model.Utils.readJsonFile;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleCatalogController implements CatalogController {
    private EventBus eventBus;

    @Inject
    public SimpleCatalogController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public OntologyResponse ontology(String id) {
        try {
            return new OntologyResponse(id, asObject(readJsonFile(id), Ontology.class));
        } catch (IOException e) {
            return new OntologyResponse(id,null);
        }
    }
}
