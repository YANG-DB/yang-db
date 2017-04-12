package com.kayhut.test.framework.providers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by moti on 4/12/2017.
 */
public class DotSeparatorDataProvider implements GenericDataProvider {
    private GenericDataProvider innerProvider;

    public DotSeparatorDataProvider(GenericDataProvider innerProvider) {
        this.innerProvider = innerProvider;
    }

    @Override
    public Stream<Map<String, Object>> getDocuments() throws IOException {
        return innerProvider.getDocuments().map(doc -> {
            List<String> compositeKeys = doc.keySet().stream().filter(key -> key.contains(".")).collect(Collectors.toList());

            Map<String, Object> newValues = new HashMap<>();
            for(String key : compositeKeys){
                Object value = doc.remove(key);
                String[] parts = key.split("\\.");
                if(!newValues.containsKey(parts[0])){
                    newValues.put(parts[0], new HashMap<String, Object>());
                }
                ((HashMap<String, Object>)newValues.get(parts[0])).put(parts[1], value);
            }

            for (Map.Entry<String, Object> entry : newValues.entrySet()){
                doc.put(entry.getKey(), entry.getValue());
            }

            return doc;
        });
    }
}