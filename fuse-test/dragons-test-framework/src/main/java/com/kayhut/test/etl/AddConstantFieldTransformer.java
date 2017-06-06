package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 06/06/2017.
 */
public class AddConstantFieldTransformer implements Transformer {
    private String fieldName;
    private String fieldValue;

    public AddConstantFieldTransformer(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newList = documents.stream().map(doc -> new HashMap<>(doc)).collect(Collectors.toList());
        newList.forEach(newDoc -> newDoc.put(fieldName, fieldValue));
        return newList;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        builder.addColumn(fieldName);
        return builder.build();
    }
}
