package org.unipop.schema.property;

/*-
 * #%L
 * unipop-core
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
 * StaticPropertySchema.java - unipop-core - yangdb - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.schema.property.type.PropertyType;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StaticPropertySchema implements PropertySchema {
    protected final String key;
    protected final String value;

    public StaticPropertySchema(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Map<String, Object> toProperties(Map<String, Object> source) {
        return Collections.singletonMap(key, this.value);
    }

    @Override
    public Map<String, Object> toFields(Map<String, Object> prop) {
        Object o = prop.get(this.key);
        if(o == null || o.equals(this.value)) return Collections.emptyMap();
        return null;
    }

    @Override
    public Set<String> toFields(Set<String> propertyKeys) {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getValues(PredicatesHolder predicatesHolder) {
        return Collections.singleton(value);
    }

    @Override
    public PredicatesHolder toPredicates(PredicatesHolder predicatesHolder) {
        Set<PredicatesHolder> predicates = predicatesHolder.findKey(this.key).map(has -> {
            if (has != null && !test(has.getPredicate())) {
                return PredicatesHolderFactory.abort();
            }
            return PredicatesHolderFactory.empty();
        }).collect(Collectors.toSet());

        return PredicatesHolderFactory.create(predicatesHolder.getClause(), predicates);
    }

    @Override
    public Set<String> excludeDynamicProperties() {
        return Collections.singleton(this.key);
    }

    protected boolean test(P predicate) {
        return predicate.test(this.value);
    }

    @Override
    public String toString() {
        return "StaticPropertySchema{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static class Builder implements PropertySchemaBuilder {
        @Override
        public PropertySchema build(String key, Object conf, AbstractPropertyContainer container) {
            if (!(conf instanceof  String)) return null;
            String value = conf.toString();
            if (value.startsWith("@")) return null;
            return new StaticPropertySchema(key, value);
        }
    }
}
