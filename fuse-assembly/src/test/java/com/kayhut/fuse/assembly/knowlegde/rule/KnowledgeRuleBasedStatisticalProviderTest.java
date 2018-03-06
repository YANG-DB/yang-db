package com.kayhut.fuse.assembly.knowlegde.rule;

import com.kayhut.fuse.assembly.knowlegde.KnowledgeRuleBasedStatisticalProvider;
import com.kayhut.fuse.assembly.knowlegde.KnowlegdeOntology;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry on 3/5/2018.
 */
public class KnowledgeRuleBasedStatisticalProviderTest {
    private KnowledgeRuleBasedStatisticalProvider statisticalProvider;
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        statisticalProvider = new KnowledgeRuleBasedStatisticalProvider();
        ontology = KnowlegdeOntology.createOntology();
    }

    @Test
    public void testNodeStatistics() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 500000,0.5);
        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 1000000,0.5);
        eTyped = new ETyped(1, "a", "reference", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 500000,0.5);
        eTyped = new ETyped(1, "a", "insight", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped).getTotal(), 25000,0.5);
    }

    @Test
    public void testNodeFilterStatisticsSingleEprop() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity", 0, 0);
        EPropGroup group = new EPropGroup(101, new EProp(102, "title", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 625.0,0.5);

        eTyped = new ETyped(1, "a", "entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "title", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500,0.5);

        eTyped = new ETyped(1, "a", "entity", 0, 0);
        group = new EPropGroup(101, new EProp(102, "context", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 2500,0.5);

        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        group = new EPropGroup(101, new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 2,0.5);

        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        group = new EPropGroup(101, new EProp(102, "fieldId", Constraint.of(ConstraintOp.likeAny)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 5,0.5);

        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        group = new EPropGroup(101,
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 25,0.5);

        eTyped = new ETyped(1, "a", "reference", 0, 0);
        group = new EPropGroup(101, new EProp(102, "url", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100.0,0.5);

        eTyped = new ETyped(1, "a", "insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 62.5,0.5);
    }

    @Test
    public void testNodeFilterStatisticsDualEprop() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "title", Constraint.of(ConstraintOp.eq)),
                new EProp(103, "nicknames", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500,0.5);

        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "logicalId", Constraint.of(ConstraintOp.eq)),
                new EProp(103, "stringValue", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 2,0.5);

        eTyped = new ETyped(1, "a", "reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "context", Constraint.of(ConstraintOp.likeAny)),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100,0.5);

        eTyped = new ETyped(1, "a", "insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.eq)),
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 25,0.5);

        eTyped = new ETyped(1, "a", "insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 62.5,0.5);
    }

    @Test
    public void testNodeFilterStatisticsDualEpropMore() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "", Constraint.of(ConstraintOp.like)),
                new EProp(103, "nicknames", Constraint.of(ConstraintOp.contains)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1250,0.5);

        eTyped = new ETyped(1, "a", "entity", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "bbb", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1250,0.5);

        eTyped = new ETyped(1, "a", "entity", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "bbb", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 1000,0.5);

        eTyped = new ETyped(1, "a", "e.value", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq)),
                new EProp(103, "title", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 2,0.5);

        eTyped = new ETyped(1, "a", "reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "context", Constraint.of(ConstraintOp.likeAny)),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 100,0.5);

        eTyped = new ETyped(1, "a", "insight", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny)),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 62.5,0.5);
    }

    @Test
    public void testNodeNoSuch() throws IOException {
        StatisticsProvider statisticsProvider = statisticalProvider.get(ontology);
        ETyped eTyped = new ETyped(1, "a", "entity1", 0, 0);
        assertEquals(statisticsProvider.getNodeStatistics(eTyped ).getTotal(), 1000000,0.5);

        eTyped = new ETyped(1, "a", "rs.value", 0, 0);
        EPropGroup group = new EPropGroup(101,
                new EProp(102, "fieldId", Constraint.of(ConstraintOp.eq)),
                new EProp(103, "title123", Constraint.of(ConstraintOp.like)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 5000,0.5);

        eTyped = new ETyped(1, "a", "reference", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "a", Constraint.of(ConstraintOp.likeAny)),
                new EProp(103, "b", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 500,0.5);

        eTyped = new ETyped(1, "a", "insighta", 0, 0);
        group = new EPropGroup(101,
                new EProp(102, "entityIds", Constraint.of(ConstraintOp.likeAny)),
                new EProp(103, "url", Constraint.of(ConstraintOp.eq)));
        assertEquals(statisticsProvider.getNodeFilterStatistics(eTyped, group).getTotal(), 5000,0.5);
    }


}
