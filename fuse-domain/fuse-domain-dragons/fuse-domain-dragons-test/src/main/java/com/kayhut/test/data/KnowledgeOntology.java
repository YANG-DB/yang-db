package com.kayhut.test.data;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.kayhut.fuse.model.Utils;
import com.kayhut.fuse.model.ontology.*;
import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.ontology.Property.Builder.get;
import static com.kayhut.test.data.DragonsOntology.Color.TYPE_COLOR;
import static com.kayhut.test.data.DragonsOntology.Gender.TYPE_GENDER;
import static java.util.Collections.emptyList;

public class KnowledgeOntology {

    public static final String DATE_TYPE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String FLOAT = "float";
    public static final String CM = "cm";


    public static Property LOGICAL_ID = new Property("logicalId", STRING, "string");


    public static List<Property> properties = Arrays.asList(
           LOGICAL_ID);


    public interface IEntity {
        String name();

        String type();

        List<RelationshipType> relations();

        List<Property> properties();

    }

    public static class Property {
        public String name;
        public boolean redundant;
        public String className;
        public String type;

        public Property(String name, String className, String type) {
            this(name, className, type, false);
        }

        public Property(String name, String className, String type, boolean redundant) {
            this.name = name;
            this.className = className;
            this.type = type;
            this.redundant = redundant;
        }

    }

    /**
     * #Dragon Header
     * id,name,Power,Gender,Color
     */
    public static class Entity implements IEntity {
        public static String name = "Entity";
        public static String type = "Entity";
        public static List<Property> propertyList = Arrays.asList(LOGICAL_ID);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                );

        @Override
        public String name() {
            return name;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return relationshipList;
        }
    }

    public static Property getPropertyByName(List<Property> properties, String name) {
        return properties.stream().filter(p -> p.name.equals(name)).findFirst().get();
    }

    public static Property getPropertyByType(List<Property> properties, String type) {
        return properties.stream().filter(p -> p.type.equals(type)).findFirst().get();
    }

    public static void main(String[] args) throws IOException {
    }
}
