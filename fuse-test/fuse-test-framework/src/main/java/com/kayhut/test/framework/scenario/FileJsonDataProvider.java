package com.kayhut.test.framework.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
    public Stream<HashMap<String, Object>> getDocuments() throws IOException {
        return Files.lines(Paths.get(filePath)).map(line -> {
            try {
                return mapper.readValue(line, new TypeReference<Map<String, Object>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
