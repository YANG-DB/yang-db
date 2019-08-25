package com.yangdb.dragons.load;

/*-
 * #%L
 * fuse-domain-dragons-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.executor.ontology.schema.DataLoaderUtils;
import com.yangdb.fuse.executor.ontology.schema.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

public class DragonsDataLoader implements GraphDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DragonsDataLoader.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private RawSchema schema;
    private OntologyTransformerProvider transformer;
    private ObjectMapper mapper;

    @Inject
    public DragonsDataLoader(Config config, Client client, RawSchema schema, OntologyTransformerProvider transformerProvider) {
        this.schema = schema;
        this.mapper = new ObjectMapper();
        //load knowledge transformer
        final Optional<OntologyTransformer> assembly = transformerProvider.transformer(config.getString("assembly"));
        if (!assembly.isPresent())
            throw new IllegalArgumentException("No transformer provider found for selected ontology " + config.getString("assembly"));
        this.transformer = transformerProvider;
        this.client = client;

    }

    @Override
    public long init() throws IOException {
        return DataLoaderUtils.init(client,schema);
    }

    @Override
    public LoadResponse load(LogicalGraphModel root, Directive directive) throws IOException {
        return null;
    }

    @Override
    public LoadResponse load(File data, Directive directive) throws IOException {
        return null;
    }

    @Override
    public long drop() throws IOException {
        return DataLoaderUtils.drop(client,schema);
    }
}
