package com.kayhut.fuse.stat.es.providers;

import java.io.IOException;
import java.util.Map;

/**
 * Created by moti on 3/12/2017.
 */
public interface GenericDataProvider {

    Iterable<Map<String, Object>> getDocuments() throws IOException;
}
