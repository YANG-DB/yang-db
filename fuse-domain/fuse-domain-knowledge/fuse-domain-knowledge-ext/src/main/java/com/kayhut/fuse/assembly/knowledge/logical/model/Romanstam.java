package com.kayhut.fuse.assembly.knowledge.logical.model;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.results.Relationship;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;

public class Romanstam {
    public interface LogicalModelAdder {
        void addChild(LogicalItemBase parent, LogicalItemBase child);
    }

    public static class EntityPovAdder implements LogicalModelAdder {
        @Override
        public void addChild(LogicalItemBase parent, LogicalItemBase child) {
            EntityLogical entityLogical = (EntityLogical)parent;
            PovLogical povLogical = (PovLogical)child;

            entityLogical.getPovs().add(povLogical);
        }
    }

    public static class PovReferenceAdder implements LogicalModelAdder {
        @Override
        public void addChild(LogicalItemBase parent, LogicalItemBase child) {
            PovLogical povLogical = (PovLogical) parent;
            ReferenceLogical referenceLogical = (ReferenceLogical) child;

            povLogical.getReferences().add(referenceLogical);
        }
    }

    public void doIt() {
        Ontology.Accessor ont = null;
        for(RelationshipType relationship : ont.relations()) {
         //   relationship.getePairs()
        }


        //big Map
        Map<String, LogicalItemBase> logicalItems = new HashMap<>();

        // protoypefactory
        Map<String, LogicalModelAdder> logicalAdders = new HashMap<>();

        logicalAdders.put("LogicalEntity.Entity", new EntityPovAdder());
        logicalAdders.put("Entity.Reference", new PovReferenceAdder());


        Edge edge = null;

        Vertex outVertex = edge.outVertex();
        Vertex inVertex = edge.inVertex();

        LogicalItemBase parentItem = logicalItems.get(outVertex.id());
        LogicalItemBase childItem = logicalItems.get(inVertex.id());

        String logicalAdderKey = String.format("%s.%s", outVertex.label(), inVertex.label());
        LogicalModelAdder logicalAdder = logicalAdders.get(logicalAdderKey);
    }
}
