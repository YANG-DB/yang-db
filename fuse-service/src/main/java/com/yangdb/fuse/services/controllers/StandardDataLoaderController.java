package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphInitiator;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.ContentResponse.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.jooby.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardDataLoaderController implements DataLoaderController {
    //region Constructors
    @Inject
    public StandardDataLoaderController(OntologyProvider ontologyProvider,
                                        GraphInitiator initiator,
                                        CSVDataLoader csvDataLoader,
                                        GraphDataLoader graphDataLoader) {
        this.ontologyProvider = ontologyProvider;
        this.initiator = initiator;
        this.csvDataLoader = csvDataLoader;
        this.graphDataLoader = graphDataLoader;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                        .data(Optional.of(this.graphDataLoader.load(data, directive)))
                        .compose();
            } catch (IOException e) {
                return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(new LoadResponse<String, FuseError>() {
                            @Override
                            public List<CommitResponse<String, FuseError>> getResponses() {
                                return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                    @Override
                                    public List<String> getSuccesses() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public List<FuseError> getFailures() {
                                        return Collections.singletonList(new FuseError(e.getMessage(), e));
                                    }
                                });
                            }

                            @Override
                            public LoadResponse response(CommitResponse<String, FuseError> response) {
                                return this;
                            }
                        }))
                        .compose();
            }
        }

        return ContentResponse.notFound();
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                        .data(Optional.of(this.csvDataLoader.load(type,label , data, directive)))
                        .compose();
            } catch (IOException e) {
                return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(new LoadResponse<String, FuseError>() {
                            @Override
                            public List<CommitResponse<String, FuseError>> getResponses() {
                                return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                    @Override
                                    public List<String> getSuccesses() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public List<FuseError> getFailures() {
                                        return Collections.singletonList(new FuseError(e.getMessage(), e));
                                    }
                                });
                            }

                            @Override
                            public LoadResponse response(CommitResponse<String, FuseError> response) {
                                return this;
                            }
                        }))
                        .compose();
            }
        }
        return ContentResponse.notFound();
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                        .data(Optional.of(this.csvDataLoader.load(type, label, data, directive)))
                        .compose();
            } catch (IOException e) {
                return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(new LoadResponse<String, FuseError>() {
                            @Override
                            public List<CommitResponse<String, FuseError>> getResponses() {
                                return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                    @Override
                                    public List<String> getSuccesses() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public List<FuseError> getFailures() {
                                        return Collections.singletonList(new FuseError(e.getMessage(), e));
                                    }
                                });
                            }

                            @Override
                            public LoadResponse response(CommitResponse<String, FuseError> response) {
                                return this;
                            }
                        }))
                        .compose();
            }
        }
        return ContentResponse.notFound();
    }

    @Override
    /**
     * does:
     *  - unzip file
     *  - split to multiple small files
     *  - for each file (in parallel)
     *      - convert into bulk set
     *      - commit to repository
     */
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                        .data(Optional.of(this.graphDataLoader.load(data, directive)))
                        .compose();
            } catch (IOException e) {
                return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                        .data(Optional.of(new LoadResponse<String, FuseError>() {
                            @Override
                            public List<CommitResponse<String, FuseError>> getResponses() {
                                return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                    @Override
                                    public List<String> getSuccesses() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public List<FuseError> getFailures() {
                                        return Collections.singletonList(new FuseError(e.getMessage(), e));
                                    }
                                });
                            }

                            @Override
                            public LoadResponse response(CommitResponse<String, FuseError> response) {
                                return this;
                            }
                        }))
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
                        .data(Optional.of("indices created:" + this.initiator.init()))
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
    public ContentResponse<String> createMapping(String ontology, String indexProvider) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<String>builder(OK, NOT_FOUND)
                        .data(Optional.of("mapping created:" + this.initiator.createTemplate(ontology,indexProvider)))
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
    public ContentResponse<String> createIndices(String ontology, String indexProvider) {
        if (ontologyProvider.get(ontology).isPresent()) {
            try {
                return Builder.<String>builder(OK, NOT_FOUND)
                        .data(Optional.of("indices created:" + this.initiator.createIndices(ontology,indexProvider)))
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
                        .data(Optional.of("indices dropped:" + this.initiator.drop()))
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
    private final GraphInitiator initiator;
    private final CSVDataLoader csvDataLoader;
    private GraphDataLoader graphDataLoader;

    //endregion

}
