package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;

import java.util.HashMap;
import java.util.Map;

public class LogicalModelAdderProvider {
    public LogicalModelAdderProvider() {
        this.logicalModelAdders = new HashMap<>();

         // Entity sub-resources
        logicalModelAdders.put(String.format("%s.%s", ETypes.LOGICAL_ENTITY, ETypes.ENTITY),
                new LogicalModelEntityPovAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.ENTITY_VALUE),
                new LogicalModelEntityEValueAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.REFERENCE),
                new LogicalModelPovReferenceAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.INSIGHT),
                new LogicalModelPovInsightAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.RELATION),
                new LogicalModelPovRelationAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.FILE),
                new LogicalModelPovFileAdder());

        // Value sub-resources
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY_VALUE, ETypes.REFERENCE),
                new LogicalModelValueReferenceAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.RELATION_VALUE, ETypes.REFERENCE),
                new LogicalModelValueReferenceAdder());

        // Insight sub-resources
        logicalModelAdders.put(String.format("%s.%s", ETypes.INSIGHT, ETypes.REFERENCE),
                new LogicalModelInsightReferenceAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.INSIGHT, ETypes.ENTITY),
                new LogicalModelInsightGlobalEntityAdder());

        // Relation sub-resources
        logicalModelAdders.put(String.format("%s.%s", ETypes.RELATION, ETypes.REFERENCE),
                new LogicalModelRelationReferenceAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.RELATION, ETypes.ENTITY),
                new LogicalModelEntityPovAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.RELATION, ETypes.RELATION_VALUE),
                new LogicalModelRelationRValueAdder());

    }


    public Map<String, LogicalModelAdder> getLogicalModelAdders() {
        return logicalModelAdders;
    }

    public void setLogicalModelAdders(Map<String, LogicalModelAdder> logicalModelAdders) {
        this.logicalModelAdders = logicalModelAdders;
    }

    public void addChild(ElementBaseLogical parentItem, ElementBaseLogical childItem, String parentType, String childType) {
        String logicalAdderKey = String.format("%s.%s", parentType, childType);
        LogicalModelAdder logicalAdder = this.logicalModelAdders.get(logicalAdderKey);
        logicalAdder.addChild(parentItem, childItem);
    }


    private Map<String, LogicalModelAdder> logicalModelAdders;


}
