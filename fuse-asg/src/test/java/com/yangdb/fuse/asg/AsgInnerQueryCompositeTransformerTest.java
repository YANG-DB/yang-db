package com.yangdb.fuse.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.yangdb.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgCompositeQuery;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.*;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.INNER;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static org.junit.Assert.*;

/**
 * Created by liorp on 09-May-17.
 */
public class AsgInnerQueryCompositeTransformerTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        ont = new ObjectMapper().readValue(writer.toString(), Ontology.class);

    }
    //endregion


    //region Test Methods
    @Test
    public void testTransformWhereByToComposite() throws IOException {
        QueryToAsgTransformer asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        });
        AsgQuery asgQuery = asgSupplier.transform(Q5());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q5().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertNotNull(inner.getOrigin());
        assertEquals("[└── Start, \n" +
                "    ──Typ[:Person P1#1]]", AsgQueryDescriptor.print(inner));
        assertEquals(Q5().getName() + INNER, inner.getOrigin().getName());
        assertTrue(AsgQueryUtil.elements(asgQuery, EProp.class).stream()
                .anyMatch(p -> p.geteBase().getCon() instanceof ParameterizedConstraint));


    }

    @Test
    public void testTransformWhereWithInnerFilterByToComposite() throws IOException {
        QueryToAsgTransformer asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        });
        AsgQuery asgQuery = asgSupplier.transform(Q6());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q6().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertNotNull(inner.getOrigin());
        assertEquals("[└── Start, \n" +
                "    ──Typ[:Person P1#1]──Q[2:all]:{20}, \n" +
                "                                  └─?[20]:[Person<contains,Jimmy>]]", AsgQueryDescriptor.print(inner));
        assertEquals(Q6().getName() + INNER, inner.getOrigin().getName());
        assertTrue(AsgQueryUtil.elements(asgQuery, EProp.class).stream()
                .anyMatch(p -> p.geteBase().getCon().getClass().isAssignableFrom(ParameterizedConstraint.class)));


    }

    @Test
    public void testTransformToComposite() throws IOException {
        QueryToAsgTransformer asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        });
        AsgQuery asgQuery = asgSupplier.transform(Q1());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q1().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertNotNull(inner.getOrigin());
        assertEquals("[└── Start, \n" +
                "    ──Typ[:Person P#1]──?[..][2], \n" +
                "                            └─?[3]:[name<like,jhon*>]]", AsgQueryDescriptor.print(inner));
        assertEquals(Q2().getName(), inner.getOrigin().getName());
        assertTrue(AsgQueryUtil.element(asgQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

    }

    @Test
    public void testTransformToMultiComposite() {
        QueryToAsgTransformer asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        });
        AsgQuery asgQuery = asgSupplier.transform(Q4());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);

        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q4().getName(), asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 2);
        assertTrue(AsgQueryUtil.element(asgQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q3().getName(), innerQuery.getOrigin().getName());

        innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(1);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q2().getName(), innerQuery.getOrigin().getName());
    }

    @Test
    @Ignore(value = "only one level hierarchy is currently supported in inner queries ")
    public void testTransformToMultiInnerComposite() {
        QueryToAsgTransformer asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        });
        AsgQuery asgQuery = asgSupplier.transform(Q0());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);

        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q0().getName(), asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);
        assertTrue(AsgQueryUtil.element(asgQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q1().getName(), innerQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) innerQuery).getQueryChain().size(), 1);

        assertTrue(AsgQueryUtil.element(innerQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery realInnerQuery = ((AsgCompositeQuery) innerQuery).getQueryChain().get(0);
        assertNotNull(realInnerQuery.getOrigin());
        assertEquals(Q2().getName(), realInnerQuery.getOrigin().getName());

    }


    private Query Q0() {
        Query query = Query.Builder.instance().withName("q0").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id", InnerQueryConstraint.of(ConstraintOp.inSet, Q1(), "P", "id")))
                )).build();
        return query;
    }

    private Query Q1() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "People", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id", InnerQueryConstraint.of(ConstraintOp.inSet, Q2(), "P", "id")))
                )).build();
        return query;
    }

    private Query Q2() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name", Constraint.of(ConstraintOp.like, "jhon*")))
                )).build();
        return query;
    }

    private Query Q3() {
        Query query = Query.Builder.instance().withName("q3").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name", Constraint.of(ConstraintOp.inSet, Arrays.asList("jhon", "george", "jim"))))
                )).build();
        return query;
    }

    private Query Q4() {
        Query query = Query.Builder.instance().withName("q4").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(4, 8), 0),
                        new Rel(4, OWN.getrType(), L, null, 5, 0),
                        new ETyped(5, "C", OntologyTestUtils.DRAGON.name, 6, 0),
                        new EPropGroup(6,
                                new EProp(7, "id", InnerQueryConstraint.of(ConstraintOp.inSet, Q2(), "P", "id"))),

                        new Rel(8, OWN.getName(), L, null, 9, 0),
                        new ETyped(9, "D", DRAGON.name, 10, 0),
                        new EProp(10, "id", InnerQueryConstraint.of(ConstraintOp.inSet, Q3(), "P", "id"))
                )).build();
        return query;
    }

    private Query Q5() {
        return Query.Builder.instance().withName("q5").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 6), 0),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType(), R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11, BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1", "creationTime"))
                )).build();
    }

    private Query Q6() {
        return Query.Builder.instance().withName("q6").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, PERSON.name, WhereByConstraint.of(ConstraintOp.contains, "Jimmy")),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType(), R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11, BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1", "creationTime"))
                )).build();
    }

    private Query Q7() {
        return Query.Builder.instance().withName("q7").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, PERSON.name, WhereByConstraint.of(ConstraintOp.contains, "Jimmy")),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", DRAGON.name, 8, 0),
                        new Quant1(8, QuantType.all, Arrays.asList(9, 12), 0),
                        new Rel(9, FIRE.getrType(), R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11, BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1", "creationTime")),
                        new Rel(12, FREEZE.getrType(), R, null, 13, 0),
                        new ETyped(13, "V3", DRAGON.type, 14, 0),
                        new EProp(14, COLOR.type, WhereByConstraint.of(ConstraintOp.ne, "V1", "stringValue"))
                )).build();
    }


    //endregion

    //region Fields
    private Ontology ont;
    //endregion

}