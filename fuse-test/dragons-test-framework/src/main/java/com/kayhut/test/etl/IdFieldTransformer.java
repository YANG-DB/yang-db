package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kayhut.test.scenario.ETLUtils.sdf;

/**
 * Created by moti on 6/7/2017.
 */
public class IdFieldTransformer implements Transformer {
    private String idField;
    private String directionField;
    private String type;

    public IdFieldTransformer(String idField, String directionField, String type) {
        this.idField = idField;
        this.directionField = directionField;
        this.type = type;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newDocs = documents.stream().map(HashMap::new).collect(Collectors.toList());
        newDocs.forEach(doc -> doc.put(idField, type +"_" + doc.get(idField) + "_" + doc.get(directionField)));
        return newDocs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        return oldSchema;
    }
}
