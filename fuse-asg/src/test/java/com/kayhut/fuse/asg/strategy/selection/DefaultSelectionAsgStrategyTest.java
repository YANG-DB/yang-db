package com.kayhut.fuse.asg.strategy.selection;

import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;

public class DefaultSelectionAsgStrategyTest {
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testSelectionForTypedEntity() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(eProp(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(eProp(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        DefaultSelectionAsgStrategy selectionAsgStrategy = new DefaultSelectionAsgStrategy(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        });

        selectionAsgStrategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Optional<AsgEBase<EPropGroup>> ePropGroupAsgEBase = AsgQueryUtil.element(query, 10);
        Assert.assertTrue(ePropGroupAsgEBase.isPresent());
        for (Property prop : PERSON.propertyList) {

            Assert.assertFalse(Stream.ofAll(ePropGroupAsgEBase.get().geteBase().getProps()).find(rp -> rp.getpType().equals(prop.type) && rp.getProj() instanceof IdentityProjection).isEmpty());

        }
    }

    @Test
    public void testNoSelectionForTypedEntityWithProj() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(eProp(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe")), EProp.of(100, "firstName", new IdentityProjection())))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(eProp(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        DefaultSelectionAsgStrategy selectionAsgStrategy = new DefaultSelectionAsgStrategy(new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        });

        selectionAsgStrategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Optional<AsgEBase<EPropGroup>> ePropGroupAsgEBase = AsgQueryUtil.element(query, 10);
        Assert.assertTrue(ePropGroupAsgEBase.isPresent());
        Assert.assertEquals(2, ePropGroupAsgEBase.get().geteBase().getProps().size());
    }
}
