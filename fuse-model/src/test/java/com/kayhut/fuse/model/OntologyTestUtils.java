package com.kayhut.fuse.model;

import com.kayhut.fuse.model.ontology.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.OntologyTestUtils.Color.TYPE_COLOR;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.TYPE_GENDER;
import static java.util.Collections.singletonList;

/**
 * Created by liorp on 4/27/2017.
 */
public class OntologyTestUtils {

    public static final String DATE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String CM = "cm";

    public static final RelationType OWN = new RelationType("own", 1);
    public static final RelationType MEMBER_OF = new RelationType("memberOf", 2);
    public static final RelationType FIRE = new RelationType("fire", 3);
    public static final RelationType FREEZE = new RelationType("freeze", 4);
    public static final RelationType ORIGIN = new RelationType("origin", 5);
    public static final RelationType SUBJECT = new RelationType("subject", 6);
    public static final RelationType REGISTERED = new RelationType("registered", 7);


    public static Property FIRST_NAME = new Property("firstName", STRING, 1);
    public static Property LAST_NAME = new Property("lastName", STRING, 2);
    public static Property GENDER = new Property("gender", Gender.TYPE_GENDER, 3);
    public static Property BIRTH_DATE = new Property("birthDate", STRING, 4);
    public static Property DEATH_DATE = new Property("deathDate", STRING, 5);
    public static Property HEIGHT = new Property("height", INT, 6);
    public static Property NAME = new Property("name", STRING, 7);
    public static Property COLOR = new Property("color", TYPE_COLOR, 8);

    public static Property START_DATE = new Property("startDate", DATE, 8);
    public static Property END_DATE = new Property("endDate", DATE, 9);

    public static class RelationType {
        public RelationType(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public String name;
        public int type;
    }

    public static class RelationshipTypeOf extends RelationshipType {
        private List<Property> propertiesOf;

        public RelationshipTypeOf(String name, int type, boolean dir) {
            super(name, type, dir);
        }

        public RelationshipTypeOf withEPairs(List<EPair> pairs) {
            this.setePairs(pairs);
            return this;
        }

        public RelationshipTypeOf withProperties(List<Property> properties) {
            this.propertiesOf = properties;
            return this;
        }

        public List<Integer> getProperties() {
            super.getProperties().addAll(propertiesOf.stream().map(p -> p.type).collect(Collectors.toList()));
            return super.getProperties();
        }

        @Override
        public int hashCode() {
            return propertiesOf.hashCode();
        }
    }

    public static class Property {
        public String name;
        public String className;
        public int type;

        public Property(String name, String className, int type) {
            this.name = name;
            this.className = className;
            this.type = type;
        }
    }

    public static class DRAGON {
        public static String name = "Dragon";
        public static int type = 2;
        public static List<Property> propertyList = Arrays.asList(NAME, GENDER, COLOR);

