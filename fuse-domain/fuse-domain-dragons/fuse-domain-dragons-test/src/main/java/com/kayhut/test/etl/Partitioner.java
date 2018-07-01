package com.kayhut.test.etl;

import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public interface Partitioner {
    String getPartition(Map<String, String> document);
}
