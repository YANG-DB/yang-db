package com.kayhut.fuse.neo4j.executor;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.collection.Stream;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.internal.value.StringValue;

import java.util.ArrayList;

/**
 * Created by liorp on 3/16/2017.
 */
abstract class NeoGraphUtils {

    public static Entity entityFromNodeValue(String tag, InternalNode node, Ontology ont) {

        ArrayList<Property> props = new ArrayList<>();
        node.keys().forEach(propName -> {
            Property prop = new Property();
            String pType = Stream.ofAll(ont.getProperties()).find(p -> p.getName().equals(propName)).get().getpType();
            prop.setpType(pType);
            prop.setAgg(propName);
            prop.setValue(node.get(propName) instanceof StringValue ? node.get(propName).asString() : String.valueOf(node.get(propName)));
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
            String pType = Stream.ofAll(ont.getProperties()).find(p -> p.getName().equals(propName)).get().getpType();
            prop.setpType(pType);
            prop.setAgg(propName);
            prop.setValue(rel.get(propName) instanceof StringValue ? rel.get(propName).asString() :String.valueOf(rel.get(propName)));
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

}
