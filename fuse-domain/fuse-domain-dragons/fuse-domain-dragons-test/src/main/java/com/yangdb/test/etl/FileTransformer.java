package com.yangdb.test.etl;

/*-
 *
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by moti on 6/5/2017.
 */
public class FileTransformer {

    private String inputFile;
    private String outputFile;
    private Transformer transformer;
    private List<String> fieldNames;
    private int batchSize;

    public FileTransformer(String inputFile, String outputFile, Transformer transformer, List<String> fieldNames, int batchSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.transformer = transformer;
        this.fieldNames = fieldNames;
        this.batchSize = batchSize;
    }

    public void transform() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        CsvSchema.Builder builder = CsvSchema.builder();
        fieldNames.forEach(f -> builder.addColumn(f));
        CsvSchema schema = builder.build();

        CsvSchema newSchema = transformer.getNewSchema(schema);
        ObjectMapper mapper = new CsvMapper();
        System.out.println(newSchema.getColumnDesc());
        ObjectWriter writer = mapper.writerFor(new TypeReference<Map<String, Object>>() {
        }).with(newSchema).without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        MappingIterator<Map<String, String>> mappingIterator = mapper.readerFor(new TypeReference<Map<String, Object>>() {
        }).with(schema).readValues(new File(inputFile));

        try (OutputStream out = new FileOutputStream(outputFile)) {
            newSchema.getColumnDesc();
            for (List<Map<String, String>> batch : new Splitter(mappingIterator, batchSize)) {
                System.out.println(count.incrementAndGet());
                transformer.transform(batch).forEach(doc -> {
                    try {
                        writer.writeValue(out, doc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private class Splitter implements Iterable<List<Map<String, String>>> {

        private Iterator<Map<String, String>> documents;
        private int batchSize;

        public Splitter(Iterator<Map<String, String>> documents, int batchSize) {

            this.documents = documents;
            this.batchSize = batchSize;
        }

        @Override
        public Iterator<List<Map<String, String>>> iterator() {
            return new Iterator<List<Map<String, String>>>() {

                @Override
                public boolean hasNext() {
                    return documents.hasNext();
                }

                @Override
                public List<Map<String, String>> next() {
                    List<Map<String, String>> docs = new ArrayList<>(batchSize);
                    int i = 0;
                    while (i < batchSize && documents.hasNext()) {
                        docs.add(documents.next());
                        i++;
                    }
                    return docs;
                }
            };
        }
    }



}
