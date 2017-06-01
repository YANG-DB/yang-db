package com.kayhut.fuse.neo4j.executor;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;

import java.util.ArrayList;

/**
 * Created by liorp on 3/16/2017.
 */
abstract class NeoGraphUtils {

    public static Entity entityFromNodeValue(String tag, InternalNode node) {
        ArrayList<Property> props = new ArrayList<>();
        node.keys().forEach(propName -> {
            Property prop = new Property();
            prop.setAgg(propName);
            prop.setValue(String.valueOf(node.get(propName)));
            props.add(prop);
        });

        return Entity.Builder.instance()
                .withEID(String.valueOf(node.id()))
                .withETag(Lists.newArrayList(tag))
                .withProperties(props).build();
    }

    public static Relationship relFromRelValue(String key, InternalRelationship rel) {
        ArrayList<Property> props = new ArrayList<>();
        rel.keys().forEach(propName -> {
            Property prop = new Property();
            prop.setAgg(propName);
            prop.setValue(String.valueOf(rel.get(propName)));
            props.add(prop);
        });

        return Relationship.Builder.instance()
                .withAgg(false)
                .withRID(String.valueOf(rel.id()))
                .withDirectional(true)
                .withEID1(String.valueOf(rel.startNodeId()))
                .withEID2(String.valueOf(rel.endNodeId()))
                .withProperties(props).build();
    }

}
