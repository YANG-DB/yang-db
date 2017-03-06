package com.kayhut.fuse.neo4j.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;


/**
 * Created by EladW on 23/02/2017.
 */
public class Schema {

    private Ontology ontology;

    public static class Entity {
        public Entity() {

        }
        public int eType;
        public String name;
        public List<Property> properties;
        public List<String> display;
    }

    public static class EntityPair {
        public EntityPair() {

        }
        public int eTypeA;
        public int eTypeB;
    }

    public static class Relation {
        public Relation() {

        }
        public int rType;
        public String name;
        public boolean directional;
        public List<EntityPair> ePairs;
        public List<Property> properties;
    }

    public static class Property {
        public Property() {

        }
        public int pType;
        public String name;
        public String type;
        public String units;
        public List<String> report;
    }

    public static class EnumVal {
        public EnumVal() {

        }
        public int val;
        public String name;
    }

    public static class Enum {

        public Enum() {

        }

        public String eType;
        public List<EnumVal> values;
    }

    public static class Ontology {

        public Ontology(){

        }

        public String ont;
        public List<Entity> entityTypes;
        public List<Relation> relationshipTypes;
        public List<Enum> enumeratedTypes;

        public String getName() {
            return ont;
        }
    }

    public String getName() {
        return ontology.getName();
    }

    public String getEntityLabel(int eType) {
        for(Entity e : ontology.entityTypes) {
            if(e.eType == eType) {
                return e.name;
            }
        }
        return null;
    }

    public String getRelationLabel(int rType) {
        for(Relation r : ontology.relationshipTypes) {
            if(r.rType == rType) {
                return r.name;
            }
        }
        return null;
    }

    public Property getProperty(int eType, int pType) {
        for(Entity e : ontology.entityTypes) {
            if(e.eType == eType) {
                for(Property p : e.properties) {
                    if(p.pType == pType) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public String getPropertyField(String pName) {
        return pName.replace(' ','_');
    }

    public void load(String ontJson) {

        ObjectMapper mapper = new ObjectMapper();

        try {

            ontology = mapper.readValue(ontJson, Ontology.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
