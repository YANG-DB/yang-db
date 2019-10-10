package org.unipop.schema.property;

/*-
 *
 * NonDynamicPropertySchema.java - unipop-core - yangdb - 2,016
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

import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NonDynamicPropertySchema extends DynamicPropertySchema {
    public NonDynamicPropertySchema(ArrayList<PropertySchema> otherSchemas) {
        super(otherSchemas);
    }

    @Override
    public Map<String, Object> toProperties(Map<String, Object> source) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> toFields(Map<String, Object> properties) {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> toFields(Set<String> propertyKeys) {
        return Collections.emptySet();
    }

    @Override
    public PredicatesHolder toPredicates(PredicatesHolder predicatesHolder) {
        PredicatesHolder newPredicatesHolder = super.toPredicates(predicatesHolder);
        if(newPredicatesHolder.notEmpty()) return PredicatesHolderFactory.abort();
        return newPredicatesHolder;
    }
}
