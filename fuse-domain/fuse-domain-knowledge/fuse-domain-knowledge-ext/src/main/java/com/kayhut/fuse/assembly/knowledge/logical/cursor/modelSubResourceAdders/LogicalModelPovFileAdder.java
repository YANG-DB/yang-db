package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;

import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.FileLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.InsightLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;

import java.util.HashMap;


public class LogicalModelPovFileAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        PovLogical povLogical = (PovLogical) parent;
        FileLogical fileLogical = (FileLogical) child;

        HashMap<String, FileLogical> files = povLogical.getFiles();
        String fileId = fileLogical.getId();
        if (!files.containsKey(fileId)) {
            files.put(fileId, fileLogical);
        }
    }
}


