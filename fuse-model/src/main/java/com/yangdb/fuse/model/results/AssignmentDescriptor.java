package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.GraphDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class AssignmentDescriptor implements Descriptor<Assignment<Entity, Relationship>>, GraphDescriptor<Assignment<Entity, Relationship>> {
    @Override
    public String describe(Assignment<Entity, Relationship> item) {
        return patternValue(item);
    }

    //region Private Methods
    private String patternValue(Assignment<Entity, Relationship> assignment) {
        StringJoiner joiner = new StringJoiner("-", "", "");

        assignment.getRelationships().stream().sorted()
                .forEach(rel -> {
                    joiner.add(print(getEntityById(rel.geteID1(), assignment).orElseGet(() ->
                            new Entity(Collections.emptySet(), rel.geteID1(), "???", Collections.emptyMap()))));
                    joiner.add(print(rel));
                    joiner.add(print(getEntityById(rel.geteID2(), assignment).orElseGet(() ->
                            new Entity(Collections.emptySet(), rel.geteID1(), "???", Collections.emptyMap()))));
                });
        return (joiner.toString().toCharArray()[joiner.length() - 1] == '-') ?
                joiner.toString().substring(0, joiner.length() - 1) : joiner.toString();

    }

    private Optional<Entity> getEntityById(String id, Assignment<Entity, Relationship> assignment) {
        return assignment.getEntities().stream().filter(e -> e.geteID().equals(id)).findFirst();
    }

    public static String print(Assignment<Entity, Relationship> item) {
        return new AssignmentDescriptor().patternValue(item);
    }

    private static String print(Entity entity) {
        return String.format("[%s,%s, %s]", entity.geteID(), strip(String.join(",", entity.geteTag())), entity.geteType());
    }

    private static String print(Relationship relationship) {
        return String.format("(%s, %s)", relationship.getrID(), relationship.getrType());
    }

    private static String strip(String value) {
        return value.replace('[', ' ').replace(']', ' ');
    }

    public static String printGraph(Assignment<Entity, Relationship> item) {
        return new AssignmentDescriptor().visualize(item);
    }

    @Override
    public String visualize(Assignment<Entity, Relationship> item) {
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
        sb.append("\t rankdir=LR; \n");
        //general node shape
        sb.append("\t node [shape=Mrecord]; \n");
        //append start node shape (first node in query elements list)
//        sb.append("\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n");

        //print entities
        entities(sb, item.getEntities());
        //print relations
        relations(sb, item.getRelationships());
        //iterate over the query
        sb.append("\n\t } \n");
        return sb.toString();
    }

    private void relations(StringBuilder sb, List<Relationship> relationships) {
        relationships.forEach(rel -> appendRel(sb, rel));
    }

    private void appendRel(StringBuilder sb, Relationship rel) {
        String id = rel.getrID().replace(".", "_");
        String label = rel.getrType();

        if (rel.fields().isEmpty()) {
            sb.append("\n \t " + id + " [color=green, fillcolor=yellow, shape=rarrow, label=\"" + label + "\"]; \n");
        } else {
            //only print when entity has fields
            sb.append(" \n subgraph cluster_Q_" + id + " { \n");
            sb.append(" \t color=blue; \n");
            sb.append(" \t " + id + " [fillcolor=lightblue, shape=folder, label=\"" + label + "\"]; \n");
            //print def
            rel.getProperties().forEach(property -> sb.append(" \t " + id + "_" + property.getpType() + " [fillcolor=khaki3, shape=record, label=\"" + property.getpType() + "|" + property.getValue() + "\"] \n"));
            //print graph
            sb.append(" \n \t");
            rel.getProperties().forEach(property -> sb.append(id).append("->").append(id + "_" + property.getpType()).append("\n \t"));
            sb.append("\n");
            sb.append(" \t } \n");
        }
        //print actual graph
        String id1 = rel.geteID1().replace(".", "_");
        String id2 = rel.geteID2().replace(".", "_");
        sb.append(" \t ").append(id1).append("->").append(id).append("->").append(id2).append(" \n");
    }

    private void entities(StringBuilder sb, List<Entity> entities) {
        entities.forEach(e -> appendEntity(sb, e));
    }

    private void appendEntity(StringBuilder sb, Entity e) {
        String id = e.geteID().replace(".", "_");
        String label = e.geteTag() + ":" + e.geteType();

        if (e.getProperties().isEmpty()) {
            sb.append("\n \t " + id + " [style=filled, color=lightblue, shape=Mrecord, label=\"" + label + "\"]; \n");
            return;
        }

        //only print when entity has fields
        sb.append(" \n subgraph cluster_Q_" + id + " { \n");
        sb.append(" \t color=blue; \n");
        sb.append(" \t " + id + " [style=filled, color=lightblue,  shape=folder, label=\"" + label + "\"]; \n");
        //print def
        e.getProperties().forEach(property -> sb.append(" \t " + id + "_" + property.getpType() + " [fillcolor=khaki3, shape=record, label=\"" + property.getpType() + "|" + property.getValue() + "\"] \n"));
        //print graph
        sb.append(" \n \t");
        e.getProperties().forEach(property -> sb.append(id).append("->").append(id + "_" + property.getpType()).append("\n \t"));
        sb.append("\n");

        sb.append(" \t ");
        sb.append(" \t } \n");
    }
}

