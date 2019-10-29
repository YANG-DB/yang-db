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
 * CoalescePropertySchema.java - unipop-core - yangdb - 2,016
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.schema.property.type.PropertyType;
import org.unipop.util.PropertySchemaFactory;

import java.util.*;

/**
 * Created by sbarzilay on 8/8/16.
 */
public class CoalescePropertySchema implements ParentSchemaProperty {
    protected final String key;
    protected final List<PropertySchema> children;

    public CoalescePropertySchema(String key, List<PropertySchema> schemas) {
        this.key = key;
        this.children = schemas;
    }

    @Override
    public Collection<PropertySchema> getChildren() {
        return children;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Map<String, Object> toProperties(Map<String, Object> source) {
        Optional<Map<String, Object>> first = this.children.stream()
                .map(schema -> schema.toProperties(source)).filter(prop -> !prop.equals(Collections.emptyMap())).findFirst();
        if (!first.isPresent()) return Collections.singletonMap(key, "null");
        return first.get();
    }

    @Override
    public Set<Object> getValues(PredicatesHolder predicatesHolder) {
        return null;
    }

    @Override
    public PredicatesHolder toPredicate(HasContainer hasContainer) {
        Set<PredicatesHolder> predicates = new HashSet<>();
        children.forEach(schema -> predicates.add(schema.toPredicate(hasContainer)));
        return PredicatesHolderFactory.or(predicates);
    }

    public static class Builder implements PropertySchemaBuilder {
        @Override
        public PropertySchema build(String key, Object conf, AbstractPropertyContainer container) {
            if (!(conf instanceof JSONObject)) return null;
            JSONObject config = (JSONObject) conf;
            Object obj = config.opt("fields");
            if (obj == null || !(obj instanceof JSONArray)) return null;
            if (!config.optString("type", "String").toUpperCase().equals("COALESCE")) return null;
            JSONArray fieldsArray = (JSONArray) obj;
            List<PropertySchema> schemas = new ArrayList<>();
            for (int i = 0; i < fieldsArray.length(); i++) {
                Object field = fieldsArray.get(i);
                schemas.add(PropertySchemaFactory.createPropertySchema(key, field, container));
            }
            return new CoalescePropertySchema(key, schemas);
        }
    }
}
