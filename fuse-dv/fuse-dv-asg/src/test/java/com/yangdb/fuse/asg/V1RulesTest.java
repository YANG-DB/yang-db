package com.yangdb.fuse.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.asg.strategy.M2AsgStrategyRegistrar;
import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Checks what docs/spec/v1/rules.md says the engine does with each example. Every example is marked
 * {@code <!-- engine: accepts -->} or {@code <!-- engine: rejects "<part of the error>" -->} and goes through the
 * steps {@code POST /fuse/query/v1/run} takes before planning: the composite transformer, the M2 rewrite strategies
 * (the Dragons distribution's registrar) and the validators, with the Dragons ontology the distribution ships.
 * A rejection is the validators' errors, or the message of whatever a step throws.
 */
public class V1RulesTest {
    private static final Path REPO = Paths.get("../..").toAbsolutePath().normalize();
    private static final Pattern EXAMPLE = Pattern.compile(
            "<!-- engine: (accepts|rejects \"([^\"]+)\") -->\\s*\\n```json\\s*\\n(\\{.*?)\\n```", Pattern.DOTALL);

    private static Ontology ontology;
    private static QueryToCompositeAsgTransformer transformer;
    private static AsgQueryTransformer rewriter;
    private static AsgQueryValidator validator;

    @BeforeClass
    public static void setup() throws Exception {
        ontology = new ObjectMapper().readValue(
                REPO.resolve("distribution/resources/assembly/Dragons/ontology/Dragons.json").toFile(), Ontology.class);
        OntologyProvider ontologyProvider = new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return ontology.getOnt().equals(id) ? Optional.of(ontology) : Optional.empty();
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
        // one vertex schema per entity type, as in M2CypherPipelineTestSupport: enough for the rewrite strategies
        List<GraphVertexSchema> vertexSchemas = ontology.getEntityTypes().stream()
                .map(EntityType::geteType)
                .map(eType -> (GraphVertexSchema) new GraphVertexSchema.Impl(eType, new GraphElementConstraint.Impl(__.start()),
                        Optional.empty(), Optional.empty(), Collections.emptyList()))
                .collect(Collectors.toList());
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(vertexSchemas, Collections.emptyList());

        transformer = new QueryToCompositeAsgTransformer(ontologyProvider);
        rewriter = new AsgQueryTransformer(new M2AsgStrategyRegistrar(ontologyProvider, ont -> schemaProvider), ontologyProvider);
        validator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), ontologyProvider);
    }

    /** "accepted", or the errors the steps of /run report before planning */
    private static String outcome(Query query) {
        try {
            AsgQuery asg = rewriter.transform(transformer.transform(query));
            ValidationResult result = validator.validate(asg);
            return result.valid() ? "accepted" : String.join("; ", Stream.ofAll(result.errors()).toJavaList());
        } catch (Throwable e) {
            return e.toString();
        }
    }

    @Test
    public void rulesPageExamplesDoWhatThePageSays() throws Exception {
        String page = new String(Files.readAllBytes(REPO.resolve("docs/spec/v1/rules.md")), StandardCharsets.UTF_8);
        Matcher m = EXAMPLE.matcher(page);
        ObjectMapper mapper = new ObjectMapper();
        List<String> wrong = new ArrayList<>();
        int checked = 0;
        while (m.find()) {
            checked++;
            String outcome = outcome(mapper.readValue(m.group(3), Query.class));
            boolean accepts = m.group(1).equals("accepts");
            if (accepts ? !outcome.equals("accepted") : !outcome.contains(m.group(2))) {
                wrong.add("example #" + checked + " should " + m.group(1) + " but: " + outcome);
            }
        }
        assertTrue("expected the rules page's examples, found " + checked, checked >= 15);
        assertTrue(String.join("\n", wrong), wrong.isEmpty());
    }
}
