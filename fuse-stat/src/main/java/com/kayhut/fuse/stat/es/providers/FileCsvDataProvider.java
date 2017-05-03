package com.kayhut.fuse.stat.es.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by moti on 3/12/2017.
 */
public class FileCsvDataProvider implements GenericDataProvider {
    private String filePath;
    private CsvSchema csvSchema;

    public FileCsvDataProvider(String filePath, CsvSchema csvSchema) {
        this.filePath = filePath;
        this.csvSchema = csvSchema;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, Object>>() {
        }).with(this.csvSchema);
        MappingIterator<Map<String, Object>> objectMappingIterator = reader.readValues(new File(filePath));
        return () -> objectMappingIterator;
    }
}
