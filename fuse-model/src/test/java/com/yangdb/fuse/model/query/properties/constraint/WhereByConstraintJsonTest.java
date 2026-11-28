package com.yangdb.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/** Q17: a V1 query may leave joinType out; it then follows the operator, as the Java constructors do. */
public class WhereByConstraintJsonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private WhereByConstraint read(String json) throws Exception {
        return (WhereByConstraint) mapper.readValue(json, Constraint.class);
    }

    @Test
    public void missingJoinTypeFollowsTheOperator() throws Exception {
        WhereByConstraint single = read("{\"type\":\"WhereByConstraint\",\"op\":\"eq\",\"tagEntity\":\"A\",\"projectedField\":\"name\"}");
        Assert.assertEquals(WhereByFacet.JoinType.FOR_EACH, single.getJoinType());
        WhereByConstraint multi = read("{\"type\":\"WhereByConstraint\",\"op\":\"in set\",\"tagEntity\":\"A\",\"projectedField\":\"name\"}");
        Assert.assertEquals(WhereByFacet.JoinType.FULL, multi.getJoinType());
    }

    @Test
    public void givenJoinTypeIsKept() throws Exception {
        WhereByConstraint c = read("{\"type\":\"WhereByConstraint\",\"op\":\"eq\",\"tagEntity\":\"A\",\"projectedField\":\"name\",\"joinType\":\"FULL\"}");
        Assert.assertEquals(WhereByFacet.JoinType.FULL, c.getJoinType());
    }
}
