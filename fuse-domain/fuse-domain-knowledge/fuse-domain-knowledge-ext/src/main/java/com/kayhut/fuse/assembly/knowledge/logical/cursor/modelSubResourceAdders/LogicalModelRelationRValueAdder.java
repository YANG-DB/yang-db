package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.HashMap;
import java.util.Map;


public class LogicalModelRelationRValueAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        FieldLogical field = (FieldLogical) child;
        Map.Entry<String, ValueLogical> valueEntry = field.getValues().entrySet().iterator().next();
        RelationLogical relation = (RelationLogical) parent;

        HashMap<String, FieldLogical> relationFields = relation.getFields();
        if (!relationFields.containsKey(field.getId())) {
            relationFields.put(field.getId(), field);
        } else {
            relationFields.get(field.getId()).addValue(valueEntry.getValue());
        }

    }
}