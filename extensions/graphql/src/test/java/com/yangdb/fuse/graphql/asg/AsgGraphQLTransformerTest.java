package com.yangdb.fuse.graphql.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.graphql.translator.GraphQLSchemaUtils;
import com.yangdb.fuse.graphql.translator.GraphQLToOntologyTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.asg.strategy.M2AsgStrategyRegistrar;
import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.provider;
import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.resource;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_GRAPHQL;

/** The graphQL language (as the service creates it, once) translates query after query independently. */
public class AsgGraphQLTransformerTest {
    @Test
    public void eachQueryIsTranslatedOnItsOwn() throws Exception {
        Ontology dragons = new ObjectMapper().readValue(resource("ontology/Dragons.json"), Ontology.class);
        AsgGraphQLTransformer transformer = Guice.createInjector(binder -> {
            binder.bind(OntologyProvider.class).toInstance(provider(dragons));
            binder.bind(GraphQLSchemaUtils.class).to(GraphQLToOntologyTransformer.class);
        }).getInstance(AsgGraphQLTransformer.class);

        AsgQuery first = transformer.transform(new QueryInfo<>("{ person { name Own { name ... on Dragon { color } } } }", "q1", TYPE_GRAPHQL, "Dragons"));
        AsgQuery second = transformer.transform(new QueryInfo<>("{ horse { name } }", "q2", TYPE_GRAPHQL, "Dragons"));

        Assert.assertEquals("Dragons", second.getOnt());
        Assert.assertEquals("q2", second.getName());
        String print = AsgQueryDescriptor.print(second);
        Assert.assertTrue(print, print.contains("Horse"));
        Assert.assertFalse("the first query's elements leaked into the second: " + print,
                print.contains("Person") || print.contains("Own") || print.contains("Dragon"));
        Assert.assertTrue(first.getElements().size() > second.getElements().size());
    }

    /** G4: queries on a relationship to several types (Own: dragons and horses) pass the service's ASG rewrite (M2) and validation */
    @Test
    public void queriesOnARelationshipToSeveralTypesRewriteAndValidate() throws Exception {
        Ontology dragons = new ObjectMapper().readValue(resource("ontology/Dragons.json"), Ontology.class);
        OntologyProvider provider = provider(dragons);
        AsgGraphQLTransformer transformer = Guice.createInjector(binder -> {
            binder.bind(OntologyProvider.class).toInstance(provider);
            binder.bind(GraphQLSchemaUtils.class).to(GraphQLToOntologyTransformer.class);
        }).getInstance(AsgGraphQLTransformer.class);
        List<GraphVertexSchema> vertices = dragons.getEntityTypes().stream()
                .map(EntityType::geteType)
                .map(eType -> (GraphVertexSchema) new GraphVertexSchema.Impl(eType, new GraphElementConstraint.Impl(__.start()),
                        Optional.empty(), Optional.empty(), Collections.emptyList()))
                .collect(Collectors.toList());
        GraphElementSchemaProvider schema = new GraphElementSchemaProvider.Impl(vertices, Collections.emptyList());
        M2AsgStrategyRegistrar rewrite = new M2AsgStrategyRegistrar(provider, ont -> schema);
        AsgQueryValidator validator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), provider);
        for (String q : Arrays.asList("{ person { name Own { name } } }",
                "{ person { name Own { ... on Dragon { name } } } }",
                "{ person { name Own { ... on Horse { name weight } } } }",
                "{ person { Own { name ... on Dragon { color } ... on Horse { weight } } } }",
                "{ person { Own { name OriginatedIn { name } } } }")) {
            AsgQuery asg = transformer.transform(new QueryInfo<>(q, "q", TYPE_GRAPHQL, "Dragons"));
            Assert.assertTrue(q + ": " + validator.validate(asg), validator.validate(asg).valid());
            AsgStrategyContext context = new AsgStrategyContext(new Ontology.Accessor(dragons));
            Stream.ofAll(rewrite.register()).forEach(strategy -> strategy.apply(asg, context));
            ValidationResult result = validator.validate(asg);
            Assert.assertTrue(q + ": " + result + "\n" + AsgQueryDescriptor.print(asg), result.valid());
        }
    }
}
