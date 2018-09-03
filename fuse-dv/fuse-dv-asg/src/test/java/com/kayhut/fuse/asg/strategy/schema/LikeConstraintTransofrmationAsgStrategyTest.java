package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class LikeConstraintTransofrmationAsgStrategyTest {
    //region Setup
    @BeforeClass
    public static void setup() {
        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withEntityTypes(Collections.singletonList(
                        EntityType.Builder.get().withEType("Person").withName("Person").withProperties(
                                Collections.singletonList("name")).build()))
                .withProperties(Collections.singletonList(
                        Property.Builder.get().withPType("name").withName("name").withType("string").build()))
                .build();

        OntologyProvider ontologyProvider = new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        };

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Collections.singletonList(
                        new GraphVertexSchema.Impl(
                                "Person",
                                new GraphElementConstraint.Impl(__.start()),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.singletonList(
                                        new GraphElementPropertySchema.Impl("name", "string", Arrays.asList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword"),
                                                new GraphElementPropertySchema.NgramsIndexingSchema.Impl("name.ngrams", 10)
                                        ))
                                )
                        )
                ),
                Collections.emptyList());

        GraphElementSchemaProviderFactory schemaProviderFactory = ontology1 -> schemaProvider;

        asgStrategy = new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory);
        context = new AsgStrategyContext(new Ontology.Accessor(ontology));
    }
    //endregion

    //region Tests
    @Test
    public void testLikeWithoutWildcards() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Collections.singletonList(
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.eq, "Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_BeginningMiddle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_BeginningEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Collections.singletonList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_MiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "**Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She**rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcardsOnly() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertTrue(actual.getProps().isEmpty());
    }

    @Test
    public void testLikeWith3Wildcards_BeginningMiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley"))));

        Assert.assertEquals(expected, actual);
    }
    //endregion

    //region Fields
    private static AsgStrategy asgStrategy;
    private static AsgStrategyContext context;
    //endregion
}
