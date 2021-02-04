package com.yangdb.fuse.model.execution.plan.descriptors;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * QueryDescriptor.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.Below;
import com.yangdb.fuse.model.Container;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.asgQuery.IQuery;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.GraphDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.entity.*;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.Query.QueryUtils.findByEnum;
import static com.yangdb.fuse.model.query.Query.QueryUtils.getPath;

public class OntologyDescriptor implements GraphDescriptor<Ontology> {


    //region Descriptor Implementation

    @Override
    public String visualize(Ontology query) {
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
        sb.append("\t rankdir=LR; \n");
        //general node shape
        sb.append("\t node [shape=Mrecord]; \n");
        //append start node shape (first node in query elements list)
        sb.append("\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n");

        //iterate over the entities
        //iterate over the relations
        sb.append("\n\t }");
        return sb.toString();
    }

    public static String printProps(Query query, BasePropGroup element) {
        //add subgraph for the entire quant
        StringBuilder prpoBuilder = new StringBuilder();
        prpoBuilder.append(" \n subgraph cluster_Props_" + element.geteNum() + " { \n");
        prpoBuilder.append(" \t color=green; \n");
        prpoBuilder.append(" \t node [fillcolor=khaki3, shape=component]; \n");
        prpoBuilder.append(" \t " + element.geteNum() + " [fillcolor=green, shape=folder, label=\"" + element.getQuantType() + "\"]; \n");
        // label the prop group type
        prpoBuilder.append(" \t label = \" Props[" + element.geteNum() + "]\"; \n");
        //print the prop group list path itself
        //non inclusive for additional group inside the path - they will be printed separately
        List<BaseProp> props = (List<BaseProp>) element.getProps()
                .stream()
                .map(p -> ((BaseProp) p).clone())
                .collect(Collectors.toList());

        //give specific number to each property in the group
        for (int i = 0; i < props.size(); i++) {
            props.get(i).seteNum(element.geteNum() * 100 + i);
        }

        removeRedundentArrow(prpoBuilder);

        prpoBuilder.append("\n } \n");
        return prpoBuilder.toString();
    }

    public static void removeRedundentArrow(StringBuilder builder) {
        if (builder.toString().endsWith("->"))
            builder.delete(builder.toString().length() - 2, builder.toString().length());
    }


}
