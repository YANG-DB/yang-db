package com.kayhut.fuse.model.results;

import javaslang.collection.Stream;
import org.junit.Assert;

import java.util.List;

/**
 * Created by Roman on 15/05/2017.
 */
public class QueryResultAssert {
    public static void assertEquals(AssignmentsQueryResult expected, AssignmentsQueryResult actual) {
        assertEquals(expected, actual, false);
    }

    public static void assertEquals(CsvQueryResult expected, CsvQueryResult actual){
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        assertIfBothNull(expected.getCsvLines(), actual.getCsvLines());
        assertIfBothNotNull(expected.getCsvLines(), actual.getCsvLines());

        Assert.assertEquals(expected.getSize(), actual.getSize());
        for (int i = 0; i < expected.getSize(); i++) {
            Assert.assertEquals(expected.getCsvLines()[i], actual.getCsvLines()[i]);
        }

    }

    //region Public Methods
    public static void assertEquals(AssignmentsQueryResult expected, AssignmentsQueryResult actual, boolean ignoreRelId) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        assertIfBothNull(expected.getAssignments(), actual.getAssignments());
        assertIfBothNotNull(expected.getAssignments(), actual.getAssignments());

        Assert.assertEquals(expected.getAssignments().size(), actual.getAssignments().size());

        List<Assignment> expectedAssignments = Stream.ofAll(expected.getAssignments())
                .sortBy(Assignment::toString).toJavaList();
        List<Assignment> actualAssignments = Stream.ofAll(actual.getAssignments())
                .sortBy(Assignment::toString).toJavaList();

