package com.kayhut.fuse.neo4j.cypher;

import java.util.List;


/**
 * Created by EladW on 23/02/2017.
 */
public class Schema {

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

    public String getPropertyField(String pName) {
        return pName.replace(' ','_');
    }


}
