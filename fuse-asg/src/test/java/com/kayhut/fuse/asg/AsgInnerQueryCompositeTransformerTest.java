package com.kayhut.fuse.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.kayhut.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgCompositeQuery;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 09-May-17.
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
        assertEquals(Q1().getName(),asgQuery.getOrigin().getName());
        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(),1);

        final AsgQuery inner = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertNotNull(inner.getOrigin());
        assertEquals(Q2().getName(),inner.getOrigin().getName());
        assertTrue(AsgQueryUtil.element(asgQuery,EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p->p.getCon().getClass().isAssignableFrom(ParameterizedConstraint.class)));

    }

    @Test
    public void testTransformToMultiComposite() throws IOException {
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
        assertEquals(Q4().getName(),asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(),2);
        assertTrue(AsgQueryUtil.element(asgQuery,EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p->p.getCon().getClass().isAssignableFrom(ParameterizedConstraint.class)));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q3().getName(),innerQuery.getOrigin().getName());

        innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(1);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q2().getName(),innerQuery.getOrigin().getName());
    }

    @Test
    public void testTransformToMultiInnerComposite() throws IOException {
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
        assertEquals(Q0().getName(),asgQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) asgQuery).getQueryChain().size(),1);
        assertTrue(AsgQueryUtil.element(asgQuery,EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p->p.getCon().getClass().isAssignableFrom(ParameterizedConstraint.class)));

        AsgQuery innerQuery = ((AsgCompositeQuery) asgQuery).getQueryChain().get(0);
        assertTrue(innerQuery instanceof AsgCompositeQuery);
        assertNotNull(innerQuery.getOrigin());
        assertEquals(Q1().getName(),innerQuery.getOrigin().getName());

        assertEquals(((AsgCompositeQuery) innerQuery).getQueryChain().size(),1);

        assertTrue(AsgQueryUtil.element(innerQuery,EPropGroup.class).get().geteBase().getProps().stream()
                .anyMatch(p->p.getCon().getClass().isAssignableFrom(ParameterizedConstraint.class)));

        AsgQuery realInnerQuery = ((AsgCompositeQuery) innerQuery).getQueryChain().get(0);
        assertNotNull(realInnerQuery.getOrigin());
        assertEquals(Q2().getName(),realInnerQuery.getOrigin().getName());

    }


    private Query Q0() {
        Query query = Query.Builder.instance().withName("q0").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q1(),"P.id")))
                )).build();
        return query;
    }

    private Query Q1() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "People", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q2(),"P.id")))
                )).build();
        return query;
    }

    private Query Q2() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name",Constraint.of(ConstraintOp.like,"jhon*")))
                )).build();
        return query;
    }

    private Query Q3() {
        Query query = Query.Builder.instance().withName("q3").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name",Constraint.of(ConstraintOp.inSet,Arrays.asList("jhon","george","jim"))))
                )).build();
        return query;
    }

    private Query Q4() {
        Query query = Query.Builder.instance().withName("q4").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(4,8),0),
                            new Rel(4, ORIGINATED_IN.getrType(), Rel.Direction.L, null, 5, 0),
                            new ETyped(5, "C", OntologyTestUtils.DRAGON.name, 6, 0),
                            new EPropGroup(6,
                                new EProp(7, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q2(),"P.id"))),

                            new Rel(8, ORIGINATED_IN.getName(), Rel.Direction.L, null, 9, 0),
                            new ETyped(9, "D", DRAGON.name, 10, 0),
                            new EProp(10, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q3(),"P.id"))
                )).build();
        return query;
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