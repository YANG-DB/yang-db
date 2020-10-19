package com.yangdb.fuse.dispatcher.convertion;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.yangdb.fuse.model.ontology.EnumeratedType;
import com.yangdb.fuse.model.ontology.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * csv to json translators
 */
public abstract class CsvToJsonConverter {
    /**
     * transform a csv file into json
     *
     * @param csv
     * @return
     * @throws IOException
     */
    public static String csvToJson(String csv) throws IOException {

        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();

        // Read data from CSV file
        List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csv).readAll();

        ObjectMapper mapper = new ObjectMapper();

        // Write JSON formated data to output.json file
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll);
    }

    /**
     * convert json to Enum type
     *
     * @param csv
     * @return
     * @throws IOException
     */
    public static EnumeratedType csvToEnum(String name,int counterIndex,int valueIndex, String csv) throws IOException {
        CsvSchema csvSchema = CsvSchema
                .builder()
                .setUseHeader(true)
                .build();
        CsvMapper csvMapper = new CsvMapper();

        // Read data from CSV file
        List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csv).readAll();
        List<Value> values = readAll.stream().map(v -> (Map) v).map(line ->
                new Value(Integer.parseInt(line.values().toArray()[counterIndex].toString()),
                        line.values().toArray()[valueIndex].toString()))
                .collect(Collectors.toList());

        return new EnumeratedType(name,values);
    }
}
