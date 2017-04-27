package com.kayhut.fuse.model;

import com.kayhut.fuse.model.ontology.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liorp on 4/27/2017.
 */
public class OntologyTestUtils {

    public static Ontology createDragonsOntologyShort(Ontology ontology) {
        ontology.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<>();

        //region EntityType1 = Person
        EntityType entityType1 = new EntityType();
        entityType1.seteType(1);
        entityType1.setName("Person");
        List<Property> entityType1Properties = new ArrayList<>();

        Property entityType1Prop1 = new Property() {{
            setpType(1);
            setName("first name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop2 = new Property() {{
            setpType(2);
            setName("last name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop3 = new Property() {{
            setpType(3);
            setName("gender");
            setType("TYPE_Gender");
        }};

        Property entityType1Prop4 = new Property() {{
            setpType(4);
            setName("birth date");
            setType("date");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop5 = new Property() {{
            setpType(5);
            setName("death date");
            setType("date");
        }};

        Property entityType1Prop6 = new Property() {{
            setpType(6);
            setName("height");
            setType("int");
            setUnits("cm");
            setReport(Arrays.asList("raw"));
        }};

        entityType1Properties.addAll(Arrays.asList(entityType1Prop1, entityType1Prop2, entityType1Prop3, entityType1Prop4, entityType1Prop5, entityType1Prop6));
        entityType1.setProperties(entityType1Properties);
        entityType1.setDisplay(Arrays.asList("%1 %2", "%4", "%6"));
        entityTypes.add(entityType1);
        //endregion

        //region EntityType2 = Dragon
        EntityType entityType2 = new EntityType();
        entityType2.seteType(2);
        entityType2.setName("Dragon");
        List<Property> entityType2Properties = new ArrayList<>();

        Property entityType2Prop1 = new Property() {{
            setpType(1);
            setName("name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        entityType2Properties.addAll(Arrays.asList(entityType2Prop1));
        entityType2.setProperties(entityType2Properties);
        entityType2.setDisplay(Arrays.asList("name: %1"));
        entityTypes.add(entityType2);
        //endregion

        //region Future Testing
//        EntityType entityType3 = new EntityType();
//        entityType3.seteType(3);
//        entityType3.setName("Horse");
//        List<Property> entityType3Properties = new ArrayList<Property>();
//
//        Property entityType3Prop1 = new Property() {{
//            setpType(1);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        Property entityType3Prop2 = new Property() {{
//            setpType(2);
//            setName("color");
//            setType("TYPE_HorseColor");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        Property entityType3Prop3 = new Property() {{
//            setpType(3);
//            setName("weight");
//            setType("int");
//            setUnits("Kg");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType3Properties.addAll(Arrays.asList(entityType3Prop1, entityType3Prop2, entityType3Prop3));
//        entityType3.setProperties(entityType3Properties);
//        entityType3.setDisplay(Arrays.asList("%1", "%2", "%3"));
//        entityTypes.add(entityType3);
//
//        EntityType entityType4 = new EntityType();
//        entityType4.seteType(4);
//        entityType4.setName("Guild");
//        List<Property> entityType4Properties = new ArrayList<Property>();
//
//        Property entityType4Prop1 = new Property() {{
//            setpType(1);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType4Properties.addAll(Arrays.asList(entityType4Prop1));
//        entityType4.setProperties(entityType4Properties);
//        entityTypes.add(entityType4);
//
//        EntityType entityType5 = new EntityType();
//        entityType5.seteType(5);
//        entityType5.setName("Kingdom");
//        List<Property> entityType5Properties = new ArrayList<Property>();
//
//        Property entityType5Prop1 = new Property() {{
//            setpType(5);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType5Properties.addAll(Arrays.asList(entityType5Prop1));
//        entityType5.setProperties(entityType5Properties);
//        entityType5.setDisplay(Arrays.asList("%1"));
//        entityTypes.add(entityType5);
        //endregion

        ontology.setEntityTypes(entityTypes);

        //region relationshipTypes
        List<RelationshipType> relationshipTypes = new ArrayList<>();

        RelationshipType relationshipType1 = new RelationshipType();
        relationshipType1.setrType(1);
        relationshipType1.setName("own");
        relationshipType1.setDirectional(true);
        relationshipType1.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(1);
            seteTypeB(2);
        }}));
        relationshipType1.setProperties(Arrays.asList(new Property() {{
            setpType(1);
            setName("since");
            setType("date");
            setReport(Arrays.asList("min", "max"));
        }}, new Property() {{
            setpType(2);
            setName("till");
            setType("date");
            setReport(Arrays.asList("min", "max"));
        }}));

        relationshipTypes.add(relationshipType1);

        RelationshipType relationshipType2 = new RelationshipType();
        relationshipType2.setrType(2);
        relationshipType2.setName("fires at");
        relationshipType2.setDirectional(true);
        relationshipType2.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(2);
            seteTypeB(2);
        }}));
        relationshipType2.setProperties(Arrays.asList(new Property() {{
            setpType(1);
            setName("time");
            setType("datetime");
            setReport(Arrays.asList("min", "max"));
        }}));

        relationshipTypes.add(relationshipType2);
        //endregion

        //region enumeratedTypes
        List<EnumeratedType> enumeratedTypes = new ArrayList<>();

        EnumeratedType enumeratedType1 = new EnumeratedType() {{
            seteType("TYPE_Gender");
            setValues(Arrays.asList(new Value() {{
                setName("Female");
                setVal(1);
            }}, new Value() {{
                setName("Male");
                setVal(2);
            }}, new Value() {{
                setName("Other");
                setVal(3);
            }}));
        }};

        enumeratedTypes.add(enumeratedType1);
        //endregion

        ontology.setRelationshipTypes(relationshipTypes);
        ontology.setEnumeratedTypes(enumeratedTypes);

        return ontology;
    }

}
