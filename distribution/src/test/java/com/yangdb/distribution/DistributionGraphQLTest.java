package com.yangdb.distribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.graphql.asg.AsgGraphQLTransformer;
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
import java.util.List;
import java.util.stream.Collectors;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_GRAPHQL;

/**
 * GraphQL on the distribution's classpath (the jars its lib/ folder ships), with the Dragons ontology it ships.
 * <p>
 * The service's QueryDriverBase.run reports any Error thrown while creating a query as "failed building the cursor
 * request ...": that is how a GraphQL parser that cannot load on the shipped ANTLR runtime showed up in the e2e suite,
 * while the extension's own tests (a different classpath) passed.
 */
public class DistributionGraphQLTest {
    private static Ontology dragons() throws Exception {
        return new ObjectMapper().readValue(new File("resources/assembly/Dragons/ontology/Dragons.json"), Ontology.class);
    }

    private static OntologyProvider provider(Ontology dragons) {
        return new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(dragons);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singletonList(dragons);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        };
    }

    private static AsgGraphQLTransformer transformer(OntologyProvider provider) {
        return Guice.createInjector(binder -> {
            binder.bind(OntologyProvider.class).toInstance(provider);
            binder.bind(GraphQLSchemaUtils.class).to(GraphQLToOntologyTransformer.class);
        }).getInstance(AsgGraphQLTransformer.class);
    }

    @Test
    public void theGraphQLExtensionTranslatesADragonsQueryOnTheShippedClasspath() throws Exception {
        AsgQuery asg = transformer(provider(dragons()))
                .transform(new QueryInfo<>("{ dragon { name color } }", "q1", TYPE_GRAPHQL, "Dragons"));
        Assert.assertTrue(AsgQueryDescriptor.print(asg), AsgQueryDescriptor.print(asg).contains("Dragon"));
    }

    /** the playground's GraphQL examples (GQ1-GQ3) pass the service's ASG rewrite (M2) and validation */
    @Test
    public void thePlaygroundGraphQLExamplesRewriteAndValidate() throws Exception {
        Ontology dragons = dragons();
        OntologyProvider provider = provider(dragons);
        List<GraphVertexSchema> vertices = dragons.getEntityTypes().stream()
                .map(EntityType::geteType)
                .map(eType -> (GraphVertexSchema) new GraphVertexSchema.Impl(eType, new GraphElementConstraint.Impl(__.start()),
                        Optional.empty(), Optional.empty(), Collections.emptyList()))
                .collect(Collectors.toList());
        GraphElementSchemaProvider schema = new GraphElementSchemaProvider.Impl(vertices, Collections.emptyList());
        M2AsgStrategyRegistrar rewrite = new M2AsgStrategyRegistrar(provider, ont -> schema);
        AsgQueryValidator validator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), provider);
        AsgGraphQLTransformer transformer = transformer(provider);
        for (String q : Arrays.asList("{ dragon { name color } }",
                "{ dragon(where: {operator: AND, constraints: [{operand: \"color\", operator: \"eq\", expression: \"BLACK\"}]}) { name color } }",
                "{ person { name Own { name } } }")) {
            AsgQuery asg = transformer.transform(new QueryInfo<>(q, "q", TYPE_GRAPHQL, "Dragons"));
            Assert.assertTrue(q + ": " + validator.validate(asg), validator.validate(asg).valid());
            AsgStrategyContext context = new AsgStrategyContext(new Ontology.Accessor(dragons));
            Stream.ofAll(rewrite.register()).forEach(strategy -> strategy.apply(asg, context));
            ValidationResult result = validator.validate(asg);
            Assert.assertTrue(q + ": " + result + "\n" + AsgQueryDescriptor.print(asg), result.valid());
        }
    }

    @Test
    public void theGraphQLParserUsesItsOwnAntlrRuntime() throws Exception {
        // graphql-java >= 20 ships a relocated copy of ANTLR; its parser must never depend on the shared runtime
        Class<?> shaded = Class.forName("graphql.org.antlr.v4.runtime.RuntimeMetaData");
        Assert.assertEquals(shaded.getProtectionDomain().getCodeSource().getLocation(),
                graphql.parser.Parser.class.getProtectionDomain().getCodeSource().getLocation());
    }

    @Test
    public void theShippedAntlrRuntimeIsTheOnePainlessWasGeneratedWith() {
        // lang-painless 2.19.7's parser is generated with ANTLR 4.13.1 (the serialized ATN changed in 4.10, so
        // older runtimes cannot load it); graphql-java no longer competes for this artifact
        Assert.assertTrue(org.antlr.v4.runtime.RuntimeMetaData.getRuntimeVersion(),
                org.antlr.v4.runtime.RuntimeMetaData.getRuntimeVersion().startsWith("4.13"));
    }
}
