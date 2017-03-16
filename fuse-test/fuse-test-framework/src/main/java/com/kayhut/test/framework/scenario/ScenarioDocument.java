package com.kayhut.test.framework.scenario;

import java.util.Map;

/**
 * Created by moti on 3/13/2017.
 */
public class ScenarioDocument {
    private String id;
    private String indexName;
    private String docType;
    private Map<String, Object> docValues;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Map<String, Object> getDocValues() {
        return docValues;
    }

    public void setDocValues(Map<String, Object> docValues) {
        this.docValues = docValues;
    }
}
