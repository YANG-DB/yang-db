package com.yangdb.fuse.model;

import com.yangdb.fuse.model.ontology.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.OntologyTestUtils.Color.TYPE_COLOR;
import static com.yangdb.fuse.model.OntologyTestUtils.Gender.TYPE_GENDER;
import static com.yangdb.fuse.model.ontology.Property.Builder.get;
import static java.util.Collections.emptyList;

/**
 * Created by lior.perry on 4/27/2017.
 */
public class OntologyTestUtils {

    public static final String DATE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String CM = "cm";
    public static final String ORDER = "Order";

    public static Property FIRST_NAME = new Property("firstName", STRING, "firstName");
    public static Property LAST_NAME = new Property("lastName", STRING, "lastName");
    public static Property GENDER = new Property("gender", TYPE_GENDER, "gender");
    public static Property BIRTH_DATE = new Property("birthDate", STRING, "birthDate");
    public static Property DESCRIPTION = new Property("description", STRING, "description");
    public static Property DEATH_DATE = new Property("deathDate", STRING, "deathDate");
    public static Property HEIGHT = new Property("height", INT, "height");
    public static Property NAME = new Property("name", STRING, "name");
    public static Property COLOR = new Property("color", TYPE_COLOR, "color");
    public static Property POWER = new Property("power", INT, "power");

    public static Property START_DATE = new Property("startDate", DATE, "startDate");
    public static Property END_DATE = new Property("endDate", DATE, "endDate");
    public static Property TEMPERATURE = new Property("temperature", INT, "temperature");
    public static Property TIMESTAMP = new Property("timestamp", DATE, "timestamp");


