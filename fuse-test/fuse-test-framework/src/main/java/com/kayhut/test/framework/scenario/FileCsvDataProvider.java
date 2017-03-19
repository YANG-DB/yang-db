package com.kayhut.test.framework.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by moti on 3/12/2017.
 */
public abstract class FileCsvDataProvider implements GenericDataProvider {
    private ObjectMapper mapper = new ObjectMapper();
    private String filePath;

    public FileCsvDataProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Stream<HashMap<String, Object>> getDocuments() throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(new TypeReference<HashMap<String, Object>>() {
        }).with(getSchema());
        MappingIterator<HashMap<String, Object>> objectMappingIterator = reader.readValues(new File(filePath));
        Iterable<HashMap<String, Object>> iterable = () -> objectMappingIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public abstract CsvSchema getSchema();

}
