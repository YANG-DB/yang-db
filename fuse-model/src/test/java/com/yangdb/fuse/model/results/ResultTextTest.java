package com.yangdb.fuse.model.results;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * The text of a result row is part of the API: {@link Assignment#getId()} is a hash of {@code Assignment.toString()},
 * and {@link AssignmentsQueryResult#distinct} keys rows by {@link AssignmentDescriptor#print}. Phase 7.11 stopped
 * building these texts with String.format (it was 15-20% of the CPU of large queries); they must stay exactly the
 * same, or every row id changes.
 */
public class ResultTextTest {

    @Test
    public void propertyTextIsUnchanged() {
        for (Object value : Arrays.asList("Braden", 175, null, 1.5d)) {
            Property p = new Property("name", "raw", value);
            Assert.assertEquals(String.format("Property [pType = %s, value = %s]", "name", value), p.toString());
        }
    }

    @Test
    public void distinctKeyTextIsUnchanged() {
        Entity person = new Entity(new HashSet<>(Collections.singletonList("p")), "7", "Person", Collections.emptyMap());
        Entity dragon = new Entity(new HashSet<>(Collections.singletonList("d")), "30", "Dragon", Collections.emptyMap());
        Relationship own = Relationship.Builder.instance().withRID("dragon_30_7").withRType("Own")
                .withEID1("7").withEID2("30").build();
        Assignment<Entity, Relationship> row = Assignment.Builder.instance()
                .withEntities(Arrays.asList(person, dragon)).withRelationships(Collections.singletonList(own)).build();

        String expected = String.format("[%s,%s, %s]", "7", "p", "Person")
                + "-" + String.format("(%s, %s)", "dragon_30_7", "Own")
                + "-" + String.format("[%s,%s, %s]", "30", "d", "Dragon");
        Assert.assertEquals(expected, AssignmentDescriptor.print(row));
    }

    @Test
    public void rowsWithoutRelationshipsAreKeyedByTheirEntities() {
        Entity dragon = new Entity(new HashSet<>(Collections.singletonList("d")), "30", "Dragon", Collections.emptyMap());
        Assignment<Entity, Relationship> row = Assignment.Builder.instance()
                .withEntities(Collections.singletonList(dragon)).build();
        Assert.assertEquals(String.format("[%s,%s, %s]", "30", "d", "Dragon"), AssignmentDescriptor.print(row));
    }
}
