package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.test.scenario.ETLUtils.sdf;

/**
 * Created by moti on 6/7/2017.
 */
public class DateFieldTransformer implements Transformer {
    private String[] fields;

    public DateFieldTransformer(String... fields) {
        this.fields = fields;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newDocs = documents.stream().map(HashMap::new).collect(Collectors.toList());
        newDocs.forEach(doc -> Arrays.asList(fields).forEach(
                field -> doc.put(field, sdf.format(new Date(Long.parseLong(doc.get(field)) * 1000)))));
        return newDocs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        return oldSchema;
    }
}
