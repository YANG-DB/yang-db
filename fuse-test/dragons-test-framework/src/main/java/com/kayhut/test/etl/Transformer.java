package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.List;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public interface Transformer {
    List<Map<String, String>> transform(List<Map<String, String>> documents);
    CsvSchema getNewSchema(CsvSchema oldSchema);
}
