package com.yangdb.fuse.assembly.knowledge.parser;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.yangdb.fuse.assembly.knowledge.parser.model.Step;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaFields.CATEGORY;
import static com.yangdb.fuse.assembly.knowledge.parser.JsonQueryTranslator.PropertyBuilder.*;
import static com.yangdb.fuse.assembly.knowledge.parser.model.TypeConstraint.asConstraint;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static java.util.Collections.emptyList;

public class JsonQueryTranslator {
    static private ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Query translate(String query, BusinessTypesProvider typesProvider) throws IOException {
        return jsonParser(new JSONObject(query), typesProvider);
    }

    public Query translate(JSONObject query, BusinessTypesProvider typesProvider) throws IOException {
        return jsonParser(query, typesProvider);
    }


    private Query jsonParser(JSONObject query, BusinessTypesProvider typesProvider) throws IOException {
        Query.Builder builder = Query.Builder.instance();
        builder.withName(query.optString("name", "Query:" + System.currentTimeMillis()));
        builder.withOnt(query.optString("ontology", "Knowledge"));

        JSONArray clause = query.getJSONArray("clause");
        AtomicInteger sequence = new AtomicInteger(0);
        final JSONArray clauseArray = clause.getJSONArray(0);
        final Start start = new Start(sequence.get(), 0);
        AtomicReference<EBase> context = new AtomicReference<>(start);
        builder.start();

        List<EBase> elements = new ArrayList<>();
        //iterate and populate query elements
        for (int i = 0; i < clauseArray.length(); i++) {
            elements.addAll(element(i, clauseArray, context, sequence, typesProvider));
        }

        builder.withElements(elements);
        return builder.build();
    }


    private List<EBase> element(int index, JSONArray clauseArray, AtomicReference<EBase> context, AtomicInteger sequence, BusinessTypesProvider typesProvider) throws IOException {
        List<EBase> elements = new ArrayList<>();
        JSONObject obj = clauseArray.getJSONArray(index).getJSONObject(0);
        Step step = mapper.readValue(obj.toString(), Step.class);
        boolean hasNextStep = index < (clauseArray.length() - 1);
        switch (step.getType()) {
            case entity:
                elements.addAll(createNode(context, step, sequence, typesProvider, hasNextStep));
                break;
            case link:
                elements.addAll(createEdge(context, step, sequence, typesProvider, hasNextStep));
                break;
        }
        return elements;
    }

    /**
     * generate Entity type node plus hasEvalue relation with the eValue props
     *
     * @param step
     * @param sequence
     * @param hasNextStep
     * @return
     */
    private Collection<EBase> createNode(AtomicReference<EBase> context, Step step, AtomicInteger sequence, BusinessTypesProvider typesProvider, boolean hasNextStep) {
        List<EBase> nodes = new ArrayList<>();
        final ETyped entity = new ETyped(sequence.incrementAndGet(),
                step.getConceptId() + "_" + sequence.get(),
                "Entity", -1, 0);
        addNext(context, entity);
        nodes.add(entity);
        //context moves to entity
        context.set(entity);

        //region populate eValues
        if (!step.getProperties().isEmpty()) {
            nodes.addAll(appendProperties(context, step, emptyList(), sequence, entity, EProp.class, "Evalue","hasEvalue", typesProvider));
        }
        //endregion
        return nodes;
    }


    /**
     * generate Relation type node plus hasRvalue relation with the rValue props
     *
     * @param context
     * @param step
     * @param sequence
     * @param hasNextStep
     * @return
     */
    private Collection<EBase> createEdge(AtomicReference<EBase> context, Step step, AtomicInteger sequence, BusinessTypesProvider typesProvider, boolean hasNextStep) {
        List<EBase> nodes = new ArrayList<>();
        //add rel => assuming former entity if of 'Entity' type
        int relId = sequence.incrementAndGet();
        RelationTypeBuilder typeBuilder = new RelationTypeBuilder(step.getDirection());
        Rel relatedEntity = new Rel(relId, "hasRelation", typeBuilder.direction.to(), relId + ":" + step.getConceptId() + "_" + typeBuilder.rType() + "_[" + typeBuilder.direction + "]", -1);
        //add next
        addNext(context, relatedEntity);
        nodes.add(relatedEntity);
        //context moves to entity
        context.set(relatedEntity);

        final ETyped entity = new ETyped(sequence.incrementAndGet(),
                step.getConceptId() + "_" + sequence.get(),
                "Relation", -1, 0);
        addNext(context, entity);
        nodes.add(entity);
        //context moves to entity
        context.set(entity);
        //label is "conceptId":"http://huha.com/facebook#friends"
        String category = step.getConceptId();
        //metadata properties
        List<BaseProp> props = Collections.singletonList(new BaseProp() {
            @Override
            public String getpType() {
                return CATEGORY;
            }

            @Override
            public Constraint getCon() {
                return Constraint.of(eq, category);
            }
        });
        //region populate eValues/rValue
        nodes.addAll(appendProperties(context, step, props, sequence, entity, EProp.class, "Rvalue","hasRvalue", typesProvider));
        // add next rel only if another step (Entity) exists
        if (hasNextStep) {
            //add rel reverse <=
            relId = sequence.incrementAndGet();
            relatedEntity = new Rel(relId, "hasRelation", typeBuilder.direction.reverse().to(), relId + ":" + step.getConceptId() + "_" + typeBuilder.reverse() + "_[" + typeBuilder.direction.reverse() + "]", -1);
            //add metadata relProps below
            addNext(context, relatedEntity);
            nodes.add(relatedEntity);
            //context moves to rel
            context.set(relatedEntity);
        }
        return nodes;
    }

