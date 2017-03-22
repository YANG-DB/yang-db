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
public class FileCsvDataProvider implements GenericDataProvider {
    private ObjectMapper mapper = new ObjectMapper();
    private String filePath;
    private CsvSchema csvSchema;
    private String csvCypher;

    public FileCsvDataProvider(String filePath, CsvSchema csvSchema) {
        this.filePath = filePath;
        this.csvSchema = csvSchema;
    }

    public FileCsvDataProvider(String filePath, String csvCypher) {
        this.filePath = filePath;
        this.csvCypher = csvCypher;
    }

    @Override
    public Stream<HashMap<String, Object>> getDocuments() throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(new TypeReference<HashMap<String, Object>>() {
        }).with(this.csvSchema);
        MappingIterator<HashMap<String, Object>> objectMappingIterator = reader.readValues(new File(filePath));
        Iterable<HashMap<String, Object>> iterable = () -> objectMappingIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public CsvSchema getCsvSchema() {
        return csvSchema;
    }

    public String getCsvCypher() {
        return csvCypher;
    }
}
