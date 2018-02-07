package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.test.scenario.ETLUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 06/06/2017.
 */
public class AddConstantFieldsTransformer implements Transformer {
    private Map<String, String> fields;
    private Rel.Direction direction;

    public AddConstantFieldsTransformer(Map<String, String> fields, Rel.Direction direction) {
        this.fields = fields;
        this.direction = direction;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newList = documents.stream().map(doc -> new HashMap<>(doc)).collect(Collectors.toList());
        newList.forEach(newDoc -> {
            if(direction == Rel.Direction.RL || newDoc.get(ETLUtils.DIRECTION_FIELD).equals(direction.name())) {
                newDoc.putAll(fields);
            }
        });
        return newList;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        fields.keySet().stream().sorted().forEach(c ->{
            if(oldSchema.column(c) == null)
                builder.addColumn(c);
        });
        return builder.build();
    }
}
