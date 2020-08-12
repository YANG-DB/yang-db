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

import com.cedarsoftware.util.io.JsonWriter;
import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.ContentResponse.Builder;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.Optional;
import java.util.function.Supplier;

import static io.netty.channel.group.ChannelMatchers.compose;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardGraphQLController implements GraphQLController {
    //region Constructors
    @Inject
    public StandardGraphQLController(OntologyTransformerIfc<String, Ontology> transformer, OntologyProvider provider) {
        this.transformer = transformer;
        this.provider = provider;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<Ontology> translate(String graphQLSchema) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.transform(graphQLSchema)))
                .compose();
    }

    @Override
    public ContentResponse<String> transform(String ontologyId) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.translate(provider.get(ontologyId)
                        .orElseThrow(() -> new FuseError.FuseErrorException(
                                new FuseError("Ontology Not Found", String.format("Ontology %s is not found in repository", ontologyId)))))))
                .compose();
    }

    //endregion

    //region Private Methods

    //region Fields
    private OntologyTransformerIfc<String,Ontology> transformer;
    private OntologyProvider provider;

    //endregion

}
