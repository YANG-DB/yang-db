package com.kayhut.test.data;

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

public class DragonsOntology {

    public static final String DATE_TYPE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String FLOAT = "float";
    public static final String CM = "cm";


    public static Property FIRST_NAME = new Property("firstName", STRING, 1);
    public static Property LAST_NAME = new Property("lastName", STRING, 2);
    public static Property GENDER = new Property("gender", TYPE_GENDER, 3);
    public static Property BIRTH_DATE = new Property("birthDate", DATE_TYPE, 4);
    public static Property DEATH_DATE = new Property("deathDate", STRING, 5);
    public static Property HEIGHT = new Property("height", INT, 6);
    public static Property NAME = new Property("name", STRING, 7);
    public static Property COLOR = new Property("color", TYPE_COLOR, 8);
    public static Property DATE = new Property("date", DATE_TYPE, 9);
    public static Property END_DATE = new Property("endDate", DATE_TYPE, 10);
    public static Property TEMPERATURE = new Property("temperature", INT, 11);
    public static Property TIMESTAMP = new Property("timestamp", DATE_TYPE, 12);
    public static Property ID = new Property("id", STRING, 13);
    public static Property POWER = new Property("power", INT, 14);
    public static Property DESCRIPTION = new Property("description", STRING, 15);
    public static Property ICON_ID = new Property("iconId", STRING, 16);
    public static Property URL = new Property("url", STRING, 17);
    public static Property KING = new Property("king", STRING, 18);
    public static Property QUEEN = new Property("queen", STRING, 19);
    public static Property INDEPENDENCE_DAY = new Property("independenceDay", STRING, 20);
    public static Property FUNDS = new Property("funds", FLOAT, 21);
    public static Property WEIGHT = new Property("weight", INT, 22);
    public static Property MAX_SPEED = new Property("maxSpeed", INT, 23);
    public static Property DISTANCE = new Property("distance", INT, 24);
    public static Property ESTABLISH_DATE = new Property("establishDate", DATE_TYPE, 25);
    public static Property START_DATE = new Property("startDate", DATE_TYPE, 26);


    public static List<Property> properties = Arrays.asList(
            ID,FIRST_NAME,LAST_NAME,
            GENDER,BIRTH_DATE,DEATH_DATE,
            NAME,HEIGHT,WEIGHT,MAX_SPEED,
            DISTANCE,ESTABLISH_DATE,DESCRIPTION,
            ICON_ID,URL,KING,QUEEN,
            INDEPENDENCE_DAY,FUNDS,COLOR,
            DATE,START_DATE,END_DATE,
            TEMPERATURE,TIMESTAMP,POWER);

    /**
     * #Owns Header
     * id,Source,Target,Since,Till
     */
    public static final RelationshipType OWN = new RelationshipType("owns", 101, true)
            .withProperty(ID.type, START_DATE.type, END_DATE.type);

    /**
     * #Knows Header
     * id,Source,Target,Since
     */
    public static final RelationshipType KNOW = new RelationshipType("know", 108, true)
            .withProperty(ID.type, START_DATE.type);

    /**
     * #MemberOf Header
     * id,Source,Target,Since,Till
     */
    public static final RelationshipType MEMBER_OF = new RelationshipType("memberOf", 102, true)
            .withProperty(ID.type, START_DATE.type, END_DATE.type);

    /**
     * #Fires Header
     * id,source,target,date,temperature
     */
    public static final RelationshipType FIRE = new RelationshipType("fire", 103, true)
            .withProperty(ID.type, DATE.type, TEMPERATURE.type);

    /**
     * #Freeze Header
     * id,Source,Target,Since,Till
     */
    public static final RelationshipType FREEZE = new RelationshipType("freeze", 104, true)
            .withProperty(ID.type, START_DATE.type, END_DATE.type);

    /**
     * #Originiated Header
     * id,Source,Target,Since
     */
    public static final RelationshipType ORIGIN = new RelationshipType("originated", 105, true)
            .withProperty(ID.type, START_DATE.type);

     /**
     * #SubjectOf Header
     *   id,Source,Target,Since
     */
    public static final RelationshipType SUBJECT = new RelationshipType("subjectOf", 106, true)
            .withProperty(ID.type,START_DATE.type);

    /**
     * #Registered Header
     * id,Source,Target,Since
     */
    public static final RelationshipType REGISTERED = new RelationshipType("registered", 107, true)
            .withProperty(ID.type,START_DATE.type);


