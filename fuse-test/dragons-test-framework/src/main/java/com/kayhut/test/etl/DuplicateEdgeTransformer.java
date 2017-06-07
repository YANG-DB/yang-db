package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.fuse.model.execution.plan.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kayhut.test.scenario.ETLUtils.DIRECTION_FIELD;

/**
 * Created by moti on 6/5/2017.
 */
public class DuplicateEdgeTransformer implements Transformer {

    private String id1Field;
    private String id2Field;

    public DuplicateEdgeTransformer(String id1Field, String id2Field) {
        this.id1Field = id1Field;
        this.id2Field = id2Field;
    }
    
    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> docs = new ArrayList<>();
        for (Map<String, String> document : documents) {
            Map<String, String> newDoc = new HashMap<>(document);
            newDoc.put(DIRECTION_FIELD, Direction.out.toString());
            docs.add(newDoc);

            newDoc = new HashMap<>(document);
            newDoc.put(id1Field, document.get(id2Field));
            newDoc.put(id2Field, document.get(id1Field));
            newDoc.put(DIRECTION_FIELD, Direction.in.toString());
            docs.add(newDoc);
        }

        return docs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        return builder.addColumn(DIRECTION_FIELD).build();

    }
}
