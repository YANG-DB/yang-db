package com.kayhut.fuse.stat.es.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.collection.Stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Created by moti on 3/12/2017.
 */
public class FileJsonDataProvider implements GenericDataProvider {
    private ObjectMapper mapper = new ObjectMapper();
    private String filePath;

    public FileJsonDataProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws IOException {
        return Stream.ofAll(() -> {
            try {
                return Files.lines(Paths.get(filePath)).iterator();
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyIterator();
            }
        }).map(line -> {
            try {
                return mapper.readValue(line, new TypeReference<Map<String, Object>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
