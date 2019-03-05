package com.kayhut.fuse.client.export;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.results.Entity.Builder.instance;

public class GraphMLWriterTests {
    @Before
    public void setUp() throws Exception {

/*
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(instance()
                                .withEID("id1")
                                .withProperties(Stream.of(
                                        new Property("name","me"),
                                        new Property("id",01234))
                                        .collect(Collectors.toList()))
                                .withEType("Person")
                                .withETag(Collections.singleton("P"))
                                .build()

                        )//context entity
                        .withEntity(e2.toEntity())//context entity
                        .withEntity(rel.toEntity())//context entity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();
*/

    }

    @Test
    public void simpleEntityWithRelationExport() {

    }
}
