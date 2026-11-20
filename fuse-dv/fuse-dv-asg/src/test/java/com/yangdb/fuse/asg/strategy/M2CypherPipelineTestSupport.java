package com.yangdb.fuse.asg.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.asg.translator.cypher.CypherTranslator;
import com.yangdb.fuse.asg.translator.cypher.M1CypherAsgStrategyRegistrar;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;

/**
 * Runs a Cypher query the way the service is configured (application.conf: M1 Cypher translator, then
 * {@link M2AsgStrategyRegistrar}) against the Dragons ontology. Every vertex type gets a {@code name} property with an
 * exact ({@code name.keyword}) index, so the schema-aware strategies actually rewrite constraints.
 */
public class M2CypherPipelineTestSupport {
    public final Ontology ontology;
    private final CypherTranslator translator;
    private final M2AsgStrategyRegistrar registrar;

    public M2CypherPipelineTestSupport() throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json")) {
            ontology = new ObjectMapper().readValue(stream, Ontology.class);
        }
        OntologyProvider ontologyProvider = new OntologyProvider() {
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
        };

        List<GraphVertexSchema> vertexSchemas = ontology.getEntityTypes().stream()
                .map(EntityType::geteType)
                .map(eType -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                        eType,
                        new GraphElementConstraint.Impl(__.start()),
                        Optional.empty(),
                        Optional.empty(),
                        Collections.singletonList(new GraphElementPropertySchema.Impl("name", "string",
                                Collections.singletonList(new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword"))))))
                .collect(Collectors.toList());
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(vertexSchemas, Collections.emptyList());

        translator = new CypherTranslator(new M1CypherAsgStrategyRegistrar(ontologyProvider, null));
        registrar = new M2AsgStrategyRegistrar(ontologyProvider, ont -> schemaProvider);
    }

    public AsgQuery translate(String cypher) {
        return translator.translate(new QueryInfo<>(cypher, "q", TYPE_CYPHERQL, ontology.getOnt()));
    }

    /** Translate, then apply every M2 strategy in registration order, like {@code AsgQueryTransformer}. */
    public AsgQuery rewrite(String cypher) {
        AsgQuery query = translate(cypher);
        AsgStrategyContext context = new AsgStrategyContext(new Ontology.Accessor(ontology));
        Stream.ofAll(registrar.register()).forEach(strategy -> strategy.apply(query, context));
        return query;
    }
}
