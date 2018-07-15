package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalEntityProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.HashMap;
import java.util.Map;


public class LogicalModelEntityEValueAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        FieldLogical field = (FieldLogical) child;
        Map.Entry<String, ValueLogical> valueEntry = field.getValues().entrySet().iterator().next();
        if (parent instanceof GlobalEntityLogical) {
            GlobalEntityLogical entity = (GlobalEntityLogical) parent;
            String valueContent = valueEntry.getValue().getContent();
            if (field.getId().equals(PhysicalEntityProperties.TITLE)) {
                entity.setTitle(valueContent);
            } else {
                entity.getNicknames().add(valueContent);
            }
        } else {
            PovLogical pov = (PovLogical) parent;
            HashMap<String, FieldLogical> povFields = pov.getFields();
            if(!povFields.containsKey(field.getId())){
                povFields.put(field.getId(), field);
            }
            else{
                povFields.get(field.getId()).addValue(valueEntry.getValue());
            }
        }
    }
}