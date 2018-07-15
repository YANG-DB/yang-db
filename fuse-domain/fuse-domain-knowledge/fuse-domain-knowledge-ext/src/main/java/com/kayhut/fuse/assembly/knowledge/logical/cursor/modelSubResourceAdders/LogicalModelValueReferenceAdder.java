package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import java.util.HashMap;
import java.util.Map;


public class LogicalModelValueReferenceAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        FieldLogical fieldLogical = (FieldLogical) parent;
        ReferenceLogical referenceLogical = (ReferenceLogical) child;

        Map.Entry<String, ValueLogical> valueEntry = fieldLogical.getValues().entrySet().iterator().next();
        HashMap<String, ReferenceLogical> references = valueEntry.getValue().getReferences();
        String referenceId = referenceLogical.getId();
        if (!references.containsKey(referenceId)) {
            references.put(referenceId, referenceLogical);
        }
    }
}


