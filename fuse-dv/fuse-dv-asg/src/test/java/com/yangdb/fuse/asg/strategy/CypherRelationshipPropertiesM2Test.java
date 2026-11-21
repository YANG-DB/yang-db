package com.yangdb.fuse.asg.strategy;

import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Q10: relationship properties written in the pattern ({@code -[f:freezes {k: v, ...}]->}, named or unnamed) must
 * reach the M2 rewrite as constraints on the relationship, and the rewritten query must pass the ASG validators.
 */
public class CypherRelationshipPropertiesM2Test {
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

    private static Set<String> relPropTypes(AsgQuery query) {
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(1, rels.size());
        return AsgQueryUtil.<Rel, RelPropGroup>bDescendants(rels.get(0), RelPropGroup.class).stream()
                .flatMap(group -> allProps(group.geteBase()).stream())
                .map(RelProp::getpType)
                .collect(Collectors.toSet());
    }

    private static List<RelProp> allProps(RelPropGroup group) {
        List<RelProp> props = group.getProps().stream().collect(Collectors.toList());
        group.getGroups().forEach(child -> props.addAll(allProps(child)));
        return props;
    }

    private static void assertRewrittenAndValid(String cypher) {
        AsgQuery query = pipeline.rewrite(cypher);
        Set<String> expected = new java.util.HashSet<>(java.util.Arrays.asList("timestamp", "duration"));
        assertEquals(cypher, expected, relPropTypes(query));
        ValidationResult result = validator.validate(query);
        assertTrue(cypher + ": " + result, result.valid());
    }

    @Test
    public void namedRelationshipPropertiesSurviveM2RewriteAndValidate() {
        assertRewrittenAndValid("match (d1:Dragon)-[f:freezes {timestamp: 373, duration: 1}]->(d2:Dragon) return *");
    }

    @Test
    public void unnamedRelationshipPropertiesSurviveM2RewriteAndValidate() {
        assertRewrittenAndValid("match (d1:Dragon)-[:freezes {timestamp: 373, duration: 1}]->(d2:Dragon) return *");
    }

    @Test
    public void patternFormMatchesWhereFormAfterM2Rewrite() {
        AsgQuery where = pipeline.rewrite(
                "match (d1:Dragon)-[f:freezes]->(d2:Dragon) where f.timestamp = 373 AND f.duration = 1 return *");
        assertEquals(relPropTypes(where),
                relPropTypes(pipeline.rewrite("match (d1:Dragon)-[f:freezes {timestamp: 373, duration: 1}]->(d2:Dragon) return *")));
    }
}
