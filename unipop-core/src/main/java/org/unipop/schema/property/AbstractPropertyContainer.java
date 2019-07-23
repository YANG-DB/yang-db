package org.unipop.schema.property;

/*-
 * #%L
 * AbstractPropertyContainer.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONObject;
import org.unipop.structure.UniGraph;
import org.unipop.util.PropertySchemaFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPropertyContainer {
    protected final UniGraph graph;
    protected final JSONObject json;
    protected ArrayList<PropertySchema> propertySchemas = new ArrayList<>();
    protected DynamicPropertySchema dynamicProperties;

    public AbstractPropertyContainer(JSONObject json, UniGraph graph) {
        this.json = json;
        this.graph = graph;
        createPropertySchemas();
    }

    public List<PropertySchema> getPropertySchemas() {
        return propertySchemas;
    }

    protected void createPropertySchemas() {
        addPropertySchema(T.id.getAccessor(), json.get(T.id.toString()));
        addPropertySchema(T.label.getAccessor(), json.get(T.label.toString()));

        JSONObject properties = json.optJSONObject("properties");
        if (properties != null) {
            properties.keys().forEachRemaining(key -> addPropertySchema(key, properties.get(key)));
        }

        Object dynamicPropertiesConfig = json.opt("dynamicProperties");
        if (dynamicPropertiesConfig instanceof Boolean && (boolean) dynamicPropertiesConfig)
            this.dynamicProperties = new DynamicPropertySchema(propertySchemas);
        else if (dynamicPropertiesConfig instanceof JSONObject)
            this.dynamicProperties = new DynamicPropertySchema(propertySchemas, (JSONObject) dynamicPropertiesConfig);
        else this.dynamicProperties = new NonDynamicPropertySchema(propertySchemas);

        propertySchemas.add(this.dynamicProperties);
    }

    protected void addPropertySchema(String key, Object value) {
        PropertySchema propertySchema = PropertySchemaFactory.createPropertySchema(key, value, this);
        propertySchemas.add(propertySchema);
    }


    @Override
    public String toString() {
        return "AbstractPropertyContainer{" +
                "dynamicProperties=" + dynamicProperties +
                ", graph=" + graph +
                ", propertySchemas=" + propertySchemas +
                '}';
    }
}
