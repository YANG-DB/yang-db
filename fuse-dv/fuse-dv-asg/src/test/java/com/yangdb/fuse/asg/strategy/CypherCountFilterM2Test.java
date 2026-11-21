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
import com.yangdb.fuse.model.query.properties.constraint.CountConstraintOp;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * C1: {@code count(k) = n} / {@code count(k) <> n} must reach the M2 rewrite as a count filter (count op eq / ne) on
 * the relationship, and the rewritten query must pass the ASG validators.
 */
public class CypherCountFilterM2Test {
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

    private static List<CountConstraintOp> countOps(AsgQuery query) {
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(1, rels.size());
        return AsgQueryUtil.<Rel, RelPropGroup>bDescendants(rels.get(0), RelPropGroup.class).stream()
                .flatMap(group -> allProps(group.geteBase()).stream())
                .filter(p -> p.getCon() != null && p.getCon().isAggregation())
                .map(p -> p.getCon().getCountOp())
                .collect(Collectors.toList());
    }

    private static List<RelProp> allProps(RelPropGroup group) {
        List<RelProp> props = new ArrayList<>(group.getProps());
        group.getGroups().forEach(child -> props.addAll(allProps(child)));
        return props;
    }

    private static void assertCountFilterSurvives(String op, CountConstraintOp expected) {
        String cypher = "match (p1:Person)-[k:knows]->(p2:Person) where count(k) " + op + " 16 return *";
        AsgQuery query = pipeline.rewrite(cypher);
        assertEquals(cypher, Collections.singletonList(expected), countOps(query));
        ValidationResult result = validator.validate(query);
        assertTrue(cypher + ": " + result, result.valid());
    }

    @Test
    public void countEqualsSurvivesM2RewriteAndValidates() {
        assertCountFilterSurvives("=", CountConstraintOp.eq);
    }

    @Test
    public void countNotEqualsSurvivesM2RewriteAndValidates() {
        assertCountFilterSurvives("<>", CountConstraintOp.ne);
    }

    @Test
    public void countGreaterThanStillSurvives() {
        assertCountFilterSurvives(">", CountConstraintOp.gt);
    }
}
