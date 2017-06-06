package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 06/06/2017.
 */
public class ChainedTransformer implements Transformer{
    private List<Transformer> transformers;

    public ChainedTransformer(Transformer... transformers) {
        this.transformers = Arrays.stream(transformers).collect(Collectors.toList());
    }


    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> docs = documents;
        for (Transformer transformer : transformers) {
            docs = transformer.transform(docs);
        }
        return docs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        return null;
    }
}
