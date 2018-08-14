package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
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

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.quant1;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class LikeAnyConstraintTransformationAsgStrategyTest {
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

        asgStrategy = new LikeAnyConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory);
        context = new AsgStrategyContext(new Ontology.Accessor(ontology));
    }
    //endregion

    //region Tests
    @Test
    public void testLikeAnyWithoutWildcards() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3, new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.eq, "Sherley")))))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith1Wildcard_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "*Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3, new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith1Wildcard_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("She", "rley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith1Wildcard_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Wildcards_BeginningMiddle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "*She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("She", "rley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Wildcards_BeginningEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "*Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.singletonList(
                                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                                Collections.emptyList())));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Wildcards_MiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("She", "rley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                                                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2ConsecutiveWildcards_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "**Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2ConsecutiveWildcards_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "She**rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("She", "rley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2ConsecutiveWildcards_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "Sherley**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Sherley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*")))))));


        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2ConsecutiveWildcardsOnly() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertTrue(actual.getProps().isEmpty());
    }

    @Test
    public void testLikeAnyWith3Wildcards_BeginningMiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, "*She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("She", "rley")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.emptyList(),
                                Collections.singletonList(
                                        new EPropGroup(3,
                                                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                                                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")))))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Terms_PrefixAndContains() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("Sherley*", "*Windsor*")))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("Sherley", "Windsor")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.singletonList(new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Windsor")))),
                                Collections.singletonList(new EPropGroup(3, new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*")))))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Terms_ContainsWithSpaceAndContains() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*Sherley *", "*Windsor*")))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("Sherley", "Windsor")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.singletonList(new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Collections.singletonList("Windsor")))),
                                Collections.singletonList(new EPropGroup(3, new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley *")))))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeAnyWith2Terms_ContainsAndContains() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*Sherley*", "*Windsor*")))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3,
                QuantType.all,
                Collections.singletonList(
                        new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("Sherley", "Windsor")))),
                Collections.singletonList(
                        new EPropGroup(3,
                                QuantType.some,
                                Collections.singletonList(new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.inSet, Arrays.asList("Sherley", "Windsor")))),
                                Collections.emptyList())));

        Assert.assertEquals(expected, actual);
    }
    //endregion

    //region Fields
    private static AsgStrategy asgStrategy;
    private static AsgStrategyContext context;
    //endregion
}
