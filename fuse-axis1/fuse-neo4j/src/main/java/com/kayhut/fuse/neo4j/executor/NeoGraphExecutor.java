/*package com.kayhut.fuse.neo4j.executor;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import com.kayhut.fuse.neo4j.cypher.types.CypherTypeParsersFactory;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;

import java.util.ArrayList;*/

/**
 * Created by liorp on 3/16/2017.
 */
/*abstract class NeoGraphUtils {

    public static Entity entityFromNodeValue(String tag, InternalNode node, Ontology ont) {

        ArrayList<Property> props = new ArrayList<>();
        node.keys().forEach(propName -> {
            Property prop = new Property();
            Option<com.kayhut.fuse.model.ontology.Property> property = Stream.ofAll(ont.getProperties()).find(p -> p.getName().equals(propName));
            if(property.isEmpty()) {
                throw new RuntimeException("Unknown property returned: " + propName);
            }
            prop.setpType(property.get().getpType());
            prop.setAgg(propName);
            prop.setValue(CypherTypeParsersFactory.toPropValue(ont, property.get().getType(), node.get(propName)));
            props.add(prop);
        });

        String label = Stream.ofAll(node.labels()).toJavaList().get(0);

        EntityType entityType = Stream.ofAll(ont.getEntityTypes()).find(et -> et.getName().equals(label)).get();

        return Entity.Builder.instance()
                .withEType(entityType.geteType())
                .withEID(String.valueOf(node.id()))
                .withETag(Lists.newArrayList(tag))
                .withProperties(props).build();
    }

    public static Relationship relFromRelValue(String tag, InternalRelationship rel, Ontology ont) {

        ArrayList<Property> props = new ArrayList<>();
        rel.keys().forEach(propName -> {
            Property prop = new Property();
            Option<com.kayhut.fuse.model.ontology.Property> property = Stream.ofAll(ont.getProperties()).find(p -> p.getName().equals(propName));
            if(property.isEmpty()) {
                throw new RuntimeException("Unknown property returned: " + propName);
            }
            prop.setpType(property.get().getpType());
            prop.setAgg(propName);
            prop.setValue(CypherTypeParsersFactory.toPropValue(ont, property.get().getType(),rel.get(propName)));
            props.add(prop);
        });

        String label = rel.type();

        RelationshipType relType = Stream.ofAll(ont.getRelationshipTypes()).find(rt -> rt.getName().equals(label)).get();

        return Relationship.Builder.instance()
                .withRType(relType.getrType())
                .withAgg(false)
                .withRID(String.valueOf(rel.id()))
                .withDirectional(true)
                .withEID1(String.valueOf(rel.startNodeId()))
                .withEID2(String.valueOf(rel.endNodeId()))
                .withProperties(props).build();
    }

}*/
