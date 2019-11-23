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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeCSVTransformer;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeContext;
import com.yangdb.fuse.assembly.knowledge.load.StoreAccessor;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.*;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.elasticsearch.client.Client;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.extractFile;

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
    private DataTransformer<KnowledgeContext, CSVTransformer.CsvElement> transformer;
    private ObjectMapper mapper;

    @Inject
    public KnowledgeCSVDataLoader(Config config, Client client, RawSchema schema, OntologyTransformerProvider transformerProvider, IdGeneratorDriver<Range> idGenerator) {
        this.schema = schema;
        this.mapper = new ObjectMapper();
        //load knowledge transformer
        final Optional<OntologyTransformer> assembly = transformerProvider.transformer(config.getString("assembly"));
        if (!assembly.isPresent())
            throw new IllegalArgumentException("No transformer provider found for selected ontology " + config.getString("assembly"));
        this.transformer = new KnowledgeCSVTransformer(schema, assembly.get(), idGenerator, new StoreAccessor.DefaultAccessor(client));
        this.client = client;
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, File data, GraphDataLoader.Directive directive) throws IOException {
        String contentType = Files.probeContentType(data.toPath());
        if (Arrays.asList("application/gzip", "application/zip").contains(contentType)) {
            ByteArrayOutputStream stream = null; //unzip
            switch (contentType) {
                case "application/gzip":
                    stream = extractFile(new GZIPInputStream(Files.newInputStream(data.toPath())));
                    break;
                case "application/zip":
                    stream = extractFile(new ZipInputStream(Files.newInputStream(data.toPath())));
                    break;
            }

            String payload = new String(stream.toByteArray());
            return load(type,label,payload, directive);
        }
        String payload = new String(Files.readAllBytes(data.toPath()));
        //read
        return load(type,label,payload, directive);

    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, String payload, GraphDataLoader.Directive directive) throws IOException {
        final KnowledgeContext context = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return label;
            }

            @Override
            public String type() {
                return type;
            }

            @Override
            public Reader content() {
                return new StringReader(payload);
            }
        }, directive);
        List<String> success = new ArrayList<>();
        success.add(ENTITIES + ":" +context.getEntities().size());
        success.add(RELATIONS + ":" +context.getRelations().size());
        success.add(E_VALUES + ":" +context.geteValues().size());
        success.add(R_VALUES + ":" +context.getrValues().size());

        Response transformationFailed = new Response("logicalTransformation")
                .success(success).failure(context.getFailed());

        //load all data to designated indices according to schema
        return commit(client, schema, mapper, context, directive)
                .response(transformationFailed);
    }
}
