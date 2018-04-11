package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.schema.InitialGraphDataLoader;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;

import java.io.IOException;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardDataLoaderController implements DataLoaderController {
    //region Constructors
    @Inject
    public StandardDataLoaderController(OntologyProvider ontologyProvider,
                                        InitialGraphDataLoader graphDataLoader) {
        this.ontologyProvider = ontologyProvider;
        this.graphDataLoader = graphDataLoader;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<String> load(String ontology) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<String>builder(OK, NOT_FOUND)
                        .data(Optional.of("Elements loaded:" + this.graphDataLoader.load()))
                        .compose();
            } catch (IOException e) {
                return Builder.<String>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(e.getMessage()))
                        .compose();
            }
        }

        return ContentResponse.notFound();
    }

    @Override
    public ContentResponse<String> init(String ontology) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<String>builder(OK, NOT_FOUND)
                        .data(Optional.of("indices created:" + this.graphDataLoader.init()))
                        .compose();
            } catch (IOException e) {
                return Builder.<String>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(e.getMessage()))
                        .compose();
            }
        }

        return ContentResponse.notFound();
    }

    @Override
    public ContentResponse<String> drop(String ontology) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<String>builder(OK, NOT_FOUND)
                        .data(Optional.of("indices dropped:" + this.graphDataLoader.drop()))
                        .compose();
            } catch (IOException e) {
                return Builder.<String>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(e.getMessage()))
                        .compose();
            }
        }

        return ContentResponse.notFound();
    }
//endregion

    //region Private Methods
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private InitialGraphDataLoader graphDataLoader;

    //endregion

}
