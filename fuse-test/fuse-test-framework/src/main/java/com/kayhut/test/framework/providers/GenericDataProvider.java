package com.kayhut.test.framework.providers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by moti on 3/12/2017.
 */
public interface GenericDataProvider {

    Iterable<Map<String, Object>> getDocuments() throws IOException;
}
