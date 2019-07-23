package com.yangdb.test.etl;

/*-
 * #%L
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static com.yangdb.test.scenario.ETLUtils.DURATION;
import static com.yangdb.test.scenario.ETLUtils.START_DATE;

/**
 * Created by moti on 6/5/2017.
 */
public class FilePartitioner {
    private String inputFile;
    private String outputFolder;
    private String fileFormat;
    private List<String> fieldNames;
    private Partitioner partitioner;

    public FilePartitioner(String inputFile, String outputFolder, String fileFormat, List<String> fields, Partitioner partitioner) {
        this.inputFile = inputFile;
        this.outputFolder = outputFolder;
        this.fileFormat = fileFormat;
        this.fieldNames = fields;
        this.partitioner = partitioner;
    }

    public void partition() throws IOException {
        CsvSchema.Builder builder = CsvSchema.builder();
        fieldNames.forEach(f -> builder.addColumn(f));
        CsvSchema schema = builder.build();

        ObjectMapper mapper = new CsvMapper();
        Map<String, BufferedOutputStream> outputStreamMap = new HashMap<>();
        ObjectWriter writer = mapper.writerFor(new TypeReference<Map<String,Object>>() {}).with(schema).without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        MappingIterator<Map<String, String>> mappingIterator = mapper.readerFor(new TypeReference<Map<String,Object>>() {}).with(schema).readValues(new File(inputFile));
        while(mappingIterator.hasNext()){
            Map<String, String> document =  mappingIterator.next();
            String partitionName = partitioner.getPartition(document);
            if(!outputStreamMap.containsKey(partitionName)){
                outputStreamMap.put(partitionName, new BufferedOutputStream(new FileOutputStream(Paths.get(outputFolder, String.format(fileFormat, partitionName)).toFile())));
            }
            writer.writeValue(outputStreamMap.get(partitionName), document);
        }

        outputStreamMap.values().forEach(s -> {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        FilePartitioner dataFilePartitioner = new FilePartitioner("C:\\Users\\moti\\Downloads\\data\\dragonsRelationsTEST_FIRES.csv",
                                                                        "c:\\dataout\\",
                                                                       "fires-%s.csv",
                                                                        Arrays.asList("id", "id1","id2", START_DATE, DURATION),
                                                                        new DateFieldPartitioner(START_DATE, "%s", "", "yyyy" ));
        dataFilePartitioner.partition();
    }
}
