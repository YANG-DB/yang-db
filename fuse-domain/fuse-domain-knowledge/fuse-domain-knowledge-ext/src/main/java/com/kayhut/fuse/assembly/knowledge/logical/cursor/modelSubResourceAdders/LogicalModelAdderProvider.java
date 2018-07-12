package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;

import java.util.HashMap;
import java.util.Map;

public class LogicalModelAdderProvider {
    public LogicalModelAdderProvider() {
        this.logicalModelAdders = new HashMap<>();
        logicalModelAdders.put(String.format("%s.%s", ETypes.LOGICAL_ENTITY, ETypes.ENTITY),
                new LogicalModelEntityPovAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.ENTITY_VALUE),
                new LogicalModelEntityEValueAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.REFERENCE),
                new LogicalModelPovReferenceAdder());
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
