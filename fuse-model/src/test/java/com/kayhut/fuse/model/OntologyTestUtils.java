package com.kayhut.fuse.model;

import com.kayhut.fuse.model.ontology.*;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by liorp on 4/27/2017.
 */
public class OntologyTestUtils {

    public static Ontology createDragonsOntologyShort() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<>();

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
        ontologyShortObj.setEnumeratedTypes(enumeratedTypes);
        //endregion

        ontologyShortObj.setProperties(Arrays.asList(
                Property.Builder.get().withPType(1).withName("firstName").withType("string").build(),
                Property.Builder.get().withPType(2).withName("lastName").withType("string").build(),
                Property.Builder.get().withPType(3).withName("gender").withType("TYPE_Gender").build(),
                Property.Builder.get().withPType(4).withName("birthDate").withType("date").build(),
                Property.Builder.get().withPType(5).withName("deathDate").withType("date").build(),
                Property.Builder.get().withPType(6).withName("height").withType("int").withUnits("cm").build(),
                Property.Builder.get().withPType(7).withName("name").withType("string").build(),
                Property.Builder.get().withPType(8).withName("startDate").withType("date").build(),
                Property.Builder.get().withPType(9).withName("endDate").withType("date").build()));

        //region EntityType1 = Person
        EntityType entityType1 = new EntityType();
        entityType1.seteType(1);
        entityType1.setName("Person");
        entityType1.setProperties(Arrays.asList(1, 2, 3, 4, 5, 6));
        entityTypes.add(entityType1);
        //endregion

        //region EntityType2 = Dragon
        EntityType entityType2 = new EntityType();
        entityType2.seteType(2);
        entityType2.setName("Dragon");
        entityType2.setProperties(Collections.singletonList(7));
        entityTypes.add(entityType2);
        //endregion

        //region EntityType2 = Guild
        EntityType entityType4 = new EntityType();
        entityType4.seteType(4);
        entityType4.setName("Guild");
        entityType4.setProperties(Collections.singletonList(7));
        entityTypes.add(entityType4);
        //endregion

        ontologyShortObj.setEntityTypes(entityTypes);

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
        relationshipType1.setProperties(Arrays.asList(8, 9));

        relationshipTypes.add(relationshipType1);

        RelationshipType relationshipType2 = new RelationshipType();
        relationshipType2.setrType(2);
        relationshipType2.setName("memberOf");
        relationshipType2.setDirectional(true);
        relationshipType2.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(1);
            seteTypeB(4);
        }}));
        relationshipType2.setProperties(Arrays.asList(8, 9));
        relationshipTypes.add(relationshipType2);
        //endregion

        ontologyShortObj.setRelationshipTypes(relationshipTypes);
        return ontologyShortObj;
    }

}
