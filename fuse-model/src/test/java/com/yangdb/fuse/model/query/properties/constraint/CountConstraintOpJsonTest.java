package com.yangdb.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Pins the JSON wire format of {@link CountConstraintOp} to what Jackson 2.13 produced (Phase 5 upgrade to 2.18).
 */
public class CountConstraintOpJsonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void everyOpKeepsItsOldWireName() throws Exception {
        for (CountConstraintOp op : CountConstraintOp.values()) {
            String expected = op == CountConstraintOp.between ? "within" : op.name();
            Assert.assertEquals(op.name(), "\"" + expected + "\"", mapper.writeValueAsString(op));
        }
    }

    @Test
    public void wireNamesReadBack() throws Exception {
        for (CountConstraintOp op : CountConstraintOp.values()) {
            if (op == CountConstraintOp.between) {
                continue;
            }
            Assert.assertEquals(op, mapper.readValue("\"" + op.name() + "\"", CountConstraintOp.class));
        }
    }

    @Test(expected = ValueInstantiationException.class)
    public void betweenIsNotAcceptedOnTheWire() throws Exception {
        mapper.readValue("\"between\"", CountConstraintOp.class);
    }
}
