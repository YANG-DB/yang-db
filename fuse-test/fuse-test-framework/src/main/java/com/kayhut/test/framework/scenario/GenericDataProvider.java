package com.kayhut.test.framework.scenario;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by moti on 3/12/2017.
 */
public interface GenericDataProvider {

    Stream<HashMap<String, Object>> getDocuments() throws IOException;
}
