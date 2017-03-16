package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.scenario.DataScenarioDescriptor;
import com.kayhut.test.framework.scenario.ScenarioDocument;

/**
 * Created by moti on 3/16/2017.
 */
public abstract class BaseDataPopulator<C> {

    public void populate(DataScenarioDescriptor descriptor){
        descriptor.getScenarioDocuments().forEach(this::indexDocument);
    }

    public abstract void prepare();
    public abstract C getClient();
    public abstract void teardown();
    public abstract void indexDocument(ScenarioDocument document);
}