    public static final RelationshipType OWN = new RelationshipType("own", "own", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType KNOW = new RelationshipType("know", "know", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType MEMBER_OF = new RelationshipType("memberOf", "memberOf", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType FIRE = new RelationshipType("fire", "fire", true).withProperty(START_DATE.type, END_DATE.type, TEMPERATURE.type, TIMESTAMP.type);
    public static final RelationshipType FREEZE = new RelationshipType("freeze", "freeze", true).withProperty(START_DATE.type, END_DATE.type, TEMPERATURE.type);
    public static final RelationshipType ORIGIN = new RelationshipType("origin", "origin", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType ORIGINATED_IN = new RelationshipType("originatedIn", "originatedIn", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType SUBJECT = new RelationshipType("subject", "subject", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType REGISTERED = new RelationshipType("registered", "registered", true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType HAS_PROFESSION = new RelationshipType("hasProfession", "hasProfession", true);


    public interface Entity {
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

        public Property redundant() {
            return new Property(name, className, type, true);
        }

    }

    public static class DRAGON implements Entity {
        public static String name = "Dragon";
        public static String type = "Dragon";
        public static List<Property> propertyList = Arrays.asList(NAME.redundant(), BIRTH_DATE, GENDER, COLOR);

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

    public static class HORSE implements Entity {
        public static String name = "Horse";
        public static String type = "Horse";
        public static List<Property> propertyList = Arrays.asList(NAME.redundant(), GENDER);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, GUILD.type)));

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

    public static class PROFESSION implements Entity {
        public static String name = "Profession";
        public static String type = "Profession";

        public static List<Property> propertyList = Arrays.asList(NAME.redundant(), DESCRIPTION);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                HAS_PROFESSION.addPair(new EPair(PERSON.type, type)));

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

    public static class GUILD implements Entity {
        public static String name = "Guild";
        public static String type = "Guild";
        public static List<Property> propertyList = Arrays.asList(NAME);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, KINGDOM.type)));

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

    public static class KINGDOM implements Entity {
        public static String name = "Kingdom";
        public static String type = "Kingdom";
        public static List<Property> propertyList = Arrays.asList(NAME);

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
            return emptyList();
        }
    }

    public static class PERSON implements Entity {
        public static String name = "Person";
        public static String type = "Person";


        public static List<Property> propertyList = Arrays.asList(FIRST_NAME, LAST_NAME, GENDER, BIRTH_DATE, DEATH_DATE, HEIGHT, NAME);

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

    public static Ontology createDragonsOntologyShort() {
        //no real use of partial ontology under no validation
        return createDragonsOntologyLong();
    }

    public static Ontology createDragonsOntologyLong() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Dragons");
        //enums
        ontologyShortObj.setEnumeratedTypes(new ArrayList<>(
                Arrays.asList(
                        EnumeratedType.from(TYPE_GENDER, Gender.values()),
                        EnumeratedType.from(TYPE_COLOR, Color.values())))
        );

        //properties
        ontologyShortObj.setProperties(new ArrayList<>(
                Arrays.asList(
                        get().build(FIRST_NAME.type, FIRST_NAME.name, STRING),
                        get().build(LAST_NAME.type, LAST_NAME.name, STRING),
                        get().build(GENDER.type, GENDER.name, TYPE_GENDER),
                        get().build(BIRTH_DATE.type, BIRTH_DATE.name, DATE),
                        get().build(DEATH_DATE.type, DEATH_DATE.name, DATE),
                        get().build(HEIGHT.type, HEIGHT.name, INT),
                        get().build(NAME.type, NAME.name, STRING),
                        get().build(START_DATE.type, START_DATE.name, DATE),
                        get().build(END_DATE.type, END_DATE.name, DATE),
                        get().build(TIMESTAMP.type, TIMESTAMP.name, DATE),
                        get().build(TEMPERATURE.type, TEMPERATURE.name, INT),
                        get().build(COLOR.type, COLOR.name, TYPE_COLOR)))
        );

        ontologyShortObj.setRelationshipTypes(new ArrayList<>(
                Arrays.asList(
                        KNOW,
                        REGISTERED,
                        SUBJECT,
                        ORIGIN,
                        FREEZE,
                        FIRE,
                        MEMBER_OF,
                        OWN))
        );

        //entities
        ontologyShortObj.getEntityTypes().addAll(Arrays.asList(
                new EntityType(PERSON.type, PERSON.name, PERSON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(HORSE.type, HORSE.name, HORSE.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(DRAGON.type, DRAGON.name, DRAGON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(KINGDOM.type, KINGDOM.name, KINGDOM.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(GUILD.type, GUILD.name, GUILD.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList()))));

        return OntologyFinalizer.finalize(ontologyShortObj);
    }

    public static Ontology createNestedDragonsOntologyShort() {
        Ontology ontology = createDragonsOntologyShort();

        EntityType person = ontology.getEntityTypes().stream()
                .filter(e -> e.geteType().equals(PERSON.type))
                .findAny().get();
        //add profession entity as nested type
        EntityType profession = new EntityType(PROFESSION.name, PROFESSION.name, Arrays.asList("name", "description", "certification", "salary"));
        ontology.getEntityTypes().add(profession);
        person.getProperties().add(PROFESSION.name);

        //add order entity
        EntityType dragon = ontology.getEntityTypes().stream()
                .filter(e -> e.geteType().equals(DRAGON.type))
                .findAny().get();
        EntityType order = new EntityType(ORDER, ORDER, Arrays.asList("name", "description", "statue", "rank","Origin"));
        ontology.getEntityTypes().add(order);
//        dragon.getProperties().add(ORDER);

        //add relations
        RelationshipType hasOrder = new RelationshipType("HasOrder", "hasOrder", true);
        hasOrder.getePairs().add(new EPair(DRAGON.type, order.geteType()));
        ontology.getRelationshipTypes().add(hasOrder);

        //add order entity as nested type
        EntityType origin = new EntityType("Origin", "Origin", Arrays.asList("name", "location"));
        ontology.getEntityTypes().add(origin);

        RelationshipType hasOrigin = new RelationshipType("HasOrigin", "hasOrigin", true);
        hasOrder.getePairs().add(new EPair(order.geteType(), origin.geteType()));
        ontology.getRelationshipTypes().add(hasOrigin);

        return ontology;
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

    public static Property getPropertyByType(List<Property> properties, String type) {
        return properties.stream().filter(p -> p.type.equals(type)).findFirst().get();
    }

}
