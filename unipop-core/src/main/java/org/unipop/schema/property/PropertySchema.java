package org.unipop.schema.property;

/*-
 *
 * PropertySchema.java - unipop-core - yangdb - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PropertySchema {
    String getKey();
    Map<String, Object> toProperties(Map<String, Object> source);
    Map<String, Object> toFields(Map<String, Object> properties);
    Set<String> toFields(Set<String> propertyKeys);
    Set<Object> getValues(PredicatesHolder predicatesHolder);
    default PredicatesHolder toPredicates(PredicatesHolder predicatesHolder){
        Stream<HasContainer> hasContainers = predicatesHolder.findKey(getKey());

        Set<PredicatesHolder> predicateHolders = hasContainers.map(this::toPredicate).collect(Collectors.toSet());
        return PredicatesHolderFactory.create(predicatesHolder.getClause(), predicateHolders);
    }
    default PredicatesHolder toPredicate(HasContainer hasContainer) { return null; }

    default Set<String> excludeDynamicFields() { return Collections.emptySet(); }
    default Set<String> excludeDynamicProperties() { return Collections.singleton(getKey()); }

    interface PropertySchemaBuilder {
        PropertySchema build(String key, Object conf, AbstractPropertyContainer container);
    }
}
