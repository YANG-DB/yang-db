package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelSubResourceAdders;
import com.kayhut.fuse.assembly.knowledge.logical.model.GlobalEntityLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.assembly.knowledge.logical.model.PovLogical;

import java.util.HashMap;


public class LogicalModelEntityPovAdder implements LogicalModelAdder {
    @Override
    public void addChild(ElementBaseLogical parent, ElementBaseLogical child) {
        if(child instanceof GlobalEntityLogical){
            // Isn't a real child
            return;
        }
        GlobalEntityLogical globalEntityLogical = (GlobalEntityLogical)parent;
        PovLogical povLogical = (PovLogical)child;
        HashMap<String, PovLogical> povs = globalEntityLogical.getPovs();
        String povContext = povLogical.getContext();
        if(!povs.containsKey(povContext)){
            povs.put(povContext, povLogical);
        }
    }
}