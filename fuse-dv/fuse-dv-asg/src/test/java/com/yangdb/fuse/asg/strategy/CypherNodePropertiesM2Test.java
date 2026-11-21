package com.yangdb.fuse.asg.strategy;

import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Q12: node-pattern properties ({@code (p:Person {k: v, ...})}, named or unnamed) must reach the M2 rewrite as
 * constraints on the node, and the rewritten query must pass the ASG validators.
 */
public class CypherNodePropertiesM2Test {
    private static M2CypherPipelineTestSupport pipeline;
    private static AsgQueryValidator validator;

    @BeforeClass
    public static void setUp() throws Exception {
        pipeline = new M2CypherPipelineTestSupport();
        validator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(pipeline.ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(pipeline.ontology);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        });
    }

    private static Set<String> propTypes(AsgQuery query) {
        return AsgQueryUtil.elements(query, EPropGroup.class).stream()
                .flatMap(group -> allProps(group.geteBase()).stream())
                // DefaultSelectionAsgStrategy adds projection-only props: keep the real constraints
                .filter(p -> p.getCon() != null && !p.isProjection())
                .map(EProp::getpType)
                .collect(Collectors.toSet());
    }

    private static List<EProp> allProps(EPropGroup group) {
        List<EProp> props = new ArrayList<>(group.getProps());
        group.getGroups().forEach(child -> props.addAll(allProps(child)));
        return props;
    }

    private static void assertRewrittenAndValid(String cypher) {
        AsgQuery query = pipeline.rewrite(cypher);
        Set<String> types = propTypes(query);
        assertTrue(cypher + ": " + types, types.containsAll(Arrays.asList("gender", "firstName")));
        ValidationResult result = validator.validate(query);
        assertTrue(cypher + ": " + result, result.valid());
    }

    @Test
    public void namedNodePropertiesSurviveM2RewriteAndValidate() {
        assertRewrittenAndValid("match (p:Person {gender: 'Female', firstName: 'Braden'}) return *");
    }

    @Test
    public void unnamedNodePropertiesSurviveM2RewriteAndValidate() {
        assertRewrittenAndValid("match (:Person {gender: 'Female', firstName: 'Braden'})-[k:knows]->(p2:Person) return *");
    }
}
