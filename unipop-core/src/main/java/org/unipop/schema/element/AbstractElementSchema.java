package org.unipop.schema.element;

/*-
 * #%L
 * AbstractElementSchema.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import org.apache.tinkerpop.gremlin.structure.Element;
import org.json.JSONObject;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.schema.property.AbstractPropertyContainer;
import org.unipop.schema.property.NonDynamicPropertySchema;
import org.unipop.structure.UniElement;
import org.unipop.structure.UniGraph;
import org.unipop.util.ConversionUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractElementSchema<E extends Element> extends AbstractPropertyContainer implements ElementSchema<E> {
    protected UniGraph graph;

    public AbstractElementSchema(JSONObject configuration, UniGraph graph) {
        super(configuration, graph);
        this.graph = graph;
    }

    public UniGraph getGraph() {
        return graph;
    }

    protected Map<String, Object> getProperties(Map<String, Object> source) {
        List<Map<String, Object>> fieldMaps = this.getPropertySchemas().stream().map(schema ->
                schema.toProperties(source)).collect(Collectors.toList());

        return ConversionUtils.merge(fieldMaps, this::mergeProperties, false);
    }

    protected Object mergeProperties(Object prop1, Object prop2) {
        if(!prop1.equals(prop2))
            System.out.println("merging unequal properties '" + prop1 + "' and '" + prop2 + "', schema: " + this);
        return prop1;
    }

    @Override
    public Map<String, Object> toFields(E element) {
        return getFields(element);
    }

    protected Map<String, Object> getFields(E element) {
        Map<String, Object> properties = UniElement.fullProperties(element);
        if(properties == null) return null;

        List<Map<String, Object>> fieldMaps = this.getPropertySchemas().stream().map(schema ->
                schema.toFields(properties)).collect(Collectors.toList());
        return ConversionUtils.merge(fieldMaps, this::mergeFields, false);
    }

    protected Object mergeFields(Object obj1, Object obj2) {
        if(!obj1.equals(obj2))
            System.out.println("merging unequal fields '" + obj1 + "' and '" + obj2 + "', schema: " + this);
        return obj1;
    }

    @Override
    public Set<String> toFields(Set<String> propertyKeys) {
        return getPropertySchemas().stream().flatMap(propertySchema ->
                propertySchema.toFields(propertyKeys).stream()).collect(Collectors.toSet());
    }

    @Override
    public String getFieldByPropertyKey(String key){
        Optional<String> first = propertySchemas.stream().filter(s -> s.getKey() != null).filter(s -> s.getKey().equals(key)).flatMap(s -> s.toFields(Collections.singleton(key)).stream()).findFirst();
        if (first.isPresent()) return first.get();
        else
            if (dynamicProperties instanceof NonDynamicPropertySchema) return null;
            else return key;
    }

    @Override
    public PredicatesHolder toPredicates(PredicatesHolder predicatesHolder) {
        Set<PredicatesHolder> predicates = getPropertySchemas().stream()
                .map(schema -> schema.toPredicates(predicatesHolder))
                .filter(holder -> holder != null)
                .collect(Collectors.toSet());

        return PredicatesHolderFactory.create(predicatesHolder.getClause(), predicates);
    }

    @Override
    public String toString() {
        return "AbstractElementSchema{" +
                "graph=" + graph +
                "} " + super.toString();
    }
}