    private <T extends BaseProp> List<EBase> appendProperties(AtomicReference<EBase> context, Step step, List<BaseProp> metadata,
                                                              AtomicInteger sequence, ETyped entity, Class<T> propClazz,
                                                              String propType, String propRelType, BusinessTypesProvider typesProvider) {
        List<EBase> nodes = new ArrayList<>();
        final int quantId = sequence.incrementAndGet();
        entity.setNext(quantId);
        Quant1 quant = new Quant1(quantId, QuantType.all, new ArrayList<>(), 0);
        nodes.add(quant);
        //context moves to quant
        context.set(quant);
        //add metadata relProps below
        metadata.forEach(prop -> {
            final int propId = sequence.incrementAndGet();
            quant.addNext(propId);
            //add _Value Node
            nodes.add(createProp(propClazz, propId, prop.getpType(), prop.getCon()));
        });

        //region add eProps /
        step.getProperties().forEach((key, value) -> {
            final int relId = sequence.incrementAndGet();
            quant.addNext(relId);

            //add rel has?(E/R)value
            final int _value = sequence.incrementAndGet();
            nodes.add(new Rel(relId, propRelType, Rel.Direction.R, relId + ":" + step.getConceptId() + "_" + propRelType + "_" + key, _value, 0));

            //add _Value Node
            final int propQuantId = sequence.incrementAndGet();
            nodes.add(new ETyped(_value, _value + ":" + step.getConceptId() + "_" + propType + "_" + key, propType, propQuantId, 0));

            //add properties quant
            final Quant1 propsQuant = new Quant1(propQuantId, QuantType.all, new ArrayList<>(), 0);
            nodes.add(propsQuant);

            //add ?(e/r)Prop fieldId
            final int fieldId = sequence.incrementAndGet();
            nodes.add(createProp(propClazz, fieldId, "fieldId",
                    Constraint.of(eq, key)));
            propsQuant.addNext(fieldId);

            //add value constraint
            final int valueId = sequence.incrementAndGet();
            nodes.add(createProp(propClazz, valueId,
                    typesProvider.type(key).orElse("stringValue"),
                    asConstraint(value.getMatchType(), value.getValue())));
            propsQuant.addNext(valueId);
        });
        return nodes;
        //endregion
    }

    private void addNext(AtomicReference<EBase> context, EBase entity) {
        if (Quant1.class.isAssignableFrom(context.get().getClass())) {
            ((Quant1) context.get()).addNext(entity.geteNum());
        } else if (Next.class.isAssignableFrom(context.get().getClass())) {
            ((Next) context.get()).setNext(entity.geteNum());
        }
    }

    static class RelationTypeBuilder {
        private Direction direction;

        public RelationTypeBuilder(Direction direction) {
            this.direction = direction;
        }

        public String reverse() {
            String rType = "hasRelation";
            switch (direction.reverse().to()) {
                case L:
                    rType = "hasInRelation";
                    break;
                case R:
                    rType = "hasOutRelation";
                    break;
            }
            return rType;

        }

        public String rType() {
            String rType = "hasRelation";
            switch (direction.to()) {
                case L:
                    rType = "hasInRelation";
                    break;
                case R:
                    rType = "hasOutRelation";
                    break;
            }
            return rType;
        }

    }

    static class PropertyBuilder {

        static <T extends BaseProp> String relationTypeName(Class<T> clazz) {
            switch (clazz.getSimpleName()) {
                case "EProp":
                    return "hasEvalue";
                case "RelProp":
                    return "hasRvalue";
                default:
                    return "hasEvalue";
            }
        }

        static <T extends BaseProp> String typeName(Class<T> clazz) {
            switch (clazz.getSimpleName()) {
                case "EProp":
                    return "Evalue";
                case "RelProp":
                    return "Rvalue";
                default:
                    return "Evalue";
            }
        }

        static <T extends BaseProp> BaseProp createProp(Class<T> clazz, int eNum, String pType, Constraint con) {
            switch (clazz.getSimpleName()) {
                case "EProp":
                    return new EProp(eNum, pType, con);
                case "RelProp":
                    return new RelProp(eNum, pType, con);
                default:
                    return new EProp(eNum, pType, con);
            }
        }
    }
}
