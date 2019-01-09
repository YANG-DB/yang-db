package com.kayhut.fuse.assembly.knowledge.parser;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.parser.model.Step;
import com.kayhut.fuse.assembly.knowledge.parser.model.Types;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.kayhut.fuse.assembly.knowledge.parser.model.TypeConstraint.asConstraint;

public class JsonQueryTranslator {
    static private ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    static public Query jsonParser(JSONObject query) throws IOException {
        Query.Builder builder = Query.Builder.instance();
        builder.withName(query.optString("name","Query:"+System.currentTimeMillis()));
        builder.withOnt(query.optString("ontology","Knowledge"));

        JSONArray clause = query.getJSONArray("clause");
        AtomicInteger sequence = new AtomicInteger(0);
        final JSONArray clauseArray = clause.getJSONArray(0);
        final Start start = new Start(sequence.get(), 0);
        AtomicReference<EBase> context = new AtomicReference<>(start);
        builder.withElement(start);

        //iterate and populate query elements
        for (int i = 0; i < clauseArray.length(); i++) {
            JSONObject obj = clauseArray.getJSONArray(i).getJSONObject(0);
            builder.withElement(element(context, mapper.readValue(obj.toString(), Step.class), sequence));
        }

        return builder.build();
    }


    private static EBase[] element(AtomicReference<EBase> context,Step step, AtomicInteger sequence) {
        List<EBase> elements = new ArrayList<>();
        switch (step.getType()) {
            case entity:
                elements.addAll(createNode(context,step,sequence));
                break;
            case link:
                elements.addAll(createEdge(context,step,sequence));
                break;
        }
        return elements.toArray(new EBase[elements.size()]);
    }

    /**
     * generate Entity type node plus hasEvalue relation with the eValue props
     * @param step
     * @param sequence
     * @return
     */
    private static Collection<EBase> createNode( AtomicReference<EBase> context, Step step, AtomicInteger sequence) {
        List<EBase> nodes = new ArrayList<>();
        final ETyped entity = new ETyped(sequence.incrementAndGet(),
                step.getConceptId() + "_" + sequence.get(),
                "Entity", -1, 0);
        addNext(context,entity);
        nodes.add(entity);
        //context moves to entity
        context.set(entity);

        //region populate eValues
        if(!step.getProperties().isEmpty()) {
            nodes.addAll(appendProperties(context, step, sequence, entity));
        }
        //endregion
        return nodes;
    }

    private static List<EBase> appendProperties(AtomicReference<EBase> context, Step step, AtomicInteger sequence, ETyped entity) {
        List<EBase> nodes = new ArrayList<>();
        final int quantId = sequence.incrementAndGet();
        entity.setNext(quantId);
        Quant1 quant = new Quant1(quantId,QuantType.all, new ArrayList<>(),0 );
        nodes.add(quant);
        //context moves to quant
        context.set(quant);
        //todo add metadata relProps below

        //region add eProps
        step.getProperties().forEach((key, value) -> {
            final int relId = sequence.incrementAndGet();
            quant.addNext(relId);

            //add rel hasEvalue
            final int eValue = sequence.incrementAndGet();
            nodes.add(new Rel(relId, "hasEvalue", Rel.Direction.R, relId + ":" + step.getConceptId() + "_hasEvalue_" + key, eValue, 0));

            //add eValue Node
            final int propQuantId = sequence.incrementAndGet();
            nodes.add(new ETyped(eValue, eValue + ":" + step.getConceptId() + "_eValue_" + key, "Evalue", propQuantId, 0));

            //add properties quant
            final Quant1 propsQuant = new Quant1(propQuantId, QuantType.all, new ArrayList<>(), 0);
            nodes.add(propsQuant);

            //add eProp fieldId
            final int fieldId = sequence.incrementAndGet();
            nodes.add(new EProp(fieldId, "fieldId",
                    Constraint.of(ConstraintOp.eq, Types.byValue(key).getSuffix())));
            propsQuant.addNext(fieldId);

            //add value constraint
            final int valueId = sequence.incrementAndGet();
            nodes.add(new EProp(valueId,
                    Types.byValue(key).getFieldType(),
                    asConstraint(value.getMatchType(), value.getValue())));
            propsQuant.addNext(valueId);
        });
        return nodes;
        //endregion
    }

    /**
     * generate Relation type node plus hasEvalue relation with the eValue props
     * @param context
     * @param step
     * @param sequence
     * @return
     */
    private static Collection<EBase> createEdge(AtomicReference<EBase> context, Step step, AtomicInteger sequence) {
        List<EBase> nodes = new ArrayList<>();
        //add rel hasEvalue
        int relId = sequence.incrementAndGet();
        Rel relatedEntity = new Rel(relId, "relatedEntity", step.getDirection().to(), relId + ":" + step.getConceptId() + "_hasRelation_["+step.getDirection()+"]", 0, 0);
        nodes.add(relatedEntity);

        //todo add rValue properties

        //add metadata relProps below
        if(!step.getProperties().isEmpty()) {
            ArrayList<RelProp> props = new ArrayList<>();
            int relGroup = sequence.incrementAndGet();
            relatedEntity.setB(relGroup);
            step.getProperties().forEach((key, value) -> {
                //add metadata constraint
                final int valueId = sequence.incrementAndGet();
                props.add(new RelProp(valueId,
                        Types.byValue(key).getSuffix(),
                        asConstraint(value.getMatchType(), value.getValue()),0));
            });
            //populate group
            RelPropGroup group = new RelPropGroup(relGroup, props);
            nodes.add(group);
        }
        addNext(context,relatedEntity);
        //context moves to rel
        context.set(relatedEntity);
        return nodes;
    }

    private static void addNext(AtomicReference<EBase> context, EBase entity) {
        if(Quant1.class.isAssignableFrom(context.get().getClass())) {
            ((Quant1) context.get()).addNext(entity.geteNum());
        }else  if(Next.class.isAssignableFrom(context.get().getClass())) {
            ((Next) context.get()).setNext(entity.geteNum());
        }
    }

}