        for(int i = 0 ; i < expectedAssignments.size() ; i++) {
            assertEquals(expectedAssignments.get(i), actualAssignments.get(i), ignoreRelId);
        }
    }

    public static void assertEquals(Assignment expected, Assignment actual, boolean ignoreRelId) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        assertIfBothNull(expected.getEntities(), actual.getEntities());
        assertIfBothNotNull(expected.getEntities(), actual.getEntities());

        Assert.assertEquals(expected.getEntities().size(), actual.getEntities().size());

        List<Entity> expectedEntities = Stream.ofAll(expected.getEntities())
                .sortBy(Entity::geteID).toJavaList();
        List<Entity> actualEntities = Stream.ofAll(actual.getEntities())
                .sortBy(Entity::geteID).toJavaList();

        for(int i = 0 ; i < expectedEntities.size() ; i++) {
            assertEquals(expectedEntities.get(i), actualEntities.get(i));
        }

        assertIfBothNull(expected.getRelationships(), actual.getRelationships());
        assertIfBothNotNull(expected.getRelationships(), actual.getRelationships());

        Assert.assertEquals(expected.getRelationships().size(), actual.getRelationships().size());
        if(ignoreRelId){
            List<Relationship> expectedRelationships = Stream.ofAll(expected.getRelationships())
                    .sortBy(Relationship::toString).toJavaList();
            List<Relationship> actualRelationships = Stream.ofAll(actual.getRelationships())
                    .sortBy(Relationship::toString).toJavaList();

            for (int i = 0; i < expectedRelationships.size(); i++) {
                assertEquals(expectedRelationships.get(i), actualRelationships.get(i), ignoreRelId);
            }
        }
        else {
            List<Relationship> expectedRelationships = Stream.ofAll(expected.getRelationships())
                    .sortBy(Relationship::getrID).toJavaList();
            List<Relationship> actualRelationships = Stream.ofAll(actual.getRelationships())
                    .sortBy(Relationship::getrID).toJavaList();

            for (int i = 0; i < expectedRelationships.size(); i++) {
                assertEquals(expectedRelationships.get(i), actualRelationships.get(i), ignoreRelId);
            }
        }
    }

    public static void assertEquals(Entity expected, Entity actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertEquals(expected.geteID(), actual.geteID());
        Assert.assertEquals(Stream.ofAll(expected.geteTag()).sorted().toJavaList().toString(),
                Stream.ofAll(actual.geteTag()).sorted().toJavaList().toString());
        Assert.assertTrue(expected.geteType().equals(actual.geteType()));

        assertIfBothNull(expected.getProperties(), actual.getProperties());
        assertIfBothNotNull(expected.getProperties(), actual.getProperties());

        Assert.assertEquals(expected.getProperties().size(), actual.getProperties().size());

        List<Property> expectedProperties = Stream.ofAll(expected.getProperties())
                .sortBy(Property::getpType).toJavaList();
        List<Property> actualProperties = Stream.ofAll(actual.getProperties())
                .sortBy(Property::getpType).toJavaList();

        for(int i = 0 ; i < expectedProperties.size() ; i++) {
            assertEquals(expectedProperties.get(i), actualProperties.get(i));
        }

        assertIfBothNull(expected.getAttachedProperties(), actual.getAttachedProperties());
        assertIfBothNotNull(expected.getAttachedProperties(), actual.getAttachedProperties());

        Assert.assertEquals(expected.getAttachedProperties().size(), actual.getAttachedProperties().size());

        List<AttachedProperty> expectedAttachedProperties = Stream.ofAll(expected.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();
        List<AttachedProperty> actualAttachedProperties = Stream.ofAll(actual.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();

        for(int i = 0 ; i < expectedAttachedProperties.size() ; i++) {
            assertEquals(expectedAttachedProperties.get(i), actualAttachedProperties.get(i));
        }
    }

    public static void assertEquals(Property expected, Property actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertEquals(expected.getpType(), actual.getpType());
        Assert.assertEquals(expected.getValue(), actual.getValue());
        Assert.assertEquals(expected.getAgg(), actual.getAgg());
    }

    public static void assertEquals(AttachedProperty expected, AttachedProperty actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertEquals(expected.getpName(), actual.getpName());
        Assert.assertEquals(expected.getTag(), actual.getTag());
        Assert.assertEquals(expected.getValue(), actual.getValue());
    }

    public static void assertEquals(Relationship expected, Relationship actual, boolean ignoreRelId) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);


        Assert.assertTrue(expected.getrType().equals(actual.getrType()));
        if(!ignoreRelId)
            Assert.assertEquals(expected.getrID(), actual.getrID());
        Assert.assertEquals(expected.geteID1(), actual.geteID1());
        Assert.assertEquals(expected.geteID2(), actual.geteID2());
        Assert.assertEquals(expected.geteTag1(), actual.geteTag1());
        Assert.assertEquals(expected.geteTag2(), actual.geteTag2());

        assertIfBothNull(expected.getProperties(), actual.getProperties());
        assertIfBothNotNull(expected.getProperties(), actual.getProperties());

        Assert.assertEquals(expected.getProperties().size(), actual.getProperties().size());

        List<Property> expectedProperties = Stream.ofAll(expected.getProperties())
                .sortBy(Property::getpType).toJavaList();
        List<Property> actualProperties = Stream.ofAll(actual.getProperties())
                .sortBy(Property::getpType).toJavaList();

        for(int i = 0 ; i < expectedProperties.size() ; i++) {
            assertEquals(expectedProperties.get(i), actualProperties.get(i));
        }

        assertIfBothNull(expected.getAttachedProperties(), actual.getAttachedProperties());
        assertIfBothNotNull(expected.getAttachedProperties(), actual.getAttachedProperties());

        Assert.assertEquals(expected.getAttachedProperties().size(), actual.getAttachedProperties().size());

        List<AttachedProperty> expectedAttachedProperties = Stream.ofAll(expected.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();
        List<AttachedProperty> actualAttachedProperties = Stream.ofAll(actual.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();

        for(int i = 0 ; i < expectedAttachedProperties.size() ; i++) {
            assertEquals(expectedAttachedProperties.get(i), actualAttachedProperties.get(i));
        }
    }
    //endregion

    //region Private Methods
    private static void assertIfBothNull(Object expected, Object actual) {
        if (expected == null) {
            Assert.assertTrue(actual == null);
        }
    }

    private static void assertIfBothNotNull(Object expected, Object actual) {
        Assert.assertTrue(expected != null && actual != null);
    }

    private static void assertIfBothNotEmpty(Iterable expected, Iterable actual) {
        if (expected == null || Stream.ofAll(expected).isEmpty()) {
            Assert.assertTrue(actual == null || Stream.ofAll(actual).isEmpty());
        }
    }
    //endregion
}
