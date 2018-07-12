package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.Map;


public class LogicalModelEntityEValueAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        if (parent instanceof GlobalEntityLogical) {
            GlobalEntityLogical entity = (GlobalEntityLogical) parent;
            FieldLogical field = (FieldLogical) child;
            Map.Entry<String, ValueLogical> valueEntry = field.getValues().entrySet().iterator().next();
            String valueContent = valueEntry.getValue().getContent();
            if (field.getId().equals(PhysicalEntityProperties.TITLE)) {
                entity.setTitle(valueContent);
            } else {
                entity.getNicknames().add(valueContent);
            }
        }
        else{

        }
    }
}