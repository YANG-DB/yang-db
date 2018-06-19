package com.kayhut.fuse.generator.knowledge;

import java.util.Collections;
import java.util.Map;

public class ContextStatistics {
    //region Constructors
    public ContextStatistics() {
        this.entityCategories = Collections.emptyMap();
        this.relationCategories = Collections.emptyMap();

        this.entityFieldCounts = Collections.emptyMap();
        this.relationFieldCounts = Collections.emptyMap();

        this.entityValueCounts = Collections.emptyMap();
        this.relationValueCounts = Collections.emptyMap();

        this.entityRelationCounts = Collections.emptyMap();
        this.insightEntityCounts = Collections.emptyMap();

        this.entityReferenceCounts = Collections.emptyMap();
        this.entityValueReferenceCounts = Collections.emptyMap();
        this.relationReferenceCounts = Collections.emptyMap();
        this.relationValueReferenceCounts = Collections.emptyMap();
        this.insightReferenceCounts = Collections.emptyMap();
    }
    //endregion

    //region Properties
    public Map<String, Integer> getEntityCategories() {
        return entityCategories;
    }

    public void setEntityCategories(Map<String, Integer> entityCategories) {
        this.entityCategories = entityCategories;
    }

    public Map<String, Integer> getRelationCategories() {
        return relationCategories;
    }

    public void setRelationCategories(Map<String, Integer> relationCategories) {
        this.relationCategories = relationCategories;
    }

    public Map<String, Map<Integer, Integer>> getEntityFieldCounts() {
        return entityFieldCounts;
    }

    public void setEntityFieldCounts(Map<String, Map<Integer, Integer>> entityFieldCounts) {
        this.entityFieldCounts = entityFieldCounts;
    }

    public Map<String, Map<Integer, Integer>> getRelationFieldCounts() {
        return relationFieldCounts;
    }

    public void setRelationFieldCounts(Map<String, Map<Integer, Integer>> relationFieldCounts) {
        this.relationFieldCounts = relationFieldCounts;
    }

    public Map<Integer, Integer> getEntityValueCounts() {
        return entityValueCounts;
    }

    public void setEntityValueCounts(Map<Integer, Integer> entityValueCounts) {
        this.entityValueCounts = entityValueCounts;
    }

    public Map<Integer, Integer> getRelationValueCounts() {
        return relationValueCounts;
    }

    public void setRelationValueCounts(Map<Integer, Integer> relationValueCounts) {
        this.relationValueCounts = relationValueCounts;
    }

    public Map<Integer, Integer> getEntityRelationCounts() {
        return entityRelationCounts;
    }

    public void setEntityRelationCounts(Map<Integer, Integer> entityRelationCounts) {
        this.entityRelationCounts = entityRelationCounts;
    }

    public Map<Integer, Integer> getInsightEntityCounts() {
        return insightEntityCounts;
    }

    public void setInsightEntityCounts(Map<Integer, Integer> insightEntityCounts) {
        this.insightEntityCounts = insightEntityCounts;
    }

    public Map<Integer, Integer> getEntityReferenceCounts() {
        return entityReferenceCounts;
    }

    public void setEntityReferenceCounts(Map<Integer, Integer> entityReferenceCounts) {
        this.entityReferenceCounts = entityReferenceCounts;
    }

    public Map<Integer, Integer> getEntityValueReferenceCounts() {
        return entityValueReferenceCounts;
    }

    public void setEntityValueReferenceCounts(Map<Integer, Integer> entityValueReferenceCounts) {
        this.entityValueReferenceCounts = entityValueReferenceCounts;
    }

    public Map<Integer, Integer> getRelationReferenceCounts() {
        return relationReferenceCounts;
    }

    public void setRelationReferenceCounts(Map<Integer, Integer> relationReferenceCounts) {
        this.relationReferenceCounts = relationReferenceCounts;
    }

    public Map<Integer, Integer> getRelationValueReferenceCounts() {
        return relationValueReferenceCounts;
    }

    public void setRelationValueReferenceCounts(Map<Integer, Integer> relationValueReferenceCounts) {
        this.relationValueReferenceCounts = relationValueReferenceCounts;
    }

    public Map<Integer, Integer> getInsightReferenceCounts() {
        return insightReferenceCounts;
    }

    public void setInsightReferenceCounts(Map<Integer, Integer> insightReferenceCounts) {
        this.insightReferenceCounts = insightReferenceCounts;
    }
    //endregion

    //region Fields
    private Map<String, Integer> entityCategories;
    private Map<String, Integer> relationCategories;

    private Map<String, Map<Integer, Integer>> entityFieldCounts;
    private Map<String, Map<Integer, Integer>> relationFieldCounts;

    private Map<Integer, Integer> entityValueCounts;
    private Map<Integer, Integer> relationValueCounts;

    private Map<Integer, Integer> entityRelationCounts;

    private Map<Integer, Integer> insightEntityCounts;

    private Map<Integer, Integer> entityReferenceCounts;
    private Map<Integer, Integer> entityValueReferenceCounts;
    private Map<Integer, Integer> relationReferenceCounts;
    private Map<Integer, Integer> relationValueReferenceCounts;
    private Map<Integer, Integer> insightReferenceCounts;
    //endregion
}