    public static List<RelationshipType> relationships = Arrays.asList(
            OWN, KNOW, MEMBER_OF,
            FIRE, FREEZE, ORIGIN,
            SUBJECT,REGISTERED);

    public interface Entity {
        String name();

        int type();

        List<RelationshipType> relations();

        List<Property> properties();

    }

    public static class Property {
        public String name;
        public boolean redundant;
        public String className;
        public int type;

        public Property(String name, String className, int type) {
            this(name, className, type, false);
        }

        public Property(String name, String className, int type, boolean redundant) {
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
    public static class DRAGON implements Entity {
        public static String name = "Dragon";
        public static int type = 2;
        public static List<Property> propertyList = Arrays.asList(ID, NAME, POWER, GENDER, COLOR);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                REGISTERED.addPair(new EPair(type, GUILD.type)),
                FIRE.addPair(new EPair(type, DRAGON.type)),
                FREEZE.addPair(new EPair(type, DRAGON.type)),
                ORIGIN.addPair(new EPair(type, KINGDOM.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
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

    /**
     * #Horse Header
     * id,name,weight,maxSpeed,Distance
     */
    public static class HORSE implements Entity {
        public static String name = "Horse";
        public static int type = 3;
        public static List<Property> propertyList = Arrays.asList(ID, NAME, WEIGHT, MAX_SPEED, DISTANCE);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, GUILD.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
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

    /**
     * #Guild Header
     * id,name,description,iconId,url,establishDate
     */
    public static class GUILD implements Entity {
        public static String name = "Guild";
        public static int type = 4;
        public static List<Property> propertyList = Arrays.asList(ID, NAME, DESCRIPTION, ICON_ID, URL, ESTABLISH_DATE);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, KINGDOM.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
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

    /**
     * #Kingdom Header
     * id,name,king,queen,independenceDay,this.funds)
     */
    public static class KINGDOM implements Entity {
        public static String name = "Kingdom";
        public static int type = 5;
        public static List<Property> propertyList = Arrays.asList(ID, NAME, KING, QUEEN, INDEPENDENCE_DAY, FUNDS);

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return emptyList();
        }
    }

    /**
     * #Person Header
     * id,firstName,lastName,gender,birthDate,deathDate,height
     */
    public static class PERSON implements Entity {
        public static String name = "Person";
        public static int type = 1;


        public static List<Property> propertyList = Arrays.asList(ID, FIRST_NAME, LAST_NAME, GENDER, BIRTH_DATE, DEATH_DATE, HEIGHT);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                KNOW.addPair(new EPair(type, PERSON.type)),
                SUBJECT.addPair(new EPair(type, KINGDOM.type)),
                OWN.addPair(new EPair(type, DRAGON.type)),
                OWN.addPair(new EPair(type, HORSE.type)),
                MEMBER_OF.addPair(new EPair(type, GUILD.type)));


        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
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

    public static Ontology createDragonsOntology() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Dragons");

        //enums
        ontologyShortObj.setEnumeratedTypes(Arrays.asList(
                EnumeratedType.from(TYPE_GENDER, Gender.values()),
                EnumeratedType.from(TYPE_COLOR, Color.values())));

        //properties
        ontologyShortObj.setProperties(properties.stream().map(p->get().build(p.type,p.name,p.className)).collect(Collectors.toList()));

        //relationships
        ontologyShortObj.setRelationshipTypes(relationships);

        //entities
        ontologyShortObj.getEntityTypes().addAll(Arrays.asList(
                new EntityType(PERSON.type, PERSON.name, PERSON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(HORSE.type, HORSE.name, HORSE.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(DRAGON.type, DRAGON.name, DRAGON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(KINGDOM.type, KINGDOM.name, KINGDOM.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(GUILD.type, GUILD.name, GUILD.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList()))));

        return OntologyFinalizer.finalize(ontologyShortObj);
    }

    public static enum Gender {
        MALE, FEMALE, OTHER;
        public static final String TYPE_GENDER = "TYPE_Gender";

    }

    public static enum Color {
        RED, BLUE, GREEN, YELLOW;
        public static final String TYPE_COLOR = "TYPE_Color";

    }

    public static Property getPropertyByName(List<Property> properties, String name) {
        return properties.stream().filter(p -> p.name.equals(name)).findFirst().get();
    }

    public static Property getPropertyByType(List<Property> properties, int type) {
        return properties.stream().filter(p -> p.type == type).findFirst().get();
    }

    public static void main(String[] args) throws IOException {
        String json = Utils.asString(createDragonsOntology());
        IOUtils.write(json,new FileWriter("Dragons.json"));
    }
}
