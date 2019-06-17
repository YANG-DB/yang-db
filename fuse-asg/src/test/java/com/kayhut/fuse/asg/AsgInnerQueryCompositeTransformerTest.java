package com.kayhut.fuse.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.kayhut.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgCompositeQuery;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.*;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgCompositeQuery.INNER;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.quant1;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.WhereByConstraint.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.*;

/**
 * Created by liorp on 09-May-17.
 */
public class AsgInnerQueryCompositeTransformerTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ont = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);

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
        });
        AsgQuery asgQuery = asgSupplier.transform(Q5());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q5().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0).getQuery();
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
        });
        AsgQuery asgQuery = asgSupplier.transform(Q6());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q6().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0).getQuery();
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
        });
        AsgQuery asgQuery = asgSupplier.transform(Q1());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);
        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q1().getName(), asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0).getQuery();
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
        });
        AsgQuery asgQuery = asgSupplier.transform(Q4());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);

        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q4().getName(), asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 2);
        assertTrue(AsgQueryUtil.element(asgQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0).getQuery();
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q3().getName(), innerQuery.getOrigin().getName());

        innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(1).getQuery();
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q2().getName(), innerQuery.getOrigin().getName());
    }

    @Test
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
        });
        AsgQuery asgQuery = asgSupplier.transform(Q0());
        assertTrue(asgQuery != null);
        assertTrue(asgQuery instanceof AsgCompositeQuery);

        assertNotNull(asgQuery.getOrigin());
        assertEquals(Q0().getName(), asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(), 1);
        assertTrue(AsgQueryUtil.element(asgQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0).getQuery();
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q1().getName(), innerQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) innerQuery).getQueryChain().size(), 1);

        assertTrue(AsgQueryUtil.element(innerQuery, EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p -> p.getCon() instanceof ParameterizedConstraint));

        AsgQuery realInnerQuery = ((AsgCompositeQuery) innerQuery).getQueryChain().get(0).getQuery();
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
                        new EProp(11, BIRTH_DATE.type, of(ConstraintOp.gt, "P1", "creationTime"))
                )).build();
    }

    private Query Q6() {
        return Query.Builder.instance().withName("q6").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, PERSON.name, of(ConstraintOp.contains, "Jimmy")),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType(), R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11, BIRTH_DATE.type, of(ConstraintOp.gt, "P1", "creationTime"))
                )).build();
    }


    //endregion

    //region Private Methods
    private static String readJsonToString(String jsonRelativePath) throws Exception {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
    //endregion

    //region Fields
    private Ontology ont;
    //endregion

}