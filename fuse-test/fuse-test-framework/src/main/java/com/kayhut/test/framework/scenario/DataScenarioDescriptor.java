package com.kayhut.test.framework.scenario;

import java.util.stream.Stream;

/**
 * Created by moti on 3/12/2017.
 */
public interface DataScenarioDescriptor {

    Stream<ScenarioDocument> getScenarioDocuments();
}
