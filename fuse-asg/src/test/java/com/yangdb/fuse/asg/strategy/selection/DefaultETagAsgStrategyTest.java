package com.yangdb.fuse.asg.strategy.selection;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

public class DefaultETagAsgStrategyTest {
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testType1QueryTagged() {
        DefaultETagAsgStrategy strategy = new DefaultETagAsgStrategy(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        });

        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();


        strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        List<String> eTags = AsgQueryUtil.eTags(query);
        Assert.assertEquals(Arrays.asList("ETyped[1]", "Rel[2]", "eTag"),eTags);
    }

    @Test
    public void testType2QueryTagged() {
        DefaultETagAsgStrategy strategy = new DefaultETagAsgStrategy(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        });

        AsgQuery query = AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "_A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "$B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R").next(typed(7, "entity3", "C")),
                        optional(11)
                                .next(rel(12, "rel4", R).next(typed(13, "entity4", "D")
                                        .next(optional(14).next(rel(15, "rel4", R,"R").next(typed(16, "entity4", "B")))))))
                .build();

        strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        List<String> eTags = AsgQueryUtil.eTags(query);
        Assert.assertEquals(Arrays.asList("_A", "R", "$B", "R", "C", "Rel[12]", "D", "R", "B"),eTags);
    }
}