        public static List<RelationshipTypeOf> relationshipList = Arrays.asList(
                new RelationshipTypeOf(REGISTERED.name, REGISTERED.type, true)
                        .withEPairs(singletonList(new EPair(GUILD.type, KINGDOM.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(FIRE.name, FIRE.type, true)
                        .withEPairs(singletonList(new EPair(DRAGON.type, DRAGON.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(FREEZE.name, FREEZE.type, true)
                        .withEPairs(singletonList(new EPair(DRAGON.type, DRAGON.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(ORIGIN.name, ORIGIN.type, true)
                        .withEPairs(singletonList(new EPair(DRAGON.type, KINGDOM.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE))
        );

    }

    public static class HORSE {
        public static String name = "Horse";
        public static int type = 3;
        public static List<Property> propertyList = Arrays.asList(NAME, GENDER);

        public static List<RelationshipTypeOf> relationshipList = Arrays.asList(
                new RelationshipTypeOf(REGISTERED.name, REGISTERED.type, true)
                        .withEPairs(singletonList(new EPair(HORSE.type, KINGDOM.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)));
    }

    public static class GUILD {
        public static String name = "Guild";
        public static int type = 4;
        public static List<Property> propertyList = Arrays.asList(NAME);

        public static List<RelationshipTypeOf> relationshipList = Arrays.asList(
                new RelationshipTypeOf(REGISTERED.name, REGISTERED.type, true)
                        .withEPairs(singletonList(new EPair(GUILD.type, KINGDOM.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE))
        );

    }

    public static class KINGDOM {
        public static String name = "kingdom";
        public static int type = 5;
        public static List<Property> propertyList = Arrays.asList(NAME);
    }

    public static class PERSON {
        public static String name = "Person";
        public static int type = 1;


        public static List<Property> propertyList = Arrays.asList(FIRST_NAME, LAST_NAME, GENDER, BIRTH_DATE, DEATH_DATE, HEIGHT, NAME);

        public static List<RelationshipTypeOf> relationshipList = Arrays.asList(
                new RelationshipTypeOf(OWN.name, OWN.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, DRAGON.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(OWN.name, OWN.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, HORSE.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(MEMBER_OF.name, MEMBER_OF.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, GUILD.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(SUBJECT.name, SUBJECT.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, KINGDOM.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE))
        );

    }

    public static Ontology createDragonsOntologyLong() {
        Ontology ontology = createDragonsOntologyShort();

        ontology.getRelationshipTypes().addAll(PERSON.relationshipList);
        ontology.getRelationshipTypes().addAll(DRAGON.relationshipList);
        ontology.getRelationshipTypes().addAll(HORSE.relationshipList);
        ontology.getRelationshipTypes().addAll(GUILD.relationshipList);

        ontology.getProperties().add(com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(1).withName(COLOR.name).withType(TYPE_COLOR).build());

        //region EntityType1 = Person
        EntityType entityType3 = new EntityType(HORSE.type, HORSE.name, HORSE.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));
        EntityType entityType4 = new EntityType(KINGDOM.type, KINGDOM.name, KINGDOM.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));
        EntityType entityType5 = new EntityType(HORSE.type, HORSE.name, HORSE.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));

        ontology.getEntityTypes().add(entityType3);
        ontology.getEntityTypes().add(entityType4);
        ontology.getEntityTypes().add(entityType5);

        return ontology;
    }

    public static Ontology createDragonsOntologyShort() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<>();
        ontologyShortObj.setEnumeratedTypes(singletonList(EnumeratedType.from(TYPE_GENDER, Gender.values())));
        //endregion

        ontologyShortObj.setProperties(Arrays.asList(
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(1).withName(FIRST_NAME.name).withType(STRING).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(2).withName(LAST_NAME.name).withType(STRING).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(3).withName(GENDER.name).withType(TYPE_GENDER).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(4).withName(BIRTH_DATE.name).withType(DATE).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(5).withName(DEATH_DATE.name).withType(DATE).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(6).withName(HEIGHT.name).withType(INT).withUnits(CM).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(7).withName(NAME.name).withType(STRING).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(8).withName(START_DATE.name).withType(DATE).build(),
                com.kayhut.fuse.model.ontology.Property.Builder.get().withPType(9).withName(END_DATE.name).withType(DATE).build()));

        //region EntityType1 = Person
        EntityType entityType1 = new EntityType();
        entityType1.seteType(PERSON.type);
        entityType1.setName(PERSON.name);
        entityType1.setProperties(PERSON.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));
        entityTypes.add(entityType1);
        //endregion

        //region EntityType2 = Dragon
        EntityType entityType2 = new EntityType();
        entityType2.seteType(DRAGON.type);
        entityType2.setName(DRAGON.name);
        entityType2.setProperties(DRAGON.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));
        entityTypes.add(entityType2);
        //endregion

        //region EntityType2 = Guild
        EntityType entityType4 = new EntityType();
        entityType4.seteType(GUILD.type);
        entityType4.setName(GUILD.name);
        entityType4.setProperties(GUILD.propertyList.stream().map(p -> p.type).collect(Collectors.toList()));
        entityTypes.add(entityType4);
        //endregion

        ontologyShortObj.setEntityTypes(entityTypes);

        //region relationshipTypes
        List<RelationshipType> relationshipTypes = new ArrayList<>();
        relationshipTypes.addAll(Arrays.asList(
                new RelationshipTypeOf(OWN.name, OWN.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, DRAGON.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE)),
                new RelationshipTypeOf(MEMBER_OF.name, MEMBER_OF.type, true)
                        .withEPairs(singletonList(new EPair(PERSON.type, GUILD.type)))
                        .withProperties(Arrays.asList(START_DATE, END_DATE))
        ));
        //endregion

        ontologyShortObj.setRelationshipTypes(relationshipTypes);
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
}
