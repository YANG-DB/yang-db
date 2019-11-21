package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeContext;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeTransformer;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.*;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.elasticsearch.client.Client;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.commit;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeCSVDataLoader implements CSVDataLoader {
    public static final String RELATIONS = "Relations";
    public static final String ENTITIES = "Entities";
    public static final String E_VALUES = "eValues";
    public static final String R_VALUES = "rValues";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private RawSchema schema;
    private KnowledgeTransformer transformer;
    private ObjectMapper mapper;

    @Inject
    public KnowledgeCSVDataLoader(Config config, Client client, RawSchema schema, OntologyTransformerProvider transformerProvider, IdGeneratorDriver<Range> idGenerator) {
        this.schema = schema;
        this.mapper = new ObjectMapper();
        //load knowledge transformer
        final Optional<OntologyTransformer> assembly = transformerProvider.transformer(config.getString("assembly"));
        if (!assembly.isPresent())
            throw new IllegalArgumentException("No transformer provider found for selected ontology " + config.getString("assembly"));
        this.transformer = new KnowledgeTransformer(assembly.get(), schema, idGenerator, client);
        this.client = client;
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, File data, GraphDataLoader.Directive directive) throws IOException {
        //todo
        return LoadResponse.EMPTY;

    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String payload, GraphDataLoader.Directive directive) throws IOException {
        //todo
        return LoadResponse.EMPTY;
    }
}
