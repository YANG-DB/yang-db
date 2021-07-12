package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple2;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.extractFile;

/**
 * Loader for CSV Data Model to E/S
 * - load with file
 */
public class IndexProviderBasedCSVLoader implements CSVDataLoader {
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    private Client client;
    private CSVTransformer transformer;
    private RawSchema schema;

    @Inject
    public IndexProviderBasedCSVLoader(Client client,
                                       CSVTransformer transformer,
                                       RawSchema schema) {
        this.client = client;
        this.transformer = transformer;
        this.schema = schema;
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, File data, GraphDataLoader.Directive directive) throws IOException {
        DataTransformerContext context;
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

            ByteArrayOutputStream finalStream = stream;
            context = transformer.transform(readCsv(type, label, new BufferedReader(new InputStreamReader(new ByteArrayInputStream(finalStream.toByteArray())))), directive);
            return load(context, directive);
        }

        context = transformer.transform(readCsv(type, label, new FileReader(data.getAbsoluteFile())), directive);
        return load(context, directive);
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, String payload, GraphDataLoader.Directive directive) throws IOException {
        //todo
        DataTransformerContext context = transformer.transform(new CSVTransformer.CsvElement() {
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
        return load(context, directive);
    }

    /**
     * load data into E/S
     *
     * @param context
     * @param directive
     * @return
     */
    private LoadResponse<String, FuseError> load(DataTransformerContext context, GraphDataLoader.Directive directive) {
        //load bulk requests
        Tuple2<Response, BulkRequestBuilder> tuple = LoadUtils.load(schema, client, context);
        //submit bulk request
        LoadUtils.submit(tuple._2(), tuple._1());
        return new LoadResponseImpl().response(context.getTransformationResponse()).response(tuple._1());
    }

    private CSVTransformer.CsvElement readCsv(String type, String label, Reader reader) {
        return new CSVTransformer.CsvElement() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public String label() {
                return label;
            }

            @Override
            public Reader content() {
                return reader;
            }
        };
    }
}
